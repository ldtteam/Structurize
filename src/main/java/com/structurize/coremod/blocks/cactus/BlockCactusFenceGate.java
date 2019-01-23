package com.structurize.coremod.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.AbstractBlockStructurizeFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;

import java.util.Locale;

public class BlockCactusFenceGate extends AbstractBlockStructurizeFenceGate<BlockCactusFenceGate>
{

    public BlockCactusFenceGate(final BlockPlanks.EnumType type)
    {
        super(type);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusfencegate");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusfencegate");
        setSoundType(SoundType.WOOD);
        setLightOpacity(0);
    }

}
