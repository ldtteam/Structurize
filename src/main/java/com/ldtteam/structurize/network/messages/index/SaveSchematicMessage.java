package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Sent from the client to the server to save (validate and scan) a schematic within a pack.
 *
 * <p>The server creates a blueprint from the schematic's stored bounding box, validates it against
 * the pack type's requirements, and — if no blocking errors are found — sends the resulting
 * blueprint back to the requesting player via {@link SaveScanMessage}. The stored validation state
 * is updated in either case.
 *
 * <p>When {@code level} is {@code null} all levels of the schematic group are processed. When a
 * specific level is provided, only that single entry is saved.
 */
public class SaveSchematicMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "save_schematic", SaveSchematicMessage::new);

    private final String  packId;
    private final String  schematicPath;
    private final String  schematicName;
    @Nullable
    private final Integer level;

    /**
     * Saves all levels of the given schematic group within the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     */
    public SaveSchematicMessage(final String packId, final String schematicPath, final String schematicName)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = null;
    }

    /**
     * Saves a single level of the given schematic within the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     * @param level         the specific level to save (1-based)
     */
    public SaveSchematicMessage(final String packId, final String schematicPath, final String schematicName, final int level)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = level;
    }

    protected SaveSchematicMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
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

        PackManager.saveSchematic(packId, schematicPath, schematicName, level, player.serverLevel(), player);
    }
}
