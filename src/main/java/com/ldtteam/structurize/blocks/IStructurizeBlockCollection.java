package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.event.ClientLifecycleSubscriber;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import java.util.List;

public interface IStructurizeBlockCollection extends IBlockCollection
{
    default List<RegistryObject<Block>> create(DeferredRegister<Block> registrar,
        DeferredRegister<Item> itemRegistrar,
        ItemGroup group,
        BlockType... types)
    {
        return IBlockCollection.super.create(registrar,
            itemRegistrar,
            group,
            (type, block) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientLifecycleSubscriber.DELAYED_RENDER_TYPE_SETUP.add(new Tuple<>(block, type.getRenderType()))),
            types);
    }
}
