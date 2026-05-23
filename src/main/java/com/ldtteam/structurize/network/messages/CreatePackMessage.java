package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.Registries;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from the client to the server to request the creation of a new schematic pack.
 * The server resolves the {@link PackType} holder from the registry and delegates to
 * {@link com.ldtteam.structurize.index.PackManager#addPack}.
 */
public class CreatePackMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "create_pack", CreatePackMessage::new);

    private final String name;

    private final ResourceKey<PackType> type;

    public CreatePackMessage(final String name, final ResourceKey<PackType> type)
    {
        super(TYPE);
        this.name = name;
        this.type = type;
    }

    protected CreatePackMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.name = buf.readUtf();
        this.type = buf.readResourceKey(Registries.SCHEMATIC_INDEX_PACK_TYPES);
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(name);
        buf.writeResourceKey(type);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final Holder.Reference<PackType> typeHolder = player.level().registryAccess().registryOrThrow(Registries.SCHEMATIC_INDEX_PACK_TYPES).getHolderOrThrow(type);
        PackManager.addPack(name, typeHolder, player.serverLevel());
    }
}
