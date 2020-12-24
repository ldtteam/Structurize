package com.ldtteam.blockout.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import com.ldtteam.blockout.hooks.TriggerMechanism.Type;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

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

    protected void registerInternal(final U targetThing,
        final ResourceLocation guiLoc,
        final long expirationTime,
        final TriggerMechanism<?> trigger,
        final IGuiActionCallback<? extends T> onOpen,
        final IGuiActionCallback<? extends T> onClose)
    {
        Objects.requireNonNull(targetThing, "Target can't be null!");
        Objects.requireNonNull(guiLoc, "Gui location can't be null!");
        Objects.requireNonNull(trigger, "Trigger can't be null!");

        final IGuiActionCallback<T> onOpenListener = requireNonNullElse((IGuiActionCallback<T>) onOpen, (t, w, tt) -> {});
        final IGuiActionCallback<T> onClosedListener = requireNonNullElse((IGuiActionCallback<T>) onClose, (t, w, tt) -> {});
        final ResourceLocation registryKey = targetThing.getRegistryName();

        if (registryKeys.contains(registryKey))
        {
            final HookEntry existing = registry.stream()
                .filter(hook -> hook.targetThing.getRegistryName().equals(registryKey))
                .findFirst()
                .get();
            if (existing.trigger.getType() == trigger.getType())
            {
                throw new IllegalArgumentException(
                    String.format("\"%s\" with trigger \"%s\" is already registerd!", targetThing, existing.trigger));
            }
        }

        registry.add(new HookEntry(targetThing, guiLoc, expirationTime, trigger, onOpenListener, onClosedListener));
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
    protected abstract void translateToGuiBottomCenter(final MatrixStack ms, final T thing, final float partialTicks);

    protected void tick(final long ticks)
    {
        final long now = System.currentTimeMillis();

        // find new things
        registry.forEach(hook -> {
            if (hook.trigger.getType() == Type.DISTANCE && ticks % (Constants.TICKS_SECOND / 2) != 0) // tick distance trigger only every half-second
            {
                return;
            }

            findTriggered(hook.targetThing, hook.trigger).forEach(thing -> {
                final K key = keyMapper(thing);
                final WindowEntry entry = activeWindows.get(key);

                if (entry == null) // new entry
                {
                    final WindowEntry window = new WindowEntry(now, thing, hook, HookWindow::new);
                    activeWindows.put(key, window);
                    window.screen.init(Minecraft.getInstance(), window.screen.getWindow().getWidth(), window.screen.getWindow().getHeight());
                }
                else if (entry.hook.trigger.getType() != Type.RAY_TRACE && hook.trigger.getType() == Type.RAY_TRACE) // raytrace is priority trigger
                {
                    entry.screen.onClose();

                    final WindowEntry window = new WindowEntry(now, thing, hook, HookWindow::new);
                    activeWindows.put(key, window);
                    window.screen.init(Minecraft.getInstance(), window.screen.getWindow().getWidth(), window.screen.getWindow().getHeight());
                }
                else // already existing entry
                {
                    entry.lastTimeAccessed = now;
                }
            });
        });

        // tick them all
        activeWindows.values().forEach(entry -> entry.screen.tick());
    }

    protected void render(final MatrixStack ms, final float partialTicks)
    {
        final long now = System.currentTimeMillis();
        final Iterator<WindowEntry> it = activeWindows.values().iterator();

        while (it.hasNext())
        {
            final WindowEntry entry = it.next();
            if (now - entry.lastTimeAccessed > entry.hook.expirationTime + 800) // expired entry
            {
                entry.screen.onClose();
                it.remove();
            }
            else // renderable entry
            {
                ms.push();
                translateToGuiBottomCenter(ms, entry.thing, partialTicks);
                ms.rotate(Minecraft.getInstance().getRenderManager().getCameraOrientation());
                ms.scale(-0.01F, -0.01F, 0.01F);
                entry.screen.tick();
                entry.screen.render(ms);
                ms.pop();
            }
        }
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
        protected final IGuiActionCallback<T> onOpen;
        protected final IGuiActionCallback<T> onClose;

        private HookEntry(final U targetThing,
            final ResourceLocation guiLoc,
            final long expirationTime,
            final TriggerMechanism<?> trigger,
            final IGuiActionCallback<T> onOpen,
            final IGuiActionCallback<T> onClose)
        {
            this.targetThing = targetThing;
            this.guiLoc = guiLoc;
            this.expirationTime = expirationTime;
            this.trigger = trigger;
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
