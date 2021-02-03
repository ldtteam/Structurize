package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class StructurizeTileEntities
{
    @ObjectHolder("multiblock")
    public static TileEntityType<TileEntityMultiBlock> MULTIBLOCK;

    @SubscribeEvent
    public static void registerTileEntity(final RegistryEvent.Register<TileEntityType<?>> event)
    {
        MULTIBLOCK = TileEntityType.Builder.create(TileEntityMultiBlock::new,
          ModBlocks.multiBlock).build(null);
        MULTIBLOCK.setRegistryName(Constants.MOD_ID, "multiblock");
    }
}
