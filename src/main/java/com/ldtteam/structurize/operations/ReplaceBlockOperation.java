package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.BlockUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.UUID;

/**
 * Operation for removing a single type of block.
 */
public class ReplaceBlockOperation extends AreaOperation
{
    /**
     * The block to replace from.
     */
    private final ItemStack fromBlock;

    /**
     * The block to replace to.
     */
    private final ItemStack toBlock;

    /**
     * Replacement percentage.
     */
    private final int percentage;

    /**
     * Default constructor.
     *
     * @param player     the player who initiated the area operation.
     * @param startPos   the start pos to iterate from.
     * @param endPos     the end pos to iterate to.
     * @param fromBlock  the block to replace from.
     * @param toBlock    the block to replace to.
     * @param percentage the replacement percentage.
     */
    public ReplaceBlockOperation(final Player player, final BlockPos startPos, final BlockPos endPos, final ItemStack fromBlock, final ItemStack toBlock, final int percentage)
    {
        super(Component.translatable("com.ldtteam.structurize.replace_block", fromBlock.getDisplayName(), toBlock.getDisplayName()), player, startPos, endPos);
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.percentage = Mth.clamp(0, 100, percentage);
    }

    @Override
    protected void apply(final ServerLevel world, final BlockPos position)
    {
        final FakePlayer fakePlayer = new FakePlayer(world, new GameProfile(player == null ? UUID.randomUUID() : player.getUUID(), "structurizefakeplayer"));
        if (percentage < 100 && fakePlayer.getRandom().nextInt(100) > percentage)
        {
            return;
        }

        final BlockState blockState = world.getBlockState(position);

        if (BlockUtils.doBlocksMatch(fromBlock, world, position))
        {
            storage.addPreviousDataFor(position, world);
            BlockUtils.handleCorrectBlockPlacement(world, fakePlayer, toBlock, blockState, position);
            storage.addPostDataFor(position, world);
        }
    }
}
