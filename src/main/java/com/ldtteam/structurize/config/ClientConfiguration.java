package com.ldtteam.structurize.config;

import com.ldtteam.common.config.AbstractConfiguration;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    // blueprint renderer

    public final BooleanValue renderPlaceholdersNice;
    public final BooleanValue sharePreviews;
    public final BooleanValue displayShared;
    public final IntValue rendererLightLevel;
    public final DoubleValue rendererTransparency;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    public ClientConfiguration(final Builder builder)
    {
        super(builder, Constants.MOD_ID);

        createCategory("blueprint.renderer");
        // if you add anything to this category, also add it #collectPreviewRendererSettings()
        
        renderPlaceholdersNice = defineBoolean("render_placeholders_nice", false);
        sharePreviews = defineBoolean("share_previews", false);
        displayShared = defineBoolean("see_shared_previews", false);
        rendererLightLevel = defineInteger("light_level", 15, -1, 15);
        rendererTransparency = defineDouble("transparency", -1, -1, 1);

        addWatcher(BlueprintHandler.getInstance()::clearCache, renderPlaceholdersNice, rendererLightLevel);
        addWatcher(displayShared, (oldValue, isSharingEnabled) -> {
            // notify server
            new SyncSettingsToServer().sendToServer();
            if (!isSharingEnabled)
            {
                RenderingCache.removeSharedPreviews();
            }
        });
        addWatcher(sharePreviews, (oldVal, shouldSharePreviews) -> {
            if (shouldSharePreviews)
            {
                RenderingCache.getBlueprintsToRender().forEach(BlueprintPreviewData::syncChangesToServer);
            }
        });

        finishCategory();
    }

    /**
     * Things which should be in buildtool settings, order is mostly carried over to gui order
     */
    public void collectPreviewRendererSettings(final Consumer<ConfigValue<?>> sink)
    {
        sink.accept(sharePreviews);
        sink.accept(displayShared);
        sink.accept(renderPlaceholdersNice);
        sink.accept(rendererLightLevel);
        sink.accept(rendererTransparency);
    }
}
