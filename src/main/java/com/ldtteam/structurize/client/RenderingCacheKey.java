package com.ldtteam.structurize.client;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.RotationMirror;

public record RenderingCacheKey(RotationMirror rotationMirror, Blueprint blueprint)
{
}
