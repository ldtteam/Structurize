package com.ldtteam.structurize.datagen;

import com.ldtteam.domumornamentum.entity.block.ModBlockEntityTypes;
import com.ldtteam.structurize.tag.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeRegistryTagsProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * Datagen provider for Block Entity Tags
 */
public class BlockEntityTagProvider extends ForgeRegistryTagsProvider<BlockEntityType<?>>
{
    public BlockEntityTagProvider(@NotNull final DataGenerator generator,
                                  @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(generator, ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        tag(ModTags.SUBSTITUTION_ABSORB_WHITELIST)
                .add(BlockEntityType.CHEST)
                .add(BlockEntityType.SIGN)
                .add(BlockEntityType.LECTERN)
                .add(ModBlockEntityTypes.MATERIALLY_TEXTURED.get())
                ;
    }
}
