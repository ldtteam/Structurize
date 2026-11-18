package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class SyncPackManagerMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "sync_pack_manager", SyncPackManagerMessage::new);

    private final Map<String, Pack> packs;

    public SyncPackManagerMessage(final Map<String, Pack> packs)
    {
        super(TYPE);
        this.packs = packs;
    }

    protected SyncPackManagerMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.packs = Pack.MAP_STREAM_CODEC.decode(buf);
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        Pack.MAP_STREAM_CODEC.encode(buf, packs);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        PackManager.onClientSync(packs);
    }
}
