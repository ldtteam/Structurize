package com.ldtteam.structurize.update;

import com.google.common.collect.*;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlockComponent;
import com.ldtteam.domumornamentum.block.IModBlocks;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

public class DomumOrnamentumUpdateHandler
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Block CACTUS_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:cactus_extra")));
    private static final Block THIN_PAPER_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:white_paper_extra")));


    private static final Map<String, Block> MATERIAL_TO_BLOCK_MAP = ImmutableMap.<String, Block>builder()
                                                                           .put("oak", Blocks.OAK_PLANKS)
                                                                           .put("spruce", Blocks.SPRUCE_PLANKS)
                                                                           .put("birch", Blocks.BIRCH_PLANKS)
                                                                           .put("jungle", Blocks.JUNGLE_PLANKS)
                                                                           .put("acacia", Blocks.ACACIA_PLANKS)
                                                                           .put("dark_oak", Blocks.DARK_OAK_PLANKS)
                                                                           .put("warped", Blocks.WARPED_PLANKS)
                                                                           .put("crimson", Blocks.CRIMSON_PLANKS)
                                                                           .put("cactus", CACTUS_BLOCK)
                                                                           .put("thatched", Blocks.WHEAT)
                                                                           .build();

    private DomumOrnamentumUpdateHandler()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DomumOrnamentumUpdateHandler. This is a utility class");
    }

    public static void updateChunkTag(final CompoundTag chunkTag, final ChunkPos chunkPos) {
        final CompoundTag levelTag = chunkTag.getCompound("Level");
        final ListTag sectionsTag = levelTag.getList("Sections", Constants.NBT.TAG_COMPOUND);
        sectionsTag.forEach(sectionTag -> updateSectionTag(levelTag, (CompoundTag) sectionTag, chunkPos));
    }

    private static void updateSectionTag(final CompoundTag chunkTag, final CompoundTag sectionTag, final ChunkPos chunkPos) {
        if (!sectionTag.contains("Palette") || !sectionTag.contains("BlockStates"))
            return;

        final ListTag paletteTag = sectionTag.getList("Palette", Constants.NBT.TAG_COMPOUND);
        final long[] blockStateIds = sectionTag.getLongArray("BlockStates");

        final int bitCount = Math.max(4, Mth.ceillog2(paletteTag.size()));

        final BitStorage bitStorage = new BitStorage(bitCount, 4096, blockStateIds);
        final Multimap<Integer, Integer> paletteEntryToBitStoragePositionMap = HashMultimap.create();

        for (int i = 0; i < 4096; i++)
        {
            paletteEntryToBitStoragePositionMap.put(bitStorage.get(i), i);
        }

        int yOffset = sectionTag.getByte("Y");
        final BlockPos chunkStart = new BlockPos(chunkPos.getMinBlockX(), yOffset, chunkPos.getMinBlockZ());

        ListTag blockEntityTags = chunkTag.getList("TileEntities", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < paletteTag.size(); i++)
        {
            final CompoundTag paletteEntryTag = paletteTag.getCompound(i);
            if (!paletteEntryTag.contains("Name"))
                continue;

            final String name = paletteEntryTag.getString("Name");
            if (!name.startsWith("%s:".formatted(MOD_ID)))
                continue;

            final Optional<Tuple<BlockState, BlockEntity>> replacementData = createBlockReplacementData(paletteEntryTag);
            if (replacementData.isEmpty())
                continue;

            final BlockState blockState = replacementData.get().getA();
            final CompoundTag blockStateTag = NbtUtils.writeBlockState(blockState);
            final List<String> paletteEntryTagKeys = paletteEntryTag.getAllKeys().stream().toList();
            paletteEntryTagKeys.forEach(paletteEntryTag::remove);
            blockStateTag.getAllKeys().forEach(key -> paletteEntryTag.put(key, Objects.requireNonNull(blockStateTag.get(key))));

            final CompoundTag workingEntityNbt = replacementData.get().getB().serializeNBT();
            paletteEntryToBitStoragePositionMap.get(i).forEach(bitStorageIndex -> {
                final int inChunkX = bitStorageIndex & 15;
                final int inChunkY = (bitStorageIndex >> 8) & 15;
                final int inChunkZ = (bitStorageIndex >> 4) & 15;

                final BlockPos targetPos = chunkStart.offset(inChunkX, inChunkY, inChunkZ);
                final CompoundTag targetTag = workingEntityNbt.copy();
                targetTag.putInt("x", targetPos.getX());
                targetTag.putInt("y", targetPos.getY());
                targetTag.putInt("z", targetPos.getZ());

                blockEntityTags.add(targetTag);
            });
        }
    }

    private static Optional<Tuple<BlockState, BlockEntity>> createBlockReplacementData(final CompoundTag paletteEntryTag) {
        final String name = paletteEntryTag.getString("Name");

        if (name.endsWith("_blockpaperwall"))
            return createBlockpaperWallReplacementData(name, paletteEntryTag.getCompound("Properties"));

        return Optional.empty();
    }

    private static Optional<Tuple<BlockState, BlockEntity>> createBlockpaperWallReplacementData(final String blockName, final CompoundTag propertiesTag) {
        final String materialName = blockName.replace("structurize:", "").replace("_blockpaperwall", "");

        final Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Blocks.AIR);

        if (replacementBlock == Blocks.AIR) {
            LOGGER.error("Could not find replacement block for material: %s to create a new paper wall. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block thinWallBlock = IModBlocks.getInstance().getPaperWall();
        final IMateriallyTexturedBlock mtThinWallBlock = (IMateriallyTexturedBlock) thinWallBlock;
        final EntityBlock ebThinWallBlock = (EntityBlock) thinWallBlock;

        final BlockEntity thinWallBlockEntity = Objects.requireNonNull(ebThinWallBlock.newBlockEntity(BlockPos.ZERO, thinWallBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtThinWallBlockEntity = (IMateriallyTexturedBlockEntity) thinWallBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtThinWallBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent frameComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent centerComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(frameComponent.getId(), replacementBlock)
            .put(centerComponent.getId(), THIN_PAPER_BLOCK)
            .build()
        );

        mtThinWallBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(thinWallBlock, propertiesTag),
            thinWallBlockEntity
          )
        );
    }

    private static Optional<Tuple<BlockState, BlockEntity>> createShingleReplacementData(final String blockName, final CompoundTag propertiesTag) {
        final String blockEntryName = blockName.replace("structurize:", "").replace("_shingle", "");
        final String woodName = blockEntryName.substring(blockEntryName.lastIndexOf("_"));
        final String coverName = blockEntryName.replace("_%s".formatted(woodName), "");

        final Block woodReplacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(woodName, Blocks.AIR);
    }

    private static BlockState buildBlockState(final Block block, final CompoundTag propertiesTag) {
        BlockState workingState = block.defaultBlockState();
        if (propertiesTag == null)
        {
            return workingState;
        }

        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();

        for(String s : propertiesTag.getAllKeys()) {
            Property<?> property = statedefinition.getProperty(s);
            if (property != null) {
                workingState = setBlockStatePropertyValueFromString(workingState, property, s, propertiesTag);
            }
        }

        return workingState;
    }

    private static <T extends Comparable<T>> BlockState setBlockStatePropertyValueFromString(BlockState blockState, Property<T> property, String propertyName, CompoundTag propertiesTag) {
        Optional<T> optional = property.getValue(propertiesTag.getString(propertyName));
        if (optional.isPresent()) {
            return blockState.setValue(property, optional.get());
        } else {
            LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", propertyName, propertiesTag.getString(propertyName), propertiesTag.toString());
            return blockState;
        }
    }
}
