package com.ldtteam.structurize.event;

import com.ldtteam.blockout.Loader;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.optifine.OptifineCompat;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Tuple;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ClientLifecycleSubscriber
{
    public static final ArrayList<Tuple<Supplier<Block>, RenderType>> DELAYED_RENDER_TYPE_SETUP = new ArrayList<>();

    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        OptifineCompat.getInstance().intialize();

        final IResourceManager rm = event.getMinecraftSupplier().get().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).addReloadListener((ISelectiveResourceReloadListener) (resourceManager, resourcePredicate) -> {
                if (resourcePredicate.test(VanillaResourceType.MODELS) || resourcePredicate.test(VanillaResourceType.TEXTURES)
                    || resourcePredicate.test(VanillaResourceType.SHADERS))
                {
                    Log.getLogger().debug("Clearing blueprint renderer cache.");
                    BlueprintHandler.getInstance().clearCache();
                }
                Log.getLogger().debug("Clearing gui XML cache.");
                Loader.cleanParsedCache();
            });
        }

        // final RenderType s = RenderType.getSolid();
        // ModBlocks.blockSubstitution;
        // ModBlocks.blockSolidSubstitution;
        // ModBlocks.blockFluidSubstitution;
        // ModBlocks.multiBlock;
        // ModBlocks.blockDecoBarrel_onside;
        // ModBlocks.blockDecoBarrel_standing;
        // ModBlocks.paperWalls;
        // ModBlocks.floatingCarpets;
        // ModBlocks.timberFrames;
        // ModBlocks.shingles;
        // ModBlocks.shingleSlabs;

        // final RenderType c = RenderType.getCutout();

        // final RenderType cm = RenderType.getCutoutMipped();

        final RenderType t = RenderType.getTranslucent();
        ModBlocks.getPaperWalls().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, t));

        // ModBlocks.CACTI_BLOCKS
        // ModBlocks.BRICKS
        DELAYED_RENDER_TYPE_SETUP.forEach(tu -> {
            if (!tu.getB().equals(RenderType.getSolid()))
            {
                RenderTypeLookup.setRenderLayer(tu.getA().get(), tu.getB());
            }
        });
        DELAYED_RENDER_TYPE_SETUP.clear();
        DELAYED_RENDER_TYPE_SETUP.trimToSize();
    }
}
