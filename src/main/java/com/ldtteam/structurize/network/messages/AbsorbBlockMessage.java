package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemTagSubstitution;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Sent client to server to request that the currently held item should "absorb" a new replacement block.
 */
public class AbsorbBlockMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "absorb_block", AbsorbBlockMessage::new);
    private final BlockPos pos;
    private final ItemStack stack;

    /**
     * Construct
     * @param pos the location of the block to absorb (in the sender's level)
     * @param stack the "picked item" stack produced by that block
     */
    public AbsorbBlockMessage(@NotNull final BlockPos pos, @NotNull final ItemStack stack)
    {
        super(TYPE);
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Deserialize
     * @param buf the network buffer
     */
    protected AbsorbBlockMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.pos = buf.readBlockPos();
        this.stack = buf.readItem();
    }

    /**
     * Serialize
     * @param buf network data byte buffer
     */
    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(this.pos);
        buf.writeItem(this.stack);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final ItemStack current = player.getInventory().getSelected();

        if (current.getItem() instanceof ItemTagSubstitution anchor)
        {
            anchor.onAbsorbBlock(player, current, this.pos, this.stack);
        }
    }
}
