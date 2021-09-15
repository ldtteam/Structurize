package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public final class ModTileEntities
{
    private ModTileEntities() { /* prevent construction */ }

    private static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Constants.MOD_ID);

    public static DeferredRegister<BlockEntityType<?>> getRegistry()
    {
        return TILE_ENTITIES;
    }

    @ObjectHolder("tagsubstitution")
    public static BlockEntityType<TileEntityTagSubstitution> TAG_SUBSTITUTION;

    static
    {
        getRegistry().register("tagsubstitution",
          () -> BlockEntityType.Builder.of(TileEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null));
    }
}
