package com.ldtteam.structurize.util;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlueprintMissHitResult extends HitResult
{
    public static final BlueprintMissHitResult MISS = new BlueprintMissHitResult();

    private BlueprintMissHitResult()
    {
        super(Vec3.ZERO.subtract(-100, -100, -100));
    }

    @Override
    public Type getType()
    {
        return Type.MISS;
    }
}
