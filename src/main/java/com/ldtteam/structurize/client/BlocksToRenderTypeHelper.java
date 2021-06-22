package com.ldtteam.structurize.client;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.event.ClientLifecycleSubscriber;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.RegistryObject;

import java.util.EnumMap;

public class BlocksToRenderTypeHelper
{
    private static final EnumMap<BlockType, RenderType> BLOCK_TYPES = new EnumMap<>(BlockType.class);

    static
    {
        BLOCK_TYPES.put(BlockType.BLOCK, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.SLAB, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.STAIRS, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.WALL, RenderType.getSolid());

        BLOCK_TYPES.put(BlockType.PLANKS, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.FENCE, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.FENCE_GATE, RenderType.getSolid());
        BLOCK_TYPES.put(BlockType.TRAPDOOR, RenderType.getCutout());
        BLOCK_TYPES.put(BlockType.DOOR, RenderType.getCutout());
    }

    public static void registerBlockType(final BlockType type, final RegistryObject<Block> block)
    {
        ClientLifecycleSubscriber.DELAYED_RENDER_TYPE_SETUP.add(new Tuple<>(block, BLOCK_TYPES.get(type)));
    }
}
