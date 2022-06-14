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
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

public class DomumOrnamentumUpdateHandler
{

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Block GREEN_CACTUS_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:green_cactus_extra")));
    private static final Block CACTUS_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:cactus_extra")));
    private static final Block THIN_PAPER_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:white_paper_extra")));
    private static final Block PAPER_BLOCK = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:paper_extra")));

    private static final Block BLACK_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:black_brick_extra")));
    private static final Block BLUE_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:blue_brick_extra")));
    private static final Block BLUE_SLATE = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:blue_cobblestone_extra")));
    private static final Block BROWN_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:brown_brick_extra")));
    private static final Block CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:brick_extra")));
    private static final Block CYAN_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:cyan_brick_extra")));
    private static final Block GRAY_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:gray_brick_extra")));
    private static final Block GREEN_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:green_brick_extra")));
    private static final Block GREEN_SLATE = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:green_cobblestone_extra")));
    private static final Block LIGHT_BLUE_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:light_blue_brick_extra")));
    private static final Block LIGHT_GRAY_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:light_gray_brick_extra")));
    private static final Block LIME_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:lime_brick_extra")));
    private static final Block MAGENTA_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:magenta_brick_extra")));
    private static final Block ORANGE_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:orange_brick_extra")));
    private static final Block PINK_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:pink_brick_extra")));
    private static final Block PURPLE_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:purple_brick_extra")));
    private static final Block PURPLE_SLATE = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:purple_cobblestone_extra")));
    private static final Block RED_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:red_brick_extra")));
    private static final Block SLATE = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:cobblestone_extra")));
    private static final Block THATCHED = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:wheat_extra")));
    private static final Block WHITE_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:white_brick_extra")));
    private static final Block YELLOW_CLAY = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:yellow_brick_extra")));
    private static final Block MOSS_SLATE = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:mossy_cobblestone_extra")));

    private static final Map<String, Block> MATERIAL_TO_BLOCK_MAP = ImmutableMap.<String, Block>builder()
                                                                      .put("cobble_stone", Blocks.COBBLESTONE)
                                                                      .put("oak", Blocks.OAK_PLANKS)
                                                                      .put("spruce", Blocks.SPRUCE_PLANKS)
                                                                      .put("birch", Blocks.BIRCH_PLANKS)
                                                                      .put("jungle", Blocks.JUNGLE_PLANKS)
                                                                      .put("acacia", Blocks.ACACIA_PLANKS)
                                                                      .put("dark_oak", Blocks.DARK_OAK_PLANKS)
                                                                      .put("warped", Blocks.WARPED_PLANKS)
                                                                      .put("crimson", Blocks.CRIMSON_PLANKS)
                                                                      .put("cactus", CACTUS_BLOCK)
                                                                      .put("blockcactusplank", CACTUS_BLOCK)
                                                                      .put("blockcactus", CACTUS_BLOCK)
                                                                      .put("paper", PAPER_BLOCK)
                                                                      .put("gilded_blackstone", Blocks.GILDED_BLACKSTONE)
                                                                      .put("blackstone", Blocks.BLACKSTONE)
                                                                      .put("black_clay", BLACK_CLAY)
                                                                      .put("blue_clay", BLUE_CLAY)
                                                                      .put("blue_slate", BLUE_SLATE)
                                                                      .put("brown_clay", BROWN_CLAY)
                                                                      .put("cyan_clay", CYAN_CLAY)
                                                                      .put("gray_clay", GRAY_CLAY)
                                                                      .put("green_clay", GREEN_CLAY)
                                                                      .put("green_slate", GREEN_SLATE)
                                                                      .put("light_blue_clay", LIGHT_BLUE_CLAY)
                                                                      .put("light_gray_clay", LIGHT_GRAY_CLAY)
                                                                      .put("lime_clay", LIME_CLAY)
                                                                      .put("magenta_clay", MAGENTA_CLAY)
                                                                      .put("orange_clay", ORANGE_CLAY)
                                                                      .put("pink_clay", PINK_CLAY)
                                                                      .put("purple_clay", PURPLE_CLAY)
                                                                      .put("purple_slate", PURPLE_SLATE)
                                                                      .put("red_clay", RED_CLAY)
                                                                      .put("thatched", THATCHED)
                                                                      .put("white_clay", WHITE_CLAY)
                                                                      .put("yellow_clay", YELLOW_CLAY)
                                                                      .put("slate", SLATE)
                                                                      .put("clay", CLAY)
                                                                      .put("moss_slate", MOSS_SLATE)
                                                                      .build();

    private DomumOrnamentumUpdateHandler()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DomumOrnamentumUpdateHandler. This is a utility class");
    }

    public static void updateChunkTag(final CompoundTag chunkTag, final ChunkPos chunkPos) {
        final CompoundTag levelTag = chunkTag.getCompound("Level");
        final ListTag sectionsTag = levelTag.getList("Sections", Tag.TAG_COMPOUND);
        sectionsTag.forEach(sectionTag -> updateSectionTag(levelTag, (CompoundTag) sectionTag, chunkPos));
    }

    private static void updateSectionTag(final CompoundTag chunkTag, final CompoundTag sectionTag, final ChunkPos chunkPos) {
        if (!sectionTag.contains("Palette") || !sectionTag.contains("BlockStates"))
            return;

        final ListTag paletteTag = sectionTag.getList("Palette", Tag.TAG_COMPOUND);
        final long[] blockStateIds = sectionTag.getLongArray("BlockStates");

        final int bitCount = Math.max(4, Mth.ceillog2(paletteTag.size()));

        final BitStorage bitStorage = new SimpleBitStorage(bitCount, 4096, blockStateIds);
        final Multimap<Integer, Integer> paletteEntryToBitStoragePositionMap = HashMultimap.create();

        for (int i = 0; i < 4096; i++)
        {
            paletteEntryToBitStoragePositionMap.put(bitStorage.get(i), i);
        }

        int yOffset = sectionTag.getByte("Y");
        final BlockPos chunkStart = new BlockPos(chunkPos.getMinBlockX(), yOffset, chunkPos.getMinBlockZ());

        ListTag blockEntityTags = chunkTag.getList("TileEntities", Tag.TAG_COMPOUND);

        for (int i = 0; i < paletteTag.size(); i++)
        {
            final CompoundTag paletteEntryTag = paletteTag.getCompound(i);
            if (!paletteEntryTag.contains("Name"))
                continue;

            final String name = paletteEntryTag.getString("Name");
            if (!name.startsWith("%s:".formatted(MOD_ID)))
                continue;

            final Optional<Tuple<BlockState, Optional<BlockEntity>>> replacementData = createBlockReplacementData(paletteEntryTag);
            if (replacementData.isEmpty())
                continue;

            final BlockState blockState = replacementData.get().getA();
            final CompoundTag blockStateTag = NbtUtils.writeBlockState(blockState);
            final List<String> paletteEntryTagKeys = paletteEntryTag.getAllKeys().stream().toList();
            paletteEntryTagKeys.forEach(paletteEntryTag::remove);
            blockStateTag.getAllKeys().forEach(key -> paletteEntryTag.put(key, Objects.requireNonNull(blockStateTag.get(key))));

            int y = sectionTag.getByte("Y");

            if (replacementData.get().getB().isPresent())
            {
                final CompoundTag workingEntityNbt = replacementData.get().getB().get().saveWithFullMetadata();
                paletteEntryToBitStoragePositionMap.get(i).forEach(bitStorageIndex -> {
                    final int inChunkX = bitStorageIndex & 15;
                    final int inChunkY = (bitStorageIndex >> 8) & 15;
                    final int inChunkZ = (bitStorageIndex >> 4) & 15;

                    final BlockPos targetPos = chunkStart.offset(inChunkX, y * 15 + inChunkY, inChunkZ);
                    final CompoundTag targetTag = workingEntityNbt.copy();
                    targetTag.putInt("x", targetPos.getX());
                    targetTag.putInt("y", targetPos.getY());
                    targetTag.putInt("z", targetPos.getZ());

                    blockEntityTags.add(targetTag);
                });
            }
        }
    }

    public static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockReplacementData(final CompoundTag paletteEntryTag)
    {
        final String name = paletteEntryTag.getString("Name");

        if (name.endsWith("_blockpaperwall"))
        {
            return createBlockpaperWallReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("stair") || name.endsWith("stairs"))
        {
            return createBlockStairReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("_wall"))
        {
            return createBlockWallReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("shingle_slab"))
        {
            return createBlockShingeSlabReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("shingle"))
        {
            return createBlockShingleReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("slab"))
        {
            return createBlockSlabReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("fencegate"))
        {
            return createBlockFenceGateReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("fence"))
        {
            return createBlockFenceReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("trapdoor"))
        {
            return createBlockTrapDoorReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("door"))
        {
            return createBlockDoorReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("carpet"))
        {
            return createBlockDirectReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        if (name.endsWith("timber_frame"))
        {
            return createBlockTimberFrameReplacementData(name, paletteEntryTag.getCompound("Properties"));
        }
        return createBlockDirectReplacementData(name, paletteEntryTag.getCompound("Properties"));
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockTimberFrameReplacementData(final String blockName, final CompoundTag propertiesTag) {
        final String materialName = blockName.replace("structurize:", "").replace("_timber_frame", "");

        final Block timberFrameBlock;

        if (materialName.contains("double_crossed"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:double_crossed"));
        }
        else if (materialName.contains("down_gated"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:down_gated"));
        }
        else if (materialName.contains("horizontal_plain"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:horizontal_plain"));
        }
        else if (materialName.contains("one_crossed_lr"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:one_crossed_lr"));
        }
        else if (materialName.contains("one_crossed_rl"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:one_crossed_rl"));
        }
        else if (materialName.contains("plain"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:plain"));
        }
        else if (materialName.contains("side_framed_horizontal"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:side_framed_horizontal"));
        }
        else if (materialName.contains("up_gated"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:up_gated"));
        }
        else if (materialName.contains("side_framed"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:side_framed"));
        }
        else if (materialName.contains("framed"))
        {
            timberFrameBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum:framed"));
        }
        else
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new timberframe. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        if (timberFrameBlock == Blocks.AIR) {
            LOGGER.error("Could not find replacement block for material: %s to create a new timberframe. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final String remainingName = materialName.replace(ForgeRegistries.BLOCKS.getKey(timberFrameBlock).getPath() + "_", "");

        final Block block1;
        Block block2;

        final String[] split = remainingName.split("_");
        final String mat1;
        int startIndex2 = 1;
        if (remainingName.contains("dark_oak"))
        {
            startIndex2 = 2;
            mat1 = "dark_oak";
        }
        else
        {
            mat1 = split[0];
        }

        block1 = MATERIAL_TO_BLOCK_MAP.getOrDefault(mat1.toLowerCase(Locale.ROOT), Blocks.AIR);
        if (block1 == Blocks.AIR) {
            LOGGER.error("Could not find replacement block for material: %s to create a new timberframe. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        String mat2 = "";
        for (int i = startIndex2; i < split.length; i++)
        {
            mat2 += split[i] + "_";
        }
        mat2 = mat2.substring(0, mat2.length()-1);

        block2 = MATERIAL_TO_BLOCK_MAP.getOrDefault(mat2.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("minecraft", mat2))));
        if (block2 == Blocks.AIR) {

            block2 = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum", mat2 + "s")));
            if (block2 == Blocks.AIR)
            {
                LOGGER.error("Could not find replacement block for material: %s to create a new timberframe. Conversion is skipped.".formatted(materialName));
                return Optional.empty();
            }
        }

        final IMateriallyTexturedBlock mtTFBlock = (IMateriallyTexturedBlock) timberFrameBlock;
        final EntityBlock ebTFBlock = (EntityBlock) timberFrameBlock;

        final BlockEntity tfBlockEntity = Objects.requireNonNull(ebTFBlock.newBlockEntity(BlockPos.ZERO, timberFrameBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtTFBlockEntity = (IMateriallyTexturedBlockEntity) tfBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtTFBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent frameComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent centerComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(frameComponent.getId(), block1)
            .put(centerComponent.getId(), block2)
            .build()
        );

        mtTFBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(timberFrameBlock, propertiesTag),
            Optional.of(tfBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockShingleReplacementData(final String blockName, final CompoundTag propertiesTag) {
        final String materialName = blockName.replace("structurize:", "").replace("_shingle", "");

        final Block block1;
        Block block2;

        final String[] split = materialName.split("_");
        final String mat1;
        int endIndex = split.length - 1;
        if (materialName.contains("dark_oak"))
        {
            endIndex = split.length - 2;
            mat1 = "dark_oak";
        }
        else
        {
            mat1 = split[endIndex];
        }

        block1 = MATERIAL_TO_BLOCK_MAP.getOrDefault(mat1.toLowerCase(Locale.ROOT), Blocks.AIR);
        if (block1 == Blocks.AIR) {
            LOGGER.error("Could not find replacement block for material: %s to create a new shingle. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        String mat2 = "";
        for (int i = 0; i < endIndex; i++)
        {
            mat2 += split[i] + "_";
        }
        mat2 = mat2.substring(0, mat2.length()-1);

        block2 = MATERIAL_TO_BLOCK_MAP.getOrDefault(mat2.toLowerCase(Locale.ROOT), Blocks.AIR);
        if (block2 == Blocks.AIR) {

            LOGGER.error("Could not find replacement block for material: %s to create a new shingle. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block shingleBlock = IModBlocks.getInstance().getShingle();
        final IMateriallyTexturedBlock mtShingleBlock = (IMateriallyTexturedBlock) shingleBlock;
        final EntityBlock ebShingleBlock = (EntityBlock) shingleBlock;

        final BlockEntity shingleEntity = Objects.requireNonNull(ebShingleBlock.newBlockEntity(BlockPos.ZERO, shingleBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtShingleSlabBlockEntity = (IMateriallyTexturedBlockEntity) shingleEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtShingleBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent roofComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent supportComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(roofComponent.getId(), block2)
            .put(supportComponent.getId(), block1)
            .build()
        );

        mtShingleSlabBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(shingleBlock, propertiesTag),
            Optional.of(shingleEntity)
          )
        );
    }


    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockShingeSlabReplacementData(final String blockName, final CompoundTag propertiesTag) {
        final String materialName = blockName.replace("structurize:", "").replace("_shingle_slab", "");

        final Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Blocks.AIR);

        if (replacementBlock == Blocks.AIR) {
            LOGGER.error("Could not find replacement block for material: %s to create a new shingle slab. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block shingleSlabBlock = IModBlocks.getInstance().getShingleSlab();
        final IMateriallyTexturedBlock mtShingleSlabBlock = (IMateriallyTexturedBlock) shingleSlabBlock;
        final EntityBlock ebShingleSlablBlock = (EntityBlock) shingleSlabBlock;

        final BlockEntity thinShingleSlabEntity = Objects.requireNonNull(ebShingleSlablBlock.newBlockEntity(BlockPos.ZERO, shingleSlabBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtShingleSlabBlockEntity = (IMateriallyTexturedBlockEntity) thinShingleSlabEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtShingleSlabBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent roofComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent supportComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent coverComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(roofComponent.getId(), replacementBlock)
            .put(supportComponent.getId(), Blocks.OAK_PLANKS)
            .put(coverComponent.getId(), replacementBlock)
            .build()
        );

        mtShingleSlabBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(shingleSlabBlock, propertiesTag),
            Optional.of(thinShingleSlabEntity)
          )
        );
    }


    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockpaperWallReplacementData(final String blockName, final CompoundTag propertiesTag) {
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
            Optional.of(thinWallBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockStairReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_stairs", "").replace("stair", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new stair. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block stairBlock = IModBlocks.getInstance().getStair();
        final IMateriallyTexturedBlock mtStairBlock = (IMateriallyTexturedBlock) stairBlock;
        final EntityBlock ebStairBlock = (EntityBlock) stairBlock;

        final BlockEntity stairBlockEntity = Objects.requireNonNull(ebStairBlock.newBlockEntity(BlockPos.ZERO, stairBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtStairBlockEntity = (IMateriallyTexturedBlockEntity) stairBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtStairBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtStairBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(stairBlock, propertiesTag),
            Optional.of(stairBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockWallReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_wall", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new wall. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block wallBlock = IModBlocks.getInstance().getWall();
        final IMateriallyTexturedBlock mtWallBlock = (IMateriallyTexturedBlock) wallBlock;
        final EntityBlock ebWallBlock = (EntityBlock) wallBlock;

        final BlockEntity wallBlockEntity = Objects.requireNonNull(ebWallBlock.newBlockEntity(BlockPos.ZERO, wallBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtWallBlockEntity = (IMateriallyTexturedBlockEntity) wallBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtWallBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtWallBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(wallBlock, propertiesTag),
            Optional.of(wallBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockSlabReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_slab", "").replace("slab", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new slab. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block slabBlock = IModBlocks.getInstance().getSlab();
        final IMateriallyTexturedBlock mtSlabBlock = (IMateriallyTexturedBlock) slabBlock;
        final EntityBlock ebSlabBlock = (EntityBlock) slabBlock;

        final BlockEntity slabBlockEntity = Objects.requireNonNull(ebSlabBlock.newBlockEntity(BlockPos.ZERO, slabBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtSlabBlockEntity = (IMateriallyTexturedBlockEntity) slabBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtSlabBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtSlabBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(slabBlock, propertiesTag),
            Optional.of(slabBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockFenceReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_fence", "").replace("fence", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new fence. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block fenceBlock = IModBlocks.getInstance().getFence();
        final IMateriallyTexturedBlock mtFenceBlock = (IMateriallyTexturedBlock) fenceBlock;
        final EntityBlock ebFenceBlock = (EntityBlock) fenceBlock;

        final BlockEntity fenceBlockEntity = Objects.requireNonNull(ebFenceBlock.newBlockEntity(BlockPos.ZERO, fenceBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtFenceBlockEntity = (IMateriallyTexturedBlockEntity) fenceBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtFenceBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtFenceBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(fenceBlock, propertiesTag),
            Optional.of(fenceBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockFenceGateReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_fencegate", "").replace("fencegate", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new gate. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block fenceGateBlock = IModBlocks.getInstance().getFenceGate();
        final IMateriallyTexturedBlock mtFenceGateBlock = (IMateriallyTexturedBlock) fenceGateBlock;
        final EntityBlock ebFenceGateBlock = (EntityBlock) fenceGateBlock;

        final BlockEntity fenceGateBlockEntity = Objects.requireNonNull(ebFenceGateBlock.newBlockEntity(BlockPos.ZERO, fenceGateBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtFenceGateBlockEntity = (IMateriallyTexturedBlockEntity) fenceGateBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtFenceGateBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtFenceGateBlockEntity.updateTextureDataWith(textureData);

        return Optional.of(
          new Tuple<>(
            buildBlockState(fenceGateBlock, propertiesTag),
            Optional.of(fenceGateBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockDoorReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final Block doorBlock = IModBlocks.getInstance().getFancyDoor();
        final IMateriallyTexturedBlock mtDoorBlock = (IMateriallyTexturedBlock) doorBlock;
        final EntityBlock ebDoorBlock = (EntityBlock) doorBlock;

        final BlockEntity doorBlockEntity = Objects.requireNonNull(ebDoorBlock.newBlockEntity(BlockPos.ZERO, doorBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtDoorBlockEntity = (IMateriallyTexturedBlockEntity) doorBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtDoorBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();
        final IMateriallyTexturedBlockComponent secondComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), CACTUS_BLOCK)
            .put(secondComponent.getId(), GREEN_CACTUS_BLOCK)
            .build()
        );

        mtDoorBlockEntity.updateTextureDataWith(textureData);

        propertiesTag.putString("type", "creeper");

        return Optional.of(
          new Tuple<>(
            buildBlockState(doorBlock, propertiesTag),
            Optional.of(doorBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockTrapDoorReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "").replace("_trapdoor", "").replace("trapdoor", "");

        Block replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName))));
        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum",materialName + "s"))));
        }

        if (replacementBlock == Blocks.AIR)
        {
            LOGGER.error("Could not find replacement block for material: %s to create a new trapdoor. Conversion is skipped.".formatted(materialName));
            return Optional.empty();
        }

        final Block trapDoorBlock = IModBlocks.getInstance().getTrapdoor();
        final IMateriallyTexturedBlock mtTrapDoorBlock = (IMateriallyTexturedBlock) trapDoorBlock;
        final EntityBlock ebTrapDoorBlock = (EntityBlock) trapDoorBlock;

        final BlockEntity trapDoorBlockEntity = Objects.requireNonNull(ebTrapDoorBlock.newBlockEntity(BlockPos.ZERO, trapDoorBlock.defaultBlockState()));
        final IMateriallyTexturedBlockEntity mtTrapDoorBlockEntity = (IMateriallyTexturedBlockEntity) trapDoorBlockEntity;

        final Collection<IMateriallyTexturedBlockComponent> components = mtTrapDoorBlock.getComponents();
        final Iterator<IMateriallyTexturedBlockComponent> componentIterator = components.iterator();

        final IMateriallyTexturedBlockComponent mainComponent = componentIterator.next();

        final MaterialTextureData textureData = new MaterialTextureData(
          ImmutableMap.<ResourceLocation, Block>builder()
            .put(mainComponent.getId(), replacementBlock)
            .build()
        );

        mtTrapDoorBlockEntity.updateTextureDataWith(textureData);

        propertiesTag.putString("type", "port_manteau");

        return Optional.of(
          new Tuple<>(
            buildBlockState(trapDoorBlock, propertiesTag),
            Optional.of(trapDoorBlockEntity)
          )
        );
    }

    private static Optional<Tuple<BlockState, Optional<BlockEntity>>> createBlockDirectReplacementData(final String blockName, final CompoundTag propertiesTag)
    {
        final String materialName = blockName.replace("structurize:", "");

        Block replacementBlock = Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("domum_ornamentum", materialName)));

        if (replacementBlock == Blocks.AIR)
        {
            replacementBlock = MATERIAL_TO_BLOCK_MAP.getOrDefault(materialName.toLowerCase(Locale.ROOT), Blocks.AIR);
            if (replacementBlock == Blocks.AIR)
            {
                LOGGER.error("Could not find replacement block for material: %s to create a new direct block. Conversion is skipped.".formatted(materialName));
                return Optional.empty();
            }
        }

        return Optional.of(
          new Tuple<>(
            buildBlockState(replacementBlock, propertiesTag),
            Optional.empty()
          )
        );
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
