package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Sent from the client to the server to remove schematic(s) from a pack's index.
 *
 * <p>When {@code level} is {@code null} all levels of the schematic group are removed. When a
 * specific level is provided, only that entry is removed. No schematic files on disk are deleted.
 */
public class DeleteSchematicMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "delete_schematic", DeleteSchematicMessage::new);

    private final String  packId;
    private final String  schematicPath;
    private final String  schematicName;
    @Nullable
    private final Integer level;

    /**
     * Removes all levels of the given schematic group from the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     */
    public DeleteSchematicMessage(final String packId, final String schematicPath, final String schematicName)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = null;
    }

    /**
     * Removes a single level of the given schematic from the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     * @param level         the specific level to remove (1-based)
     */
    public DeleteSchematicMessage(final String packId, final String schematicPath, final String schematicName, final int level)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = level;
    }

    protected DeleteSchematicMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.packId = buf.readUtf();
        this.schematicPath = buf.readUtf();
        this.schematicName = buf.readUtf();
        this.level = buf.readBoolean() ? buf.readInt() : null;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(packId);
        buf.writeUtf(schematicPath);
        buf.writeUtf(schematicName);
        buf.writeBoolean(level != null);
        if (level != null)
        {
            buf.writeInt(level);
        }
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!Structurize.getConfig().getServer().isSchematicBuildServer.get())
        {
            return;
        }

        PackManager.deleteSchematic(packId, schematicPath, schematicName, level);
    }
}