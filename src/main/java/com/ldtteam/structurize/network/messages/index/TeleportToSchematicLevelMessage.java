package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.api.constants.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sent from the client to the server to teleport the player to a schematic level's bounding box.
 */
public class TeleportToSchematicLevelMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "teleport_to_schematic_level", TeleportToSchematicLevelMessage::new);

    private final BlockPos pos1;
    private final BlockPos pos2;

    public TeleportToSchematicLevelMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2)
    {
        super(TYPE);
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    protected TeleportToSchematicLevelMessage(@NotNull final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.pos1 = buf.readBlockPos();
        this.pos2 = buf.readBlockPos();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos1);
        buf.writeBlockPos(pos2);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!Structurize.getConfig().getServer().isSchematicBuildServer.get())
        {
            return;
        }

        // Land just above the top face of the bounding box — this area is always empty.
        final int midX = (pos1.getX() + pos2.getX()) / 2;
        final int maxY = Math.max(pos1.getY(), pos2.getY());
        final int midZ = (pos1.getZ() + pos2.getZ()) / 2;
        BlockPos target = new BlockPos(midX, maxY + 1, midZ);

        final ServerLevel serverLevel = player.serverLevel();

        @Nullable final BlockPos safeTarget = BlockPosUtil.findSafeTeleportPos(serverLevel, target, false);
        if (safeTarget != null)
        {
            target = safeTarget;
        }

        player.teleportTo(serverLevel, target.getX() + 0.5, target.getY(), target.getZ() + 0.5, player.getYRot(), player.getXRot());
    }
}
