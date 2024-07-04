package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.items.ItemScanTool;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class ScanToolTeleportMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "scantool_teleport", ScanToolTeleportMessage::new);

    public ScanToolTeleportMessage()
    {
        super(TYPE);
    }

    protected ScanToolTeleportMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
    }

    @Override
    protected void toBytes(RegistryFriendlyByteBuf buf)
    {
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ItemScanTool tool)
        {
            tool.onTeleport(player, stack);
        }
    }
}
