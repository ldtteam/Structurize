package com.ldtteam.structurize.client.fakelevel;

import com.ldtteam.common.fakelevel.FakeLevel;
import com.ldtteam.common.fakelevel.IFakeLevelLightProvider;
import com.ldtteam.common.fakelevel.IFakeLevelLightProvider.ConfigBasedLightProvider;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Scoreboard;

/**
 * Exists to separate blueprint specific rendering from FakeLevel.
 */
public class BlueprintBlockAccess extends FakeLevel<Blueprint>
{
    public static final IFakeLevelLightProvider LIGHT_PROVIDER = new ConfigBasedLightProvider(Structurize.getConfig().getClient().rendererLightLevel);
    private static final Scoreboard SCOREBOARD = new Scoreboard();

    /**
     * Override blockstate for solid placeholders
     */
    private BlockState solidSubstitutionOverride = null;

    public BlueprintBlockAccess(final Blueprint blueprint)
    {
        super(blueprint, LIGHT_PROVIDER, Minecraft.getInstance().level, SCOREBOARD, true);
    }

    private static Level anyLevel()
    {
        final Minecraft mc = Minecraft.getInstance();
        return mc.hasSingleplayerServer() ? mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).level() : mc.level;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return prepareBlockStateForRendering(super.getBlockState(pos), pos);
    }

    public BlockState prepareBlockStateForRendering(final BlockState state, final BlockPos pos)
    {
        return prepareBlockStateForRendering(state, pos, null);
    }

    public BlockState prepareBlockStateForRendering(final BlockState state, final BlockPos pos, final BlueprintPreviewData previewData)
    {
        if (previewData == null ? Structurize.getConfig().getClient().renderPlaceholdersNice.get() : previewData.getRenderBlocksNice())
        {
            if (state.getBlock() == ModBlocks.blockSolidSubstitution.get())
            {
                if (solidSubstitutionOverride != null)
                {
                    return solidSubstitutionOverride;
                }
                return BlockUtils.getSubstitutionBlockAtWorld(anyLevel(), worldPos.offset(pos), levelSource.getRawBlockStateFunction().compose(b -> b.subtract(worldPos)));
            }
            else if (state.getBlock() == ModBlocks.blockFluidSubstitution.get())
            {
                return BlockUtils.getFluidForDimension(anyLevel());
            }
            else if (state.getBlock() == ModBlocks.blockSubstitution.get())
            {
                return Blocks.AIR.defaultBlockState();
            }
            else if (state.getBlock() == ModBlocks.blockTagSubstitution.get())
            {
                if (super.getBlockEntity(pos) instanceof final BlockEntityTagSubstitution tag)
                {
                    return tag.getReplacement().blockState();
                }
                return Blocks.AIR.defaultBlockState();
            }
        }

        return state;
    }

    /**
     * Set the solid placeholder blockstate override, only updates when the renderer is recalculated
     *
     * @return
     */
    public void setSolidSubstitutionOverride(final BlockState solidSubstitutionOverride)
    {
        this.solidSubstitutionOverride = solidSubstitutionOverride;
    }
}
