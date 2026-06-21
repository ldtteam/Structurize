package com.ldtteam.structurize.network.messages.index;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.AreaOperation;
import com.ldtteam.structurize.operations.PlaceStructureOperation;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Sent from the client to the server when the player confirms schematic relocation.
 *
 * <p>The server recreates the blueprint in memory from the schematic's stored bounding box, places
 * it at the requested position, and updates the {@link PackSchematic} pos1/pos2 to match the new
 * placement bounding box.
 */
public class RelocateSchematicLevelMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "relocate_schematic_level", RelocateSchematicLevelMessage::new);

    private final String         packId;
    private final String         schematicPath;
    private final String         schematicName;
    private final int            level;
    private final BlockPos       anchorPos;
    private final RotationMirror rotationMirror;

    /**
     * @param packId         the pack identifier
     * @param schematicPath  the relative folder path of the schematic (may be empty)
     * @param schematicName  the schematic file name (without extension)
     * @param level          the specific level to relocate (1-based)
     * @param anchorPos      the world position where the blueprint anchor should be placed
     * @param rotationMirror the rotation and mirror to apply
     */
    public RelocateSchematicLevelMessage(
        final String packId,
        final String schematicPath,
        final String schematicName,
        final int level,
        final BlockPos anchorPos,
        final RotationMirror rotationMirror)
    {
        super(TYPE);
        this.packId = packId;
        this.schematicPath = schematicPath;
        this.schematicName = schematicName;
        this.level = level;
        this.anchorPos = anchorPos;
        this.rotationMirror = rotationMirror;
    }

    protected RelocateSchematicLevelMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        this.packId = buf.readUtf();
        this.schematicPath = buf.readUtf();
        this.schematicName = buf.readUtf();
        this.level = buf.readInt();
        this.anchorPos = buf.readBlockPos();
        this.rotationMirror = RotationMirror.values()[buf.readByte()];
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeUtf(packId);
        buf.writeUtf(schematicPath);
        buf.writeUtf(schematicName);
        buf.writeInt(level);
        buf.writeBlockPos(anchorPos);
        buf.writeByte(rotationMirror.ordinal());
    }

    @Override
    protected void onExecute(final IPayloadContext context, final ServerPlayer player)
    {
        if (!Structurize.getConfig().getServer().isSchematicBuildServer.get())
        {
            return;
        }

        final Pack pack = PackManager.getPack(packId);
        if (pack == null)
        {
            return;
        }

        final PackSchematic schematic =
            pack.schematics().stream().filter(s -> s.path().equals(schematicPath) && s.name().equals(schematicName) && s.level() == level).findFirst().orElse(null);

        if (schematic == null)
        {
            return;
        }

        // Build the blueprint in memory from the existing bounding box.
        final Blueprint blueprint = BlueprintUtil.createBlueprint(schematic, player.serverLevel());
        blueprint.setRotationMirror(rotationMirror, player.serverLevel());

        final BlockPos oldMin = schematic.pos1();
        final BlockPos oldMax = schematic.pos2();

        // Derive the new bounding box before placement (sizes are post-rotation at this point).
        final CreativeStructureHandler handler = getCreativeStructureHandler(player, blueprint, oldMin, oldMax);
        Manager.addToQueue(new PlaceStructureOperation(new StructurePlacer(handler), player));
    }

    @NotNull
    private CreativeStructureHandler getCreativeStructureHandler(final ServerPlayer player, final Blueprint blueprint, final BlockPos oldMin, final BlockPos oldMax)
    {
        final BlockPos primaryOffset = blueprint.getPrimaryBlockOffset();
        final BlockPos newMin = anchorPos.subtract(primaryOffset);
        final BlockPos newMax = newMin.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1);

        // Place the blueprint blocks and update the pack data only once placement completes.
        return new CreativeStructureHandler(player.serverLevel(), anchorPos, blueprint, rotationMirror, false)
        {
            @Override
            public void triggerSuccess(final BlockPos pos, final List<ItemStack> requiredRes, final boolean placement)
            {
                // Blueprint has no pack backing — skip the IBlueprintDataProviderBE path that
                // would NPE on a null pack name.
            }

            @Override
            public void onCompletion()
            {
                PackManager.relocateSchematic(packId, schematicPath, schematicName, level, newMin, newMax);
                Manager.addToQueue(new AreaOperation(Component.empty(), player, oldMin, oldMax)
                {
                    @Override
                    protected void apply(final ServerLevel serverLevel, final BlockPos position)
                    {
                        serverLevel.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
                    }
                });
            }
        };
    }
}
