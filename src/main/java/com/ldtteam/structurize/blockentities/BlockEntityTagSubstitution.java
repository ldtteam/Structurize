package com.ldtteam.structurize.blockentities;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The block entity for BlockTagSubstitution
 */
public class BlockEntityTagSubstitution extends BlockEntity implements IBlueprintDataProviderBE
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

    /**
     * Structure pack name.
     */
    private String packName;

    /**
     * Structure pack path.
     */
    private String inPackPath;

    /**
     * Replacement block.
     */
    private ReplacementBlock replacement = new ReplacementBlock();

    public BlockEntityTagSubstitution(final BlockPos pos, final BlockState state)
    {
        super( ModBlockEntities.TAG_SUBSTITUTION.get(), pos, state);
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

    /**
     * @return the replacement block details
     */
    @NotNull
    public ReplacementBlock getReplacement()
    {
        return this.replacement;
    }

    @Override
    public void loadAdditional( @NotNull final CompoundTag compound, final HolderLookup.Provider provider)
    {
        super.loadAdditional(compound, provider);
        IBlueprintDataProviderBE.super.readSchematicDataFromNBT(compound);
        this.replacement = new ReplacementBlock(compound, provider);
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound, final HolderLookup.Provider provider)
    {
        super.saveAdditional(compound, provider);
        writeSchematicDataToNBT(compound);
        this.replacement.write(compound, provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setPackName(final String packName)
    {
        this.packName = packName;
    }

    @Override
    public void setBlueprintPath(final String inPackPath)
    {
        this.inPackPath = inPackPath;
    }

    @Override
    public String getPackName()
    {
        return packName;
    }

    @Override
    public String getBlueprintPath()
    {
        return inPackPath;
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    /**
     * Storage for information about the replacement block, if any.
     */
    public static class ReplacementBlock
    {
        private static final String TAG_REPLACEMENT = "replacement";

        private final BlockState blockstate;
        private final CompoundTag blockentitytag;
        private final ItemStack itemstack;

        @Nullable private BlockEntity cachedBlockentity;

        /**
         * Construct
         * @param blockstate the block state
         * @param blockentity the block entity, if any
         * @param itemstack the item stack
         */
        @Deprecated(forRemoval = true, since = "1.21")
        public ReplacementBlock(@NotNull final BlockState blockstate,
                                @Nullable final BlockEntity blockentity,
                                @NotNull final ItemStack itemstack)
        {
            throw new UnsupportedOperationException("Use compound tag ctor");
        }

        /**
         * Construct
         * @param blockstate the block state
         * @param blockentity the block entity tag, if any
         * @param itemstack the item stack
         */
        public ReplacementBlock(@NotNull final BlockState blockstate,
                                @Nullable final CompoundTag blockentity,
                                @NotNull final ItemStack itemstack)
        {
            this.blockstate = blockstate;
            this.blockentitytag = blockentity == null ? new CompoundTag() : blockentity.copy();
            this.itemstack = itemstack;
        }

        /**
         * Construct from tag
         * @param tag the tag to load
         */
        public ReplacementBlock(@NotNull CompoundTag tag, final HolderLookup.Provider provider)
        {
            final CompoundTag replacement = tag.getCompound(TAG_REPLACEMENT);
            this.blockstate = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), replacement.getCompound("b"));
            this.blockentitytag = replacement.getCompound("e");
            this.itemstack = replacement.contains("i") ? ItemStack.parseOptional(provider, replacement.getCompound("i")) : ItemStack.EMPTY;
        }

        /**
         * Empty instance
         */
        public ReplacementBlock()
        {
            this.blockstate = Blocks.AIR.defaultBlockState();
            this.blockentitytag = new CompoundTag();
            this.itemstack = ItemStack.EMPTY;
        }

        /**
         * @return true if there is no replacement block set (assume air)
         */
        public boolean isEmpty()
        {
            return this.blockstate.isAir();
        }

        /**
         * @return the block state
         */
        @NotNull
        public BlockState getBlockState()
        {
            return this.blockstate;
        }

        /**
         * @return the block entity tag
         */
        @NotNull
        public CompoundTag getBlockEntityTag()
        {
            return this.blockentitytag;
        }

        /**
         * @return the item stack
         */
        @NotNull
        public ItemStack getItemStack()
        {
            return this.itemstack;
        }

        /**
         * Creates and loads (once) the replacement block entity, or returns the preloaded one.
         * @param pos the blockpos to use (ignored if already loaded)
         * @return the new or cached entity, or null if there isn't one
         */
        @Nullable
        public BlockEntity getBlockEntity(final BlockPos pos, final HolderLookup.Provider provider)
        {
            if (this.cachedBlockentity == null)
            {
                this.cachedBlockentity = createBlockEntity(pos, provider);
            }
            return this.cachedBlockentity;
        }

        /**
         * Always creates and loads a new replacement block entity, if needed.
         * @param pos the blockpos to use
         * @return the new entity, or null if there isn't one
         */
        @Nullable
        public BlockEntity createBlockEntity(final BlockPos pos, final HolderLookup.Provider provider)
        {
            return this.blockentitytag.isEmpty()
                    ? null
                    : BlockEntity.loadStatic(pos, this.blockstate, this.blockentitytag, provider);
        }

        /**
         * Serialisation
         * @param tag the target tag
         * @return the target tag, for convenience
         */
        @NotNull
        public CompoundTag write(@NotNull CompoundTag tag, final HolderLookup.Provider provider)
        {
            if (isEmpty())
            {
                tag.remove(TAG_REPLACEMENT);
            }
            else
            {
                final CompoundTag replacement = new CompoundTag();
                replacement.put("b", NbtUtils.writeBlockState(this.blockstate));
                if (this.blockentitytag.isEmpty())
                {
                    replacement.remove("e");
                }
                else
                {
                    replacement.put("e", this.blockentitytag);
                }
                replacement.put("i", this.itemstack.save(provider));

                tag.put(TAG_REPLACEMENT, replacement);
            }
            return tag;
        }

        /**
         * Creates a new single-block {@link Blueprint} for the replacement block.
         * @return the blueprint
         */
        @NotNull
        public Blueprint createBlueprint(final HolderLookup.Provider provider)
        {
            final Blueprint blueprint = new Blueprint((short) 1, (short) 1, (short) 1, provider);
            blueprint.addBlockState(BlockPos.ZERO, getBlockState());
            blueprint.getTileEntities()[0][0][0] = getBlockEntityTag().isEmpty() ? null : getBlockEntityTag().copy();
            return blueprint;
        }

        /**
         * Rotates and mirrors the replacement data, in response to a blueprint containing this replacement block
         * being rotated or mirrored.
         *
         * @param pos the world location for the replacement block
         * @param rotationMirror the relative rotation/mirror
         * @param level the (actual) world
         * @return the new replacement data
         */
        public ReplacementBlock rotateWithMirror(final BlockPos pos, final RotationMirror rotationMirror, final Level level)
        {
            final Blueprint blueprint = createBlueprint(level.registryAccess());
            blueprint.setRotationMirrorRelative(rotationMirror, level);

            final BlockState newBlockState = blueprint.getBlockState(BlockPos.ZERO);
            final CompoundTag newBlockData = blueprint.getTileEntityData(pos, BlockPos.ZERO);
            return new ReplacementBlock(newBlockState, newBlockData, this.getItemStack());
        }
    }
}
