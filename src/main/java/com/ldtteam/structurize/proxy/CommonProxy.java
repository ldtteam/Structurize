package com.ldtteam.structurize.proxy;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.MultiBlock;
import com.ldtteam.structurize.event.FMLEventHandler;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.tileentities.TileEntityMultiBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CommonProxy of the structurize mod (Server and Client).
 */
@Mod.EventBusSubscriber
public class CommonProxy implements IProxy
{
    /**
     * Used to store IExtendedEntityProperties data temporarily between player death and respawn.
     */
    private static final Map<String, NBTTagCompound> playerPropertiesData = new HashMap<>();

    /**
     * Adds an entity's custom data to the map for temporary storage.
     *
     * @param name     player UUID + Properties name, HashMap key.
     * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties
     *                 data only.
     */
    public static void storeEntityData(final String name, final NBTTagCompound compound)
    {
        playerPropertiesData.put(name, compound);
    }

    /**
     * Removes the compound from the map and returns the NBT tag stored for name
     * or null if none exists.
     *
     * @param name player UUID + Properties name, HashMap key.
     * @return NBTTagCompound PlayerProperties NBT compound.
     */
    public static NBTTagCompound getEntityData(final String name)
    {
        return playerPropertiesData.remove(name);
    }

    /**
     * Called when registering blocks,
     * we have to register all our modblocks here.
     *
     * @param event the registery event for blocks.
     */
    @SubscribeEvent
    public static void registerBlocks(@NotNull final RegistryEvent.Register<Block> event)
    {
        ModBlocks.init(event.getRegistry());
    }

    /**
     * Called when registering items,
     * we have to register all our mod items here.
     *
     * @param event the registery event for items.
     */
    @SubscribeEvent
    public static void registerItems(@NotNull final RegistryEvent.Register<Item> event)
    {
        ModItems.init(event.getRegistry());
        ModBlocks.registerItemBlock(event.getRegistry());
    }

    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void registerTileEntities()
    {
        GameRegistry.registerTileEntity(TileEntityMultiBlock.class, new ResourceLocation(Constants.MOD_ID, MultiBlock.getName()));
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openScanToolWindow(final BlockPos pos1, final BlockPos pos2)
    {
        /*
         * Intentionally left empty.
         */
    }

    /*
    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation)
    {
        /*
         * Intentionally left empty.
         *
    }
    */

    @Override
    public void openShapeToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openClipBoardWindow(final int colonyId)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    @Nullable
    @Override
    public World getWorldFromMessage(@NotNull final MessageContext context)
    {
        return context.getServerHandler().player.getServerWorld();
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final EntityPlayer player)
    {
        return ((EntityPlayerMP) player).getRecipeBook();
    }

    @Override
    public void openMultiBlockWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
