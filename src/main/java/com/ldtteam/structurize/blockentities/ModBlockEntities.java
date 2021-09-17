package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Constants.MOD_ID);

    public static DeferredRegister<BlockEntityType<?>> getRegistry()
    {
        return BLOCK_ENTITIES;
    }

    @ObjectHolder("tagsubstitution")
    public static BlockEntityType<BlockEntityTagSubstitution> TAG_SUBSTITUTION;

    static
    {
        getRegistry().register("tagsubstitution",
          () -> BlockEntityType.Builder.of(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null));
    }
}
