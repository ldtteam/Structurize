package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.network.messages.UpdateClientRender;
import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
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
    protected BlockPos.MutableBlockPos startPos = new BlockPos.MutableBlockPos();

    /**
     * The end position to iterate to.
     */
    protected BlockPos.MutableBlockPos endPos = new BlockPos.MutableBlockPos();

    /**
     * A pre-existing list of positions to work on
     */
    private List<BlockPos> workPosList = new ArrayList<>();

    /**
     * The current list index pos
     */
    private int currentListIndex = 0;

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
        this.startPos = new BlockPos.MutableBlockPos(Math.min(startPos.getX(), endPos.getX()), Math.min(startPos.getY(), endPos.getY()), Math.min(startPos.getZ(), endPos.getZ()));
        this.endPos = new BlockPos.MutableBlockPos(Math.max(startPos.getX(), endPos.getX()), Math.max(startPos.getY(), endPos.getY()), Math.max(startPos.getZ(), endPos.getZ()));
        for (int x = startPos.getX(); x <= endPos.getX(); x++)
        {
            for (int z = startPos.getZ(); z <= endPos.getZ(); z++)
            {
                for (int y = startPos.getY(); y <= endPos.getY(); y++)
                {
                    workPosList.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    protected AreaOperation(final Component storageText, final Player player, final List<BlockPos> workPosList)
    {
        super(new ChangeStorage(storageText, player != null ? player.getUUID() : UUID.randomUUID()));
        this.player = player;
        this.workPosList = workPosList;
    }

    @Override
    public final boolean apply(final ServerLevel world)
    {
        if (player != null && player.level().dimension() != world.dimension())
        {
            return false;
        }

        int count = 0;
        for (int i = currentListIndex; i < workPosList.size(); i++)
        {
            final BlockPos currentPos = workPosList.get(i);
            currentListIndex = i;
            apply(world, currentPos);

            this.startPos.set(Math.min(startPos.getX(), currentPos.getX()), Math.min(startPos.getY(), currentPos.getY()), Math.min(startPos.getZ(), currentPos.getZ()));
            this.endPos.set(Math.max(endPos.getX(), currentPos.getX()), Math.max(endPos.getY(), currentPos.getY()), Math.max(endPos.getZ(), currentPos.getZ()));

            count++;
            if (count >= Structurize.getConfig().getServer().maxOperationsPerTick.get())
            {
                return false;
            }
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
