package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintRenderer.TransparencyHack;
import com.ldtteam.structurize.items.ItemTagTool.TagData;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.WorldRenderMacros;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

import java.util.List;
import java.util.Map;

/**
 * For rendering into world.
 */
public class WorldRenderContext extends WorldRenderMacros
{
    static final WorldRenderContext INSTANCE = new WorldRenderContext();

    @Override
    protected void renderWithinContext(final Stage stage)
    {
        final double alpha = Structurize.getConfig().getClient().rendererTransparency.get();
        final boolean isAlphaApplied = alpha > 0 && alpha < TransparencyHack.THRESHOLD;

        final Stage when = isAlphaApplied ? Stage.AFTER_TRANSLUCENT_BLOCKS : Stage.AFTER_BLOCK_ENTITIES;
        // otherwise even worse sorting issues arise
        if (stage == when)
        {
            renderBlueprints();
        }

        if (stage == WorldRenderMacros.STAGE_FOR_LINES)
        {
            renderBoxes();
            renderTagTool();
        }
    }

    private void renderBlueprints()
    {
        for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
        {
            final Blueprint blueprint = previewData.getBlueprint();

            if (blueprint != null)
            {
                mc.getProfiler().push("struct_render");

                renderBlueprint(previewData, previewData.getPos());

                mc.getProfiler().pop();
            }
        }
    }

    private void renderBoxes()
    {
        for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
        {
            final Blueprint blueprint = previewData.getBlueprint();

            if (blueprint != null)
            {
                final BlockPos anchor = blueprint.getPrimaryBlockOffset();

                mc.getProfiler().push("struct_render");
                pushPoseCameraToPos(previewData.getPos().subtract(anchor));

                renderWhiteLineBox(BlockPos.ZERO,
                    new BlockPos(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                    DEFAULT_LINE_WIDTH);
                renderRedGlintLineBox(anchor, anchor, DEFAULT_LINE_WIDTH);

                popPose();
                mc.getProfiler().pop();
            }
        }

        for (final BoxPreviewData previewData : RenderingCache.getBoxesToRender())
        {
            final BlockPos root = previewData.pos1();

            mc.getProfiler().push("struct_box");
            pushPoseCameraToPos(root);

            // Used to render a red box around a scan's Primary offset (primary block)
            renderWhiteLineBox(BlockPos.ZERO, previewData.pos2().subtract(root), DEFAULT_LINE_WIDTH);
            previewData.anchor().map(pos -> pos.subtract(root)).ifPresent(pos -> renderRedGlintLineBox(pos, pos, DEFAULT_LINE_WIDTH));

            popPose();
            mc.getProfiler().pop();
        }
    }

    private void renderTagTool()
    {
        final Player player = mc.player;
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        final TagData tags = TagData.readFromItemStack(itemStack);
        if (tags.anchorPos().isPresent())
        {
            final BlockPos tagAnchor = tags.anchorPos().get();
            final BlockEntity te = player.level().getBlockEntity(tagAnchor);

            mc.getProfiler().push("struct_tags");
            pushPoseCameraToPos(tagAnchor);

            if (te instanceof final IBlueprintDataProviderBE blueprintProvider)
            {
                final Map<BlockPos, List<String>> tagPosList = blueprintProvider.getWorldTagPosMap();

                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    final BlockPos pos = entry.getKey().subtract(tagAnchor);
                    renderWhiteLineBox(pos, pos, DEFAULT_LINE_WIDTH);
                    renderDebugText(pos, entry.getKey(), entry.getValue(), true, 3);
                }
            }
            renderRedGlintLineBox(BlockPos.ZERO, BlockPos.ZERO, DEFAULT_LINE_WIDTH);

            popPose();
            mc.getProfiler().pop();
        }
    }
}
