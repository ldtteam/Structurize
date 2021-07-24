package com.ldtteam.structurize.client;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.event.ClientLifecycleSubscriber;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fmllegacy.RegistryObject;

import java.util.EnumMap;

public class BlocksToRenderTypeHelper
{
    private static final EnumMap<BlockType, RenderType> BLOCK_TYPES = new EnumMap<>(BlockType.class);

    static
    {
        BLOCK_TYPES.put(BlockType.BLOCK, RenderType.solid());
        BLOCK_TYPES.put(BlockType.SLAB, RenderType.solid());
        BLOCK_TYPES.put(BlockType.STAIRS, RenderType.solid());
        BLOCK_TYPES.put(BlockType.WALL, RenderType.solid());

        BLOCK_TYPES.put(BlockType.PLANKS, RenderType.solid());
        BLOCK_TYPES.put(BlockType.FENCE, RenderType.solid());
        BLOCK_TYPES.put(BlockType.FENCE_GATE, RenderType.solid());
        BLOCK_TYPES.put(BlockType.TRAPDOOR, RenderType.cutout());
        BLOCK_TYPES.put(BlockType.DOOR, RenderType.cutout());

    }

    public static void registerBlockType(final BlockType type, final RegistryObject<Block> block)
    {
        ClientLifecycleSubscriber.DELAYED_RENDER_TYPE_SETUP.add(new Tuple<>(block, BLOCK_TYPES.get(type)));
    }
}
