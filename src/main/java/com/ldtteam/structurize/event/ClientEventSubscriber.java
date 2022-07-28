package com.ldtteam.structurize.event;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;
import java.util.Map;

import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS;
import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS;

public class ClientEventSubscriber
{
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        if (event.getStage() != AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
        {
            return;
        }
        Settings.instance.startStructurizePass();
        OptifineCompat.getInstance().preBlueprintDraw();

        final PoseStack matrixStack = event.getPoseStack();
        final float partialTicks = event.getPartialTick();
        final MultiBufferSource.BufferSource bufferSource = WorldRenderMacros.getBufferSource();

        final Minecraft mc = Minecraft.getInstance();
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());

        final Blueprint blueprint = Settings.instance.getActiveStructure();

        if (blueprint != null)
        {
            mc.getProfiler().push("struct_render");

            final BlockPos pos = Settings.instance.getPosition();
            final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());

            BlueprintHandler.getInstance().draw(blueprint, pos, matrixStack, partialTicks);
            WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f);
            WorldRenderMacros.renderWhiteLineBox(bufferSource,
                matrixStack,
                posMinusOffset,
                posMinusOffset.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                0.02f);

            mc.getProfiler().pop();
        }

        final Tuple<BlockPos, BlockPos> box = Settings.instance.getBox();
        if (box != null)
        {
            mc.getProfiler().push("struct_box");

            // Used to render a red box around a scan's Primary offset (primary block)
            Settings.instance.getAnchorPos().ifPresent(pos -> WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f));
            WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, box.getA(), box.getB(), 0.02f);

            mc.getProfiler().pop();
        }

        final Player player = mc.player;
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool.get() && itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            mc.getProfiler().push("struct_tags");

            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
            final BlockEntity te = mc.player.level.getBlockEntity(tagAnchor);
            WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, tagAnchor, tagAnchor, 0.02f);

            if (te instanceof IBlueprintDataProvider)
            {
                final Map<BlockPos, List<String>> tagPosList = ((IBlueprintDataProvider) te).getWorldTagPosMap();

                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, entry.getKey(), entry.getKey(), 0.02f);
                    WorldRenderMacros.renderDebugText(entry.getKey(), entry.getValue(), matrixStack, true, 3, bufferSource);
                }
            }

            mc.getProfiler().pop();
        }

        bufferSource.endBatch();
        matrixStack.popPose();

        OptifineCompat.getInstance().postBlueprintDraw();
        Settings.instance.endStructurizePass();
    }

    /**
     * Used to catch the clientTickEvent.
     * Call renderer cache cleaning every 5 secs (100 ticks).
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    @SuppressWarnings("resource")
    public static void onClientTickEvent(final ClientTickEvent event)
    {
        if (event.phase != Phase.END)
        {
            return;
        }

        Minecraft.getInstance().getProfiler().push("structurize");

        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % (Constants.TICKS_SECOND * 5) == 0)
        {
            Minecraft.getInstance().getProfiler().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            Minecraft.getInstance().getProfiler().pop();
        }

        Minecraft.getInstance().getProfiler().pop();
    }
}
