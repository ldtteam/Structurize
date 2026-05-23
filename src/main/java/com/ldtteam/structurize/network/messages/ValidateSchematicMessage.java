package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Sent from the client to the server to request re-validation of a specific schematic within a pack.
 *
 * <p>The server runs {@link PackManager#validateSchematic} for all matching schematics (filtered
 * by path, name, and optionally level) and broadcasts the updated pack state to all players via a
 * {@link SyncPackManagerMessage}.
 *
 * <p>When {@code level} is {@code null} the validation covers every level of the schematic at once.
 * When a specific level is provided only that single-level entry is validated — this is intended
 * for use by the per-level inspector screen that will be added later.
 */
public class ValidateSchematicMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "validate_schematic", ValidateSchematicMessage::new);

    private final String packId;
    private final String schematicPath;
    private final String schematicName;
    /** {@code null} means "all levels". */
    @Nullable
    private final Integer level;

    /**
     * Validates all levels of the given schematic group within the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     */
    public ValidateSchematicMessage(final String packId, final String schematicPath, final String schematicName)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = null;
    }

    /**
     * Validates a single level of the given schematic within the pack.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     * @param level         the specific level to validate (1-based), or {@code null} for all levels
     */
    public ValidateSchematicMessage(final String packId, final String schematicPath, final String schematicName, final @Nullable Integer level)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = level;
    }

    protected ValidateSchematicMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
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
        PackManager.validateSchematic(packId, schematicPath, schematicName, level, player.serverLevel());
    }
}