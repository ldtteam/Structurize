package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockDoor;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

public class BlockCactusDoor extends AbstractBlockDoor<BlockCactusDoor>
{
    public BlockCactusDoor()
    {
        super(Block.Properties.create(Material.WOOD, MaterialColor.SAND)
                .hardnessAndResistance(3.0f)
                .sound(SoundType.WOOD));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusdoor");
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        return new ItemStack(ModItems.itemCactusDoor);
    }

    @NotNull
    @Override
    public Item asItem()
    {
        return super.asItem();
    }
}
