package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION = BLOCK_ENTITIES.register("tagsubstitution",
      () -> BlockEntityType.Builder.of(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null));
}
