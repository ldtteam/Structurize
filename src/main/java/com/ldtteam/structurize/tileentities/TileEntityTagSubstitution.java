package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The tile entity for BlockTagSubstitution
 */
public class TileEntityTagSubstitution extends TileEntity implements IBlueprintDataProvider
{
    /**
     * The schematic name of the block.
     */
    private String schematicName = "";

    /**
     * Corner positions of schematic, relative to te pos.
     */
    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    /**
     * Map of block positions relative to TE pos and string tags
     */
    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    public TileEntityTagSubstitution()
    {
        super(ModTileEntities.TAG_SUBSTITUTION);
    }

    @Override
    public String getSchematicName()
    {
        return schematicName;
    }

    @Override
    public void setSchematicName(final String name)
    {
        schematicName = name;
    }

    @Override
    public Map<BlockPos, List<String>> getPositionedTags()
    {
        return tagPosMap;
    }

    @Override
    public void setPositionedTags(final Map<BlockPos, List<String>> positionedTags)
    {
        tagPosMap = positionedTags;
        setChanged();
    }

    @Override
    public Tuple<BlockPos, BlockPos> getSchematicCorners()
    {
        if (corner1 == BlockPos.ZERO || corner2 == BlockPos.ZERO)
        {
            return new Tuple<>(worldPosition, worldPosition);
        }

        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setSchematicCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
    }

    @Override
    public BlockPos getTilePos()
    {
        return worldPosition;
    }

    @Override
    public void load(@NotNull final BlockState state, @NotNull final CompoundNBT compound)
    {
        super.load(state, compound);
        IBlueprintDataProvider.super.readSchematicDataFromNBT(compound);
    }

    @NotNull
    @Override
    public CompoundNBT save(@NotNull final CompoundNBT compound)
    {
        super.save(compound);
        writeSchematicDataToNBT(compound);
        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket packet)
    {
        final CompoundNBT compound = packet.getTag();
        this.load(getBlockState(), compound);
    }
}
