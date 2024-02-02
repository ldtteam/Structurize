package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class ModBlockEntities
{
    private ModBlockEntities() { /* prevent construction */ }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public static DeferredRegister<BlockEntityType<?>> getRegistry()
    {
        return BLOCK_ENTITIES;
    }

    public static RegistryObject<BlockEntityType<BlockEntityTagSubstitution>> TAG_SUBSTITUTION = getRegistry().register("tagsubstitution",
      () -> BlockEntityType.Builder.of(BlockEntityTagSubstitution::new, ModBlocks.blockTagSubstitution.get()).build(null));
}
