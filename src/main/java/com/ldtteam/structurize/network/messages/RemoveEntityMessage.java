package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.RemoveEntityOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
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
    private final ResourceLocation entityName;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveEntityMessage(final FriendlyByteBuf buf)
    {
        this.from = buf.readBlockPos();
        this.to = buf.readBlockPos();
        this.entityName = buf.readResourceLocation();
    }

    /**
     * Create a message to remove an entity from the world.
     *
     * @param pos1       start coordinate.
     * @param pos2       end coordinate.
     * @param entityName the entity to remove.
     */
    public RemoveEntityMessage(final BlockPos pos1, final BlockPos pos2, final ResourceLocation entityName)
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
        buf.writeResourceLocation(entityName);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (!ctxIn.getSender().isCreative())
        {
            return;
        }

        final EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityName);
        if (type != null)
        {
            Manager.addToQueue(new RemoveEntityOperation(ctxIn.getSender(), from, to, type));
        }
    }
}
