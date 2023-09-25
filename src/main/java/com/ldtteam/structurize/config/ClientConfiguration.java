package com.ldtteam.structurize.config;

import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.BlueprintRenderer;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    // blueprint renderer

    public final BooleanValue renderPlaceholders;
    public final BooleanValue sharePreviews;
    public final BooleanValue displayShared;
    public final IntValue rendererLightLevel;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "blueprint.renderer");
        // if you add anything to this category, also add it #collectPreviewRendererSettings()
        
        renderPlaceholders = defineBoolean(builder, "render_placeholders", false);
        sharePreviews = defineBoolean(builder, "share_previews", false);
        displayShared = defineBoolean(builder, "see_shared_previews", false);
        rendererLightLevel = defineInteger(builder, "light_level", 15, -1, 15);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            addWatcher(renderPlaceholders, (old, newV) -> BlueprintHandler.getInstance().clearCache());
            addWatcher(rendererLightLevel, (old, newV) -> BlueprintHandler.getInstance().clearCache());
        }

        finishCategory(builder);
    }

    public void collectPreviewRendererSettings(final Consumer<ConfigValue<?>> sink)
    {
        sink.accept(renderPlaceholders);
        sink.accept(sharePreviews);
        sink.accept(displayShared);
        sink.accept(rendererLightLevel);
    }
}
