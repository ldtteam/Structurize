package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static DeferredRegister<BlockEntityType<?>> getRegistry()
    {
        return BLOCK_ENTITIES;
    }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION =
        getRegistry().register(
            "tagsubstitution",
            () -> new BlockEntityType<>(BlockEntityTagSubstitution::new, new Block[] {ModBlocks.blockTagSubstitution.value()}));
}
