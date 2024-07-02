package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.network.messages.UpdateClientRender;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Operations running on entire areas, require a start and end position and will iterate over the entire area.
 */
public abstract class AreaOperation extends BaseOperation
{
    /**
     * The player who initiated the area operation.
     */
    protected final Player player;

    /**
     * The start position to iterate from.
     */
    protected final BlockPos startPos;

    /**
     * The end position to iterate to.
     */
    protected final BlockPos endPos;

    /**
     * The current iterator position.
     */
    private BlockPos currentPos;

    /**
     * Default constructor.
     *
     * @param storageText the text for the change storage.
     * @param player      the player who initiated the area operation.
     * @param startPos    the start pos to iterate from.
     * @param endPos      the end pos to iterate to.
     */
    protected AreaOperation(final Component storageText, final Player player, final BlockPos startPos, final BlockPos endPos)
    {
        super(new ChangeStorage(storageText, player != null ? player.getUUID() : UUID.randomUUID()));
        this.player = player;
        this.startPos = new BlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.endPos = new BlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
        this.currentPos = this.startPos;
    }

    @Override
    public final boolean apply(final ServerLevel world)
    {
        if (player != null && player.level().dimension() != world.dimension())
        {
            return false;
        }

        int count = 0;
        for (int y = currentPos.getY(); y <= endPos.getY(); y++)
        {
            for (int x = currentPos.getX(); x <= endPos.getX(); x++)
            {
                for (int z = currentPos.getZ(); z <= endPos.getZ(); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);

                    apply(world, here);

                    count++;
                    if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
                    {
                        currentPos = here;
                        return false;
                    }
                }
                currentPos = new BlockPos(x, y, startPos.getZ());
            }
            currentPos = new BlockPos(startPos.getX(), y, startPos.getZ());
        }

        Network.getNetwork().sendToEveryone(new UpdateClientRender(startPos, endPos));

        return true;
    }

    /**
     * Apply the operation on the world.
     *
     * @param world    the world to apply them on.
     * @param position the current area position.
     */
    protected abstract void apply(final ServerLevel world, final BlockPos position);
}
