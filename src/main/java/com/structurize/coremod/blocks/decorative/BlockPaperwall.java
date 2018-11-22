package com.structurize.coremod.blocks.decorative;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.types.AbstractBlockStructurizePane;
import com.structurize.coremod.blocks.types.PaperwallType;
import com.structurize.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The paperwall block class defining the paperwall.
 */
public class BlockPaperwall extends AbstractBlockStructurizePane<BlockPaperwall>
{
    /**
     * The variants for the paperwall.
     */
    public static final PropertyEnum<PaperwallType> VARIANT        = PropertyEnum.create("variant", PaperwallType.class);

    /**
     * This blocks name.
     */
    public static final String                      BLOCK_NAME     = "blockPaperwall";

    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    public BlockPaperwall()
    {
        super(Material.GLASS, true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, PaperwallType.JUNGLE));
        initBlock();
    }

    private void initBlock()
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemColored(this, true)).setRegistryName(this.getRegistryName()));
    }

    @NotNull
    @Override
    public MapColor getMapColor(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos)
    {
        return  state.getValue(VARIANT).getMapColor();
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(VARIANT, PaperwallType.byMetadata(meta));
    }

    @Override
    public int damageDropped(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    @NotNull
    @Override
    protected ItemStack getSilkTouchDrop(@NotNull final IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    @Override
    public void getSubBlocks(final CreativeTabs itemIn, final NonNullList<ItemStack> items)
    {
        for (final PaperwallType type : PaperwallType.values())
        {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(VARIANT).getMetadata();
    }

    @NotNull
    @Override
    public IBlockState withRotation(@NotNull final IBlockState state, final Rotation rot)
    {
        switch (rot)
        {
            case CLOCKWISE_180:
                return state.withProperty(NORTH, state.getValue(SOUTH))
                         .withProperty(EAST, state.getValue(WEST)).withProperty(SOUTH, state.getValue(NORTH))
                         .withProperty(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(EAST))
                         .withProperty(EAST, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(WEST))
                         .withProperty(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.withProperty(NORTH, state.getValue(WEST))
                         .withProperty(EAST, state.getValue(NORTH)).withProperty(SOUTH, state.getValue(EAST))
                         .withProperty(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    @NotNull
    @Override
    public IBlockState withMirror(@NotNull final IBlockState state, final Mirror mirrorIn)
    {
        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
            default:
                return super.withMirror(state, mirrorIn);
        }
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, VARIANT);
    }

    @Override
    public boolean canPaneConnectTo(final IBlockAccess world, final BlockPos pos, final EnumFacing dir)
    {
        final BlockPos off = pos.offset(dir);
        final IBlockState state = world.getBlockState(off);
        return super.canPaneConnectTo(world, pos, dir)
                 || state.isSideSolid(world, off, dir.getOpposite()) || state.getBlock() instanceof BlockPaperwall;
    }
}
