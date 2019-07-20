package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockDoor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

public class BlockCactusDoor extends AbstractBlockDoor<BlockCactusDoor>
{
    private static final String BLOCK_NAME = "blockcactusdoor";

    public BlockCactusDoor()
    {
        super(Block.Properties.from(Blocks.OAK_DOOR));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }

    @Override
    public ItemStack getPickBlock(final BlockState state, final RayTraceResult target, final IBlockReader world, final BlockPos pos, final PlayerEntity player)
    {
        return new ItemStack(this);
    }

    @NotNull
    @Override
    public Item asItem()
    {
        return super.asItem();
    }
}
