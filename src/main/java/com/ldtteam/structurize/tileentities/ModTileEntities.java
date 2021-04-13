package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public final class ModTileEntities
{
    private ModTileEntities() { /* prevent construction */ }

    private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Constants.MOD_ID);

    public static DeferredRegister<TileEntityType<?>> getRegistry()
    {
        return TILE_ENTITIES;
    }

    @ObjectHolder("multiblock")
    public static TileEntityType<TileEntityMultiBlock> MULTIBLOCK;

    static
    {
        getRegistry().register("multiblock",
          () -> TileEntityType.Builder.create(TileEntityMultiBlock::new, ModBlocks.multiBlock.get()).build(null));
    }
}
