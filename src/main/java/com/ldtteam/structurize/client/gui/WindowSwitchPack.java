package com.ldtteam.structurize.client.gui;

import com.google.common.collect.Lists;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.IOPool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.GUI_SWITCH_PACK_AUTHORS;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.GUI_SWITCH_PACK_DISABLED_TEXT;
import static com.ldtteam.structurize.api.util.constant.WindowConstants.*;
import static com.ldtteam.structurize.config.ServerConfiguration.CONFIG_OPTION_ALLOW_PLAYER_SCHEMATICS;

/**
 * Window class for the style picker.
 */
public class WindowSwitchPack extends AbstractWindowSkeleton
{
    private static final String WINDOW_TAG_TOOL = ":gui/windowswitchpack.xml";

    /**
     * The parent window that opened this one.
     */
    private final Supplier<BOWindow> prevWindow;

    /**
     * The predicate to filter structure packs for display.
     */
    private final Predicate<StructurePackMeta> packPredicate;

    /**
     * Levels scrolling list.
     */
    private final ScrollingList packList;

    /**
     * The drawable components for the packs list.
     */
    private final List<DrawableComponent> drawableComponents = new ArrayList<>();

    /**
     * List of packs.
     */
    private List<StructurePackMeta> packMetas;

    /**
     * Future list of packs.
     */
    private Future<List<StructurePackMeta>> packMetasFuture;

    /**
     * Constructor for this window.
     * @param prevWindow the origin window.
     */
    public WindowSwitchPack(final Supplier<BOWindow> prevWindow)
    {
        this(prevWindow, pack -> true);
    }

    /**
     * Constructor for this window.
     * @param prevWindow the origin window.
     * @param packPredicate predicate to filter visible packs (called on IO thread, so it can block and load blueprints).
     */
    public WindowSwitchPack(final Supplier<BOWindow> prevWindow,
                            final Predicate<StructurePackMeta> packPredicate)
    {
        super(Constants.MOD_ID + WINDOW_TAG_TOOL);
        this.prevWindow = prevWindow;
        this.packPredicate = packPredicate;
        this.packList = findPaneOfTypeByID("packs", ScrollingList.class);

        registerButton(BUTTON_CANCEL, this::cancelClicked);
        findPaneOfTypeByID(FILTER_NAME, TextField.class).setHandler(input -> sortAndFilterPacks(input.getText()));
        this.packList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return drawableComponents.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("box0", Box.class).hide();
                rowPane.findPaneOfTypeByID("box1", Box.class).hide();
                rowPane.findPaneOfTypeByID("box2", Box.class).hide();

                drawableComponents.get(index).render(rowPane);
            }
        });
    }

    /**
     * On clicking the cancel button.
     */
    public void cancelClicked()
    {
        if (prevWindow == null)
        {
            close();
            return;
        }
        prevWindow.get().open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        packMetas = Collections.emptyList();
        drawableComponents.clear();

        packMetasFuture = IOPool.submit(() ->
        {
            if (!StructurePacks.waitUntilFinishedLoading())
            {
                return Collections.emptyList();
            }

            // Here we would query from the online schematic server additional styles then, which, on select, we'd download to the server side.

            return new ArrayList<>(StructurePacks.getPackMetas().stream().filter(packPredicate).toList());
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (packMetasFuture != null && packMetasFuture.isDone())
        {
            try
            {
                packMetas = packMetasFuture.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                Log.getLogger().error("Error resolving pack metas", e);
            }
            packMetasFuture = null;

            if (!packMetas.isEmpty())
            {
                Collections.shuffle(packMetas, Constants.rand);
            }

            sortAndFilterPacks("");
            packList.on();
        }
    }

    /**
     * Sort and filter all the packs in the window.
     *
     * @param filter the filter string.
     */
    private void sortAndFilterPacks(final String filter)
    {
        final Map<String, List<StructurePackMeta>> categories = new TreeMap<>();

        // First, determine all packs
        for (final StructurePackMeta meta : packMetas)
        {
            if (StringUtils.isBlank(filter) || StringUtils.containsIgnoreCase(meta.getName(), filter))
            {
                categories.compute(meta.getOwner(), (k, v) -> v == null ? new ArrayList<>() : v).add(meta);
            }
        }

        // Second, iterate again but start creating the drawable components
        drawableComponents.clear();
        for (final Map.Entry<String, List<StructurePackMeta>> entry : categories.entrySet())
        {
            drawableComponents.add(new CategoryDrawableComponent(entry.getKey()));

            for (final List<StructurePackMeta> meta : Lists.partition(entry.getValue(), 2))
            {
                if (meta.size() == 2)
                {
                    drawableComponents.add(new PacksDrawableComponent(prevWindow, meta.get(0), meta.get(1)));
                }
                else
                {
                    drawableComponents.add(new PacksDrawableComponent(prevWindow, meta.get(0), null));
                }
            }
        }

        packList.refreshElementPanes(true);
    }

    private interface DrawableComponent
    {
        void render(final Pane rowPane);
    }

    private record CategoryDrawableComponent(String category) implements DrawableComponent
    {
        @Override
        public void render(final Pane rowPane)
        {
            rowPane.findPaneOfTypeByID("box0", Box.class).show();
            rowPane.findPaneOfTypeByID("category", Text.class).setText(Component.literal(StringUtils.capitalize(category)));
        }
    }

    private record PacksDrawableComponent(
        @NotNull Supplier<BOWindow> prevWindow,
        @NotNull StructurePackMeta first,
        @Nullable StructurePackMeta second) implements DrawableComponent
    {
        @Override
        public void render(final Pane rowPane)
        {
            rowPane.findPaneOfTypeByID("box1", Box.class).show();
            fillForMeta(rowPane, first, "1");

            if (second != null)
            {
                rowPane.findPaneOfTypeByID("box2", Box.class).show();
                fillForMeta(rowPane, second, "2");
            }
        }

        private void fillForMeta(final Pane rowPane, final StructurePackMeta packMeta, final String side)
        {
            rowPane.findPaneOfTypeByID("name" + side, Text.class).setText(Component.literal(packMeta.getName()));
            rowPane.findPaneOfTypeByID("desc" + side, Text.class).setText(Component.literal(packMeta.getDesc()));
            rowPane.findPaneOfTypeByID("authors" + side, Text.class).setText(Component.translatable(GUI_SWITCH_PACK_AUTHORS, String.join(", ", packMeta.getAuthors())));
            if (!packMeta.getIconPath().isEmpty())
            {
                rowPane.findPaneOfTypeByID("icon" + side, Image.class).setImage(OutOfJarResourceLocation.of(MOD_ID, packMeta.getPath().resolve(packMeta.getIconPath())), false);
            }

            final Button selectButton = rowPane.findPaneOfTypeByID("select" + side, Button.class);
            selectButton.setEnabled(!packMeta.isDisabled());
            selectButton.setHandler(h -> {
                StructurePacks.switchSelectedPack(packMeta);
                prevWindow.get().open();
            });

            if (packMeta.isDisabled())
            {
                final MutableComponent configOptionComponent = Component.literal(CONFIG_OPTION_ALLOW_PLAYER_SCHEMATICS).withStyle(ChatFormatting.GOLD);
                PaneBuilders.tooltipBuilder()
                    .append(Component.translatable(GUI_SWITCH_PACK_DISABLED_TEXT, configOptionComponent))
                    .hoverPane(selectButton).build();
            }
        }
    }
}
