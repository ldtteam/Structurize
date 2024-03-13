package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.IOPool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;
import static com.ldtteam.structurize.api.constants.WindowConstants.*;

/**
 * Window class for the style picker.
 */
public class WindowSwitchPack extends AbstractWindowSkeleton
{
    private static final String WINDOW_TAG_TOOL    = ":gui/windowswitchpack.xml";

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
    private ScrollingList  packList;

    /**
     * List of packs.
     */
    private List<StructurePackMeta> packMetas;

    /**
     * The packs by category.
     */
    private List<Object> sortedPacks = new ArrayList<>();

    /**
     * Future list of packs.
     */
    private Future<List<StructurePackMeta>> packMetasFuture;

    /**
     * Random with a fixed random seed.
     */
    private static int randomSeed = new Random().nextInt();

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
        registerButton(BUTTON_SELECT1, this::selectClicked);
        registerButton(BUTTON_SELECT2, this::selectClicked);

        registerButton(BUTTON_CANCEL, this::cancelClicked);

        this.prevWindow = prevWindow;
        this.packPredicate = packPredicate;

        findPaneOfTypeByID(FILTER_NAME, TextField.class).setHandler(input -> {
            final String filter = findPaneOfTypeByID(FILTER_NAME, TextField.class).getText().toLowerCase(Locale.US);

            final List<StructurePackMeta> list = new ArrayList<>();
            final Map<String, List<StructurePackMeta>> categories = new TreeMap<>();
            for (final StructurePackMeta meta : packMetas)
            {
                if (meta.getName().toLowerCase(Locale.US).contains(filter))
                {
                    list.add(meta);
                    final List<StructurePackMeta> catList = categories.getOrDefault(meta.getOwner(), new ArrayList<>());
                    catList.add(meta);
                    categories.put(meta.getOwner(), catList);
                }
            }

            sortedPacks.clear();
            for (final Map.Entry<String, List<StructurePackMeta>> entry : categories.entrySet())
            {
                sortedPacks.add(entry.getKey());
                sortedPacks.add(entry.getKey());

                sortedPacks.addAll(entry.getValue());
                if (entry.getValue().size() % 2 != 0)
                {
                    sortedPacks.add(null);
                }
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

    /**
     * On choosing a style.
     * @param button the clicked button.
     */
    public void selectClicked(final Button button)
    {
        if (prevWindow == null)
        {
            close();
            return;
        }

        int index = packList.getListElementIndexByPane(button);
        if (button.getID().contains("1"))
        {
            StructurePacks.selectedPack = (StructurePackMeta) sortedPacks.get(index * 2);
            prevWindow.get().open();
        }
        else
        {
            StructurePacks.selectedPack = (StructurePackMeta) sortedPacks.get(index * 2 + 1);
            prevWindow.get().open();
        }
    }

    @Override
    public void onOpened()
    {
        packMetas = Collections.emptyList();
        sortedPacks.clear();

        packMetasFuture = IOPool.submit(() ->
        {
            if (!StructurePacks.waitUntilFinishedLoading())
            {
                return Collections.emptyList();
            }

            // Here we would query from the online schematic server additional styles then, which, on select, we'd download to the server side.

            return new ArrayList<>(StructurePacks.getPackMetas().stream().filter(packPredicate).toList());
        });

        packList = findPaneOfTypeByID("packs", ScrollingList.class);

        super.onOpened();
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
                e.printStackTrace();
            }
            packMetasFuture = null;

            if (!packMetas.isEmpty())
            {
                Collections.shuffle(packMetas, new Random(randomSeed));
            }

            final Map<String, List<StructurePackMeta>> categories = new TreeMap<>();
            for (final StructurePackMeta meta : packMetas)
            {
                final List<StructurePackMeta> catList = categories.getOrDefault(meta.getOwner(), new ArrayList<>());
                catList.add(meta);
                categories.put(meta.getOwner(), catList);
            }

            sortedPacks.clear();
            for (final Map.Entry<String, List<StructurePackMeta>> entry : categories.entrySet())
            {
                sortedPacks.add(entry.getKey());
                sortedPacks.add(entry.getKey());

                sortedPacks.addAll(entry.getValue());
                if (entry.getValue().size() % 2 != 0)
                {
                    sortedPacks.add(null);
                }
            }

            updatePacks();
        }
    }


    /**
     * Updates the current pack list.
     */
    public void updatePacks()
    {
        packList.enable();
        packList.show();

        packList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return sortedPacks.size() / 2;
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                int metaStart = index * 2;

                final Object obj1 = sortedPacks.get(metaStart);
                final Object obj2 = sortedPacks.get(metaStart + 1);

                rowPane.findPaneByID("box0").hide();
                if (obj1 instanceof StructurePackMeta meta1)
                {
                    fillForMeta(rowPane, meta1, "1");
                    rowPane.findPaneByID("box1").show();
                }
                else
                {
                    rowPane.findPaneByID("box1").hide();
                }

                if (obj2 instanceof StructurePackMeta meta2)
                {
                    fillForMeta(rowPane, meta2, "2");
                    rowPane.findPaneByID("box2").show();
                }
                else
                {
                    rowPane.findPaneByID("box2").hide();
                }

                if (obj1 instanceof String str)
                {
                    rowPane.findPaneByID("box0").show();
                    rowPane.findPaneOfTypeByID("category", Text.class).setText(Component.literal(StringUtils.capitalize(str)));
                }
            }
        });
    }

    private static void fillForMeta(final Pane rowPane, final StructurePackMeta packMeta, final String side)
    {
        rowPane.findPaneOfTypeByID("name" + side, Text.class).setText(Component.literal(packMeta.getName()));
        rowPane.findPaneOfTypeByID("desc" + side, Text.class).setText(Component.literal(packMeta.getDesc()));
        StringBuilder author = new StringBuilder("Authors: ");
        for (int i = 0; i < packMeta.getAuthors().size(); i++)
        {
            author.append(packMeta.getAuthors().get(i));
            if (i + 1 < packMeta.getAuthors().size())
            {
                author.append(", ");
            }
        }
        rowPane.findPaneOfTypeByID("authors" + side, Text.class).setText(Component.literal(author.toString()));
        if (!packMeta.getIconPath().isEmpty())
        {
            rowPane.findPaneOfTypeByID("icon" + side, Image.class).setImage(OutOfJarResourceLocation.of(MOD_ID, packMeta.getPath().resolve(packMeta.getIconPath())), false);
        }

        rowPane.findPaneOfTypeByID("select" + side, Button.class).setTextColor(ChatFormatting.BLACK.getColor());
    }
}
