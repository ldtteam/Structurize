package com.ldtteam.blockout.hooks;

import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Core class for managing and handling gui hooks
 * 
 * @param <T> instance of U
 * @param <U> forge-register type
 * @param <K> hashable thing to hash T, can be same as T
 */
public abstract class HookManager<T, U extends IForgeRegistryEntry<U>, K>
{
    /**
     * active ray trace scroll listener
     */
    private static HookScreen scrollListener;

    /**
     * list of registered hooks
     */
    private final List<HookEntry> registry = new ArrayList<>();
    /**
     * set of registered ForgeRegistry keys
     */
    private final Set<ResourceLocation> registryKeys = new HashSet<>();
    /**
     * list of windows being rendered
     */
    private final Map<K, WindowEntry> activeWindows = new HashMap<>();

    protected HookManager()
    {
    }

    /**
     * Creates a new entry in hook registry, making the system aware of new hook.
     *
     * @param targetThing    registry object of thing on which gui should be displayed on
     * @param guiLoc         location of gui xml
     * @param expirationTime how long should gui remain opened after the condition stops being satisfied [in millis]
     * @param trigger        trigger condition
     * @param shouldOpen     gets fired when gui is about to be opened, can deny opening
     * @param onOpen         gets fired when gui is opened
     * @param onClose        gets fired when gui is closed
     * @see IGuiHookable for gui callbacks
     */
    protected void registerInternal(final U targetThing,
        final ResourceLocation guiLoc,
        final long expirationTime,
        final TriggerMechanism<?> trigger,
        final BiPredicate<? extends T, Type> shouldOpen,
        final IGuiActionCallback<? extends T> onOpen,
        final IGuiActionCallback<? extends T> onClose)
    {
        Objects.requireNonNull(targetThing, "Target can't be null!");
        Objects.requireNonNull(guiLoc, "Gui location can't be null!");
        Objects.requireNonNull(trigger, "Trigger can't be null!");

        final BiPredicate<T, Type> shouldOpenTest = requireNonNullElse((BiPredicate<T, Type>) shouldOpen, (t, tt) -> true);
        final IGuiActionCallback<T> onOpenListener = requireNonNullElse((IGuiActionCallback<T>) onOpen, IGuiActionCallback.noAction());
        final IGuiActionCallback<T> onClosedListener = requireNonNullElse((IGuiActionCallback<T>) onClose, IGuiActionCallback.noAction());
        final ResourceLocation registryKey = targetThing.getRegistryName();

        if (registryKeys.contains(registryKey))
        {
            final Optional<HookEntry> existing = registry.stream()
                .filter(hook -> hook.targetThing.getRegistryName().equals(registryKey) && hook.trigger.getType() == trigger.getType())
                .findFirst();
            if (existing.isPresent())
            {
                throw new IllegalArgumentException(
                    String.format("\"%s\" with trigger \"%s\" is already registerd!", targetThing, existing.get().trigger.getName()));
            }
        }

        registry.add(new HookEntry(targetThing, guiLoc, expirationTime, trigger, shouldOpenTest, onOpenListener, onClosedListener));
        registryKeys.add(registryKey);
    }

    /**
     * @param thingType registered type
     * @param trigger   hook trigger
     * @return all things of thingType being triggered by trigger of given hook
     */
    protected abstract List<T> findTriggered(final U thingType, final TriggerMechanism<?> trigger);

    /**
     * @param thing instance of registered type
     * @return hashable key unique to every thing
     */
    protected abstract K keyMapper(final T thing);

    /**
     * Translates matrixstack from 0,0,0 to bottom_middle of gui.
     *
     * @param ms           matrixstack of world rendering
     * @param thing        instance of registered type
     * @param partialTicks partialTicks, see world rendering
     */
    protected abstract void translateToGuiBottomCenter(final PoseStack ms, final T thing, final float partialTicks);

    protected void tick(final long ticks)
    {
        final long now = System.currentTimeMillis();

        // find new things (aka trigger tick)
        registry.forEach(hook -> {
            if (hook.trigger.canTick(ticks))
            {
                findTriggered(hook.targetThing, hook.trigger).forEach(thing -> {
                    final K key = keyMapper(thing);
                    final WindowEntry entry = activeWindows.get(key);
    
                    if ((entry == null || entry.hook.trigger.isLowerPriority(hook.trigger))
                        && hook.shouldOpen.test(thing, hook.trigger.getType())) // new entry or override
                    {
                        if (entry != null)
                        {
                            entry.screen.removed();
                        }
    
                        final WindowEntry window = new WindowEntry(now, thing, hook, HookWindow::new);
                        activeWindows.put(key, window);
                        window.screen.init(Minecraft.getInstance(), window.screen.getWindow().getWidth(), window.screen.getWindow().getHeight());
                    }
                    else // already existing entry
                    {
                        entry.lastTimeAccessed = now;
                    }
                });
            }
        });

        // check for expired windows or tick them
        // values().remove() uses naive non-hash removing, but removeIf uses HashIterator
        activeWindows.values().removeIf(entry -> {
            if (entry.hook.trigger.canTick(ticks) && now - entry.lastTimeAccessed > entry.hook.expirationTime) // expired
            {
                entry.screen.removed();
                return true;
            }
            else // tickable
            {
                entry.screen.tick();
                return false;
            }
        });
    }

    protected void render(final PoseStack ms, final float partialTicks)
    {
        activeWindows.values().forEach(entry -> {
            ms.pushPose();
            translateToGuiBottomCenter(ms, entry.thing, partialTicks);
            ms.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            ms.scale(-0.01F, -0.01F, 0.01F);
            entry.screen.render(ms);
            ms.popPose();
        });
    }

    // scroll hook management

    public static boolean onScroll(final double scrollDelta)
    {
        if (scrollListener != null)
        {
            return scrollListener.mouseScrolled(scrollDelta);
        }
        return false;
    }

    public static HookScreen getScrollListener()
    {
        return scrollListener;
    }

    public static void setScrollListener(final HookScreen scrollListener)
    {
        HookManager.scrollListener = scrollListener;
    }

    /**
     * Represents registered hook.
     */
    protected class HookEntry
    {
        protected final U targetThing;
        protected final ResourceLocation guiLoc;
        protected final long expirationTime;
        protected final TriggerMechanism<?> trigger;
        protected final BiPredicate<T, Type> shouldOpen;
        protected final IGuiActionCallback<T> onOpen;
        protected final IGuiActionCallback<T> onClose;

        private HookEntry(final U targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            final BiPredicate<T, Type> shouldOpen,
            final IGuiActionCallback<T> onOpen,
            final IGuiActionCallback<T> onClose)
        {
            this.targetThing = targetThing;
            this.guiLoc = guiLoc;
            this.expirationTime = expirationTime;
            this.trigger = trigger;
            this.shouldOpen = shouldOpen;
            this.onOpen = onOpen;
            this.onClose = onClose;
        }
    }

    /**
     * Represents active (being rendered) window.
     */
    protected class WindowEntry
    {
        private long lastTimeAccessed = 0;
        protected final T thing;
        protected final HookEntry hook;
        protected final HookScreen screen;

        public WindowEntry(final long lastTimeAccessed,
            final T thing,
            final HookManager<T, U, K>.HookEntry hook,
            final Function<WindowEntry, HookWindow<T>> windowFactory)
        {
            this.lastTimeAccessed = lastTimeAccessed;
            this.thing = thing;
            this.hook = hook;
            this.screen = windowFactory.apply(this).getScreen();
        }
    }

    // java 9 feature
    private static <T> T requireNonNullElse(T obj, T defaultObj)
    {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");
    }
}
