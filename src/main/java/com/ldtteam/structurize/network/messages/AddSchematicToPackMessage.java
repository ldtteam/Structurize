// TODO: Remove before publication — debug hook only
package com.ldtteam.structurize.network.messages;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.models.PackSchematicValidationState;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.ScanToolData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from the client to the server to add a scanned schematic to an existing pack.
 *
 * <p>The full schematic path (e.g. {@code "path/name2"}) is parsed server-side:
 * <ul>
 *     <li>Everything before the last {@code /} is the relative folder path (empty for root-level).</li>
 *     <li>Trailing digits on the file name encode the level (e.g. {@code "name2"} → level 2); no trailing digit means level 1.</li>
 *     <li>The anchor position is taken from {@link BoxPreviewData#anchor()} if present.</li>
 * </ul>
 */
public class AddSchematicToPackMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "add_schematic_to_pack", AddSchematicToPackMessage::new);

    private final ScanToolData.Slot slot;
    private final String packId;

    public AddSchematicToPackMessage(final String packId, final ScanToolData.Slot slot)
    {
        super(TYPE);
        this.packId = packId;
        this.slot = slot;
    }

    protected AddSchematicToPackMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.packId = buf.readUtf();
        this.slot = ScanToolData.Slot.STREAM_CODEC.decode(buf);
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(packId);
        ScanToolData.Slot.STREAM_CODEC.encode(buf, slot);
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        final String fullName = slot.name();
        final BoxPreviewData box = slot.box();

        final int lastSlash = fullName.lastIndexOf('/');
        final String schematicPath = lastSlash >= 0 ? fullName.substring(0, lastSlash) : "";
        final String fileNamePart = lastSlash >= 0 ? fullName.substring(lastSlash + 1) : fullName;

        int trailingDigitStart = fileNamePart.length();
        while (trailingDigitStart > 0 && Character.isDigit(fileNamePart.charAt(trailingDigitStart - 1)))
        {
            trailingDigitStart--;
        }

        final int level;
        final String schematicName;
        if (trailingDigitStart < fileNamePart.length())
        {
            level = Integer.parseInt(fileNamePart.substring(trailingDigitStart));
            schematicName = fileNamePart.substring(0, trailingDigitStart);
        }
        else
        {
            level = 1;
            schematicName = fileNamePart;
        }

        final PackSchematic schematic = new PackSchematic(
            schematicPath,
            schematicName,
            level,
            box.pos1(),
            box.pos2(),
            box.anchor(),
            new PackSchematicValidationState());
        PackManager.addSchematic(packId, schematic);
        PackManager.validateSchematic(packId, schematicPath, schematicName, level, player.serverLevel());
    }
}