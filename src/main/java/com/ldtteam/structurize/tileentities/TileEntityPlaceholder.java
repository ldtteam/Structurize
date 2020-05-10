package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This Class is about the placeholder tileEntity.
 */
public class TileEntityPlaceholder extends TileEntity
{
    public static final String TAG_NBT_LIST = "struct_tag";

    /**
     * The block to render.
     */
    private ItemStack block = new ItemStack(ModBlocks.placeholderBlock, 1);

    /**
     * List of custom tags
     */
    private Set<String> tagList = new HashSet<>();

    public TileEntityPlaceholder()
    {
        super(StructurizeTileEntities.PLACERHOLDER_BLOCK);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);

        if (compound.contains(TAG_NBT_LIST))
        {
            ListNBT nbtList = compound.getList(TAG_NBT_LIST, Constants.NBT.TAG_COMPOUND);

            for (final INBT nbt : nbtList)
            {
                tagList.add(((CompoundNBT) nbt).getString(TAG_NBT_LIST));
            }
        }

        this.block = ItemStack.read(compound.getCompound("displayblock"));
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);

        ListNBT nbtList = new ListNBT();

        for (final String name : tagList)
        {
            CompoundNBT stringCompound = new CompoundNBT();
            stringCompound.putString(TAG_NBT_LIST, name);
            nbtList.add(stringCompound);
        }

        compound.put(TAG_NBT_LIST, nbtList);
        compound.put("displayblock", this.block.write(new CompoundNBT()));
        return compound;
    }

    @Override
    public void handleUpdateTag(final CompoundNBT tag)
    {
        this.read(tag);
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt)
    {
        this.read(pkt.getNbtCompound());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    /**
     * Get the stack of this placerholder.
     * @return the stack.
     */
    public ItemStack getStack()
    {
        return this.block;
    }

    /**
     * Set the stack to the placerholder.
     * @param to the stack to set.
     */
    public void setStack(final ItemStack to)
    {
        this.block = to;
        this.markDirty();
    }

    /**
     * Checks if the given tag is present
     *
     * @param tag tag to check
     * @return true if tag exists
     */
    public boolean hasTag(final String tag)
    {
        return tagList.contains(tag);
    }

    /**
     * Sets the list of tags
     *
     * @param tagList list to set
     */
    public void setTagList(final List<String> tagList)
    {
        this.tagList = new HashSet<>(tagList);
    }

    /**
     * Get the existing list of tags.
     *
     * @return taglist
     */
    public List<String> getTagList()
    {
        return new ArrayList<>(tagList);
    }
}
