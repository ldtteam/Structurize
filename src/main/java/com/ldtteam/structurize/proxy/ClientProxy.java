package com.ldtteam.structurize.proxy;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.cactus.BlockCactusDoor;
import com.ldtteam.structurize.blocks.decorative.BlockPaperwall;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.blocks.types.PaperwallType;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowMultiBlock;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.event.ClientEventHandler;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structures.client.TemplateBlockInfoTransformHandler;
import com.ldtteam.structures.event.RenderEventHandler;
import com.ldtteam.structures.helpers.Settings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /**
     * Inventory description string.
     */
    private static final String INVENTORY = "inventory";

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void registerEvents()
    {
        super.registerEvents();

        MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public void openBuildToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos);
        window.open();
    }

    @Override
    public void openShapeToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowShapeTool window = new WindowShapeTool(pos);
        window.open();
    }

    @Override
    public void openScanToolWindow(@Nullable final BlockPos pos1, @Nullable final BlockPos pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return;
        }

        @Nullable final WindowScan window = new WindowScan(pos1, pos2);
        window.open();
    }

    /*
    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos, structureName, rotation, null);
        window.open();
    }
    */

    /**
     * Creates a custom model ResourceLocation for a block with metadata 0
     */
    private static void createCustomModel(final Block block)
    {
        final Item item = Item.getItemFromBlock(block);
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0,
              new ModelResourceLocation(block.getRegistryName(), INVENTORY));
        }
    }

    /**
     * Creates a custom model ResourceLocation for an item with metadata 0
     */
    private static void createCustomModel(final Item item)
    {
        if (item != null)
        {
            ModelLoader.setCustomModelResourceLocation(item, 0,
              new ModelResourceLocation(item.getRegistryName(), INVENTORY));
        }
    }

    /**
     * Event handler for forge ModelRegistryEvent event.
     *
     * @param event the forge pre ModelRegistryEvent event.
     */
    @SubscribeEvent
    public static void registerModels(@NotNull final ModelRegistryEvent event)
    {
        createCustomModel(ModBlocks.blockSubstitution);

        createCustomModel(ModBlocks.blockSolidSubstitution);

        createCustomModel(ModItems.buildTool);
        createCustomModel(ModItems.shapeTool);
        createCustomModel(ModItems.caliper);
        createCustomModel(ModItems.scanTool);

        createCustomModel(ModBlocks.blockCactusPlank);
        createCustomModel(ModBlocks.blockCactusTrapdoor);
        createCustomModel(ModBlocks.blockCactusStair);
        createCustomModel(ModBlocks.blockCactusSlabHalf);
        createCustomModel(ModBlocks.blockCactusSlabDouble);
        createCustomModel(ModItems.itemCactusDoor);
        createCustomModel(ModBlocks.blockCactusFence);
        createCustomModel(ModBlocks.blockCactusFenceGate);

        // Achievement proxy Items
        createCustomModel(ModBlocks.blockShingleSlab);

        ModelLoader.setCustomStateMapper(ModBlocks.blockCactusDoor, new StateMap.Builder().ignore(BlockCactusDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.blockPaperWall, new StateMap.Builder().withName(BlockPaperwall.VARIANT).withSuffix("_blockPaperwall").build());

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleOak), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.OAK.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleBirch), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.BIRCH.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleSpruce), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.SPRUCE.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleJungle), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.JUNGLE.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleDarkOak), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.DARK_OAK.getName()), INVENTORY));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockShingleAcacia), 0,
                new ModelResourceLocation(new ResourceLocation(Constants.MOD_ID,
                        BlockShingle.BLOCK_PREFIX + "_" + BlockPlanks.EnumType.ACACIA.getName()), INVENTORY));

        for (final PaperwallType type : PaperwallType.values())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.blockPaperWall), type.getMetadata(),
                    new ModelResourceLocation(ModBlocks.blockPaperWall.getRegistryName() + "_" + type.getName(), INVENTORY));
        }

        for (final BlockTimberFrame frame : ModBlocks.getTimberFrames())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(frame), 0,
                    new ModelResourceLocation(frame.getRegistryName(), INVENTORY));
        }

        createCustomModel(ModBlocks.multiBlock);

        //Additionally we register an exclusion handler here;
        TemplateBlockInfoTransformHandler.getInstance().AddTransformHandler(
          (b) -> b.blockState.getBlock() == ModBlocks.blockSubstitution,
          (b) -> new Template.BlockInfo(b.pos, Blocks.AIR.getDefaultState(), null)
        );

        //Additionally we register an exclusion handler here;
        TemplateBlockInfoTransformHandler.getInstance().AddTransformHandler(
          (b) -> b.blockState.getBlock() == ModBlocks.blockSolidSubstitution,
          (b) -> new Template.BlockInfo(b.pos, Blocks.AIR.getDefaultState(), null)
        );


    }

    @Override
    public File getSchematicsFolder()
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            if (Manager.getServerUUID() != null)
            {
                return new File(Minecraft.getMinecraft().gameDir, Constants.MOD_ID + "/" + Manager.getServerUUID());
            }
            else
            {
                Log.getLogger().error("Manager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder  /structurize/schematics if on the physical client on the logical server
        final File worldSchematicFolder = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory() + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getMinecraft().gameDir, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getClientHandler().world;
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            return super.getWorld(dimension);
        }
        return Minecraft.getMinecraft().world;
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final EntityPlayer player)
    {
        if (player instanceof EntityPlayerSP)
        {
            return ((EntityPlayerSP) player).getRecipeBook();
        }

        return super.getRecipeBookFromPlayer(player);
    }

    @Override
    public void openMultiBlockWindow(@Nullable final BlockPos pos)
    {
        @Nullable final WindowMultiBlock window = new WindowMultiBlock(pos);
        window.open();
    }
}
