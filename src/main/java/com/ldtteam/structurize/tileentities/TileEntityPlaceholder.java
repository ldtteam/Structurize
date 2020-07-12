package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This Class is about the placeholder tileEntity.
 */
public class TileEntityPlaceholder extends TileEntity implements IBlueprintDataProvider
{
    public static final String TAG_SCHEMATIC_DATA  = "schematic_data";
    public static final String TAG_CONTAINED_BLOCK = "displayblock";

    /**
     * The block to render.
     */
    private ItemStack block = new ItemStack(ModBlocks.placeholderBlock, 1);

    public TileEntityPlaceholder()
    {
        super(StructurizeTileEntities.PLACERHOLDER_BLOCK);
    }

    @Override
    public void read(final BlockState blockState, final CompoundNBT compound)
    {
        super.read(blockState, compound);
        readSchematicDataFromNBT(compound);

        this.block = ItemStack.read(compound.getCompound(TAG_CONTAINED_BLOCK));
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);
        writeSchematicDataToNBT(compound);
        compound.put(TAG_CONTAINED_BLOCK, this.block.write(new CompoundNBT()));
        return compound;
    }

    @Override
    public void handleUpdateTag(final BlockState blockState, final CompoundNBT tag)
    {
        this.read(blockState, tag);
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt)
    {
        this.read(Structurize.proxy.getBlockStateFromWorld(pkt.getPos()), pkt.getNbtCompound());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbt = new CompoundNBT();
        this.write(nbt);

        // the number here is generally ignored for non-vanilla TileEntities, 0 is safest
        return new SUpdateTileEntityPacket(this.getPos(), 0, nbt);
    }

    /**
     * Get the stack of this placerholder.
     *
     * @return the stack.
     */
    public ItemStack getStack()
    {
        return this.block;
    }

    /**
     * Set the stack to the placerholder.
     *
     * @param to the stack to set.
     */
    public void setStack(final ItemStack to)
    {
        this.block = to;
        this.markDirty();
    }

    private String schematicName = "";

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

    private Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

    @Override
    public Map<BlockPos, List<String>> getPositionedTags()
    {
        return tagPosMap;
    }

    @Override
    public void setPositionedTags(final Map<BlockPos, List<String>> positionedTags)
    {
        tagPosMap = positionedTags;
    }

    private BlockPos corner1 = BlockPos.ZERO;
    private BlockPos corner2 = BlockPos.ZERO;

    @Override
    public Tuple<BlockPos, BlockPos> getCornerPositions()
    {
        if (corner1 == BlockPos.ZERO || corner2 == BlockPos.ZERO)
        {
            return new Tuple<>(pos, pos);
        }

        return new Tuple<>(corner1, corner2);
    }

    @Override
    public void setCorners(final BlockPos pos1, final BlockPos pos2)
    {
        corner1 = pos1;
        corner2 = pos2;
    }

    @Override
    public BlockPos getTilePos()
    {
        return pos;
    }
}
