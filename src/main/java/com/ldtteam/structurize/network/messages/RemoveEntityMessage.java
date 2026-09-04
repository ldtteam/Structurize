package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveEntityOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.LogicalSide;
import com.ldtteam.structurize.network.NetworkContext;
import org.jetbrains.annotations.Nullable;

/**
 * Message to remove an entity from the world.
 */
public class RemoveEntityMessage implements IMessage
{
    /**
     * Position to scan from.
     */
    private final BlockPos from;

    /**
     * Position to scan to.
     */
    private final BlockPos to;

    /**
     * The entity to remove from the world.
     */
    private final Identifier entityName;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveEntityMessage(final FriendlyByteBuf buf)
    {
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.entityName = buf.readIdentifier();
    }

    /**
     * Create a message to remove an entity from the world.
     *
     * @param pos1       start coordinate.
     * @param pos2       end coordinate.
     * @param entityName the entity to remove.
     */
    public RemoveEntityMessage(final BlockPos pos1, final BlockPos pos2, final Identifier entityName)
    {
        this.from = pos1;
        this.to = pos2;
        this.entityName = entityName;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeIdentifier(entityName);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkContext ctxIn, final boolean isLogicalServer)
    {
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(entityName);
        if (type != null)
        {
            Manager.addToQueue(new RemoveEntityOperation(ctxIn.getSender(), from, to, type));
        }
    }
}
