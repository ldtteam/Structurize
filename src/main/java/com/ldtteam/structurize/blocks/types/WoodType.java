package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.RegistryObject;
import org.jetbrains.annotations.NotNull;

/**
 * The different wood types, a more detailed version that Minecraft's own (also adds cacti)
 */
public enum WoodType implements IStringSerializable
{
    OAK("oak", Blocks.OAK_PLANKS),
    SPRUCE("spruce", Blocks.SPRUCE_PLANKS),
    BIRCH("birch", Blocks.BIRCH_PLANKS),
    JUNGLE("jungle", Blocks.JUNGLE_PLANKS),
    ACACIA("acacia", Blocks.ACACIA_PLANKS),
    DARK_OAK("dark_oak", Blocks.DARK_OAK_PLANKS),
    WARPED("warped", Blocks.WARPED_PLANKS),
    CRIMSON("crimson", Blocks.CRIMSON_PLANKS),
    CACTUS("cactus", ModBlocks.CACTI_BLOCKS.getRegisteredBlocks().get(0));

    private final String name;
    private final Block  material;
    private final RegistryObject<Block> registeredMaterial;

    WoodType(final String nameIn, final Block material)
    {
        this.name = nameIn;
        this.material = material;
        this.registeredMaterial = null;
    }

    WoodType(final String nameIn, final RegistryObject<Block> material)
    {
        this.name = nameIn;
        this.material = null;
        this.registeredMaterial = material;
    }

    @NotNull
    @Override
    public String getSerializedName()
    {
        return this.name;
    }

    public Block getMaterial()
    {
        return material == null && registeredMaterial != null ? registeredMaterial.get() : material;
    }
}
