package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.client.BlocksToRenderTypeHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import java.util.List;
import java.util.function.BiConsumer;

public interface IStructurizeBlockCollection extends IBlockCollection
{
    default List<RegistryObject<Block>> create(DeferredRegister<Block> registrar,
        DeferredRegister<Item> itemRegistrar,
        CreativeModeTab group,
        BlockType... types)
    {
        return IBlockCollection.super.create(registrar,
          itemRegistrar,
          group,
          new BiConsumer<BlockType, RegistryObject<Block>>() {
              @Override
              public void accept(final BlockType blockType, final RegistryObject<Block> blockRegistryObject)
              {
                  DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BlocksToRenderTypeHelper.registerBlockType(blockType, blockRegistryObject));
              }
          }, types);
    }
}
