package com.ldtteam.structurize.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.ldtteam.blockout.Loader;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.commands.arguments.MultipleStringArgument;
import com.ldtteam.structurize.generation.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.item.Item;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LifecycleSubscriber
{
    /**
     * Called when mod is being initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onModInit(final FMLCommonSetupEvent event)
    {
        Network.getNetwork().registerCommonMessages();
        ArgumentTypes.register(Constants.MOD_ID + ":multistring", MultipleStringArgument.class, new MultipleStringArgument.Serializer());
        StructureLoadingUtils.addOriginMod(Constants.MOD_ID);
    }

    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        ModBlocks.getTimberFrames().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingles().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingleSlabs().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getPaperWalls().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getTranslucent()));
        ModBlocks.getFloatingCarpets().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
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
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        LanguageHandler.setMClanguageLoaded();
    }

    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        // Initialise All Singletons
        event.getGenerator().addProvider(new ModLanguageProvider(event.getGenerator(), Constants.MOD_ID, "default"));
        event.getGenerator().addProvider(new ModRecipeProvider(event.getGenerator()));
        ModBlockTagsProvider mbt = new ModBlockTagsProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper());
        event.getGenerator().addProvider(mbt);
        event.getGenerator().addProvider(new ModItemTagsProvider(event.getGenerator(), mbt, Constants.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ModBlockStateProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ModItemModelProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper()));

        ModBlocks.BRICKS.forEach(type -> type.provide(event));
        ModBlocks.CACTI_BLOCKS.provide(event);
        ModBlocks.timberFrames.forEach(type -> type.provide(event));
        ModBlocks.shingles.forEach(type -> type.provide(event));
        ModBlocks.floatingCarpets.provide(event);
        ModBlocks.shingleSlabs.provide(event);
        ModBlocks.paperWalls.provide(event);

        // Default
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));
    }

    @SubscribeEvent
    public static void onMissingBlockMappings(final RegistryEvent.MissingMappings<Block> missingBlockEvent)
    {
        final ImmutableList<RegistryEvent.MissingMappings.Mapping<Block>> missingBlocks = missingBlockEvent.getMappings(Constants.MOD_ID);
        if (missingBlocks.isEmpty())
        {
            return;
        }

        handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(missingBlocks);
    }

    @SubscribeEvent
    public static void onMissingItemMappings(final RegistryEvent.MissingMappings<Item> missingBlockEvent)
    {
        final ImmutableList<RegistryEvent.MissingMappings.Mapping<Item>> missingItems = missingBlockEvent.getMappings(Constants.MOD_ID);
        if (missingItems.isEmpty())
        {
            return;
        }

        handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(missingItems);
    }

    private static <T extends IForgeRegistryEntry<T>> void handleMissingMappingsDueToRestructureIn9af7543d7dcefa0bf7b52c2e809d6c8d48b803a6(ImmutableList<RegistryEvent.MissingMappings.Mapping<T>> missingMappings)
    {
        final Map<Pattern, String> replacementPatterns = ImmutableMap.<Pattern, String>builder()
                                                           .put(
                                                             Pattern.compile("(\\S+)stone_brick(\\S+)"),
                                                             "stone_bricks"
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)cobble_stone(\\S+)"),
                                                             "cobblestone"
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)blockbrownbrick(\\S+)"),
                                                             "brown_brick"
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)blockbeigebrick(\\S+)"),
                                                             "beige_brick"
                                                           )
                                                           .put(
                                                             Pattern.compile("(\\S+)blockcreambrick(\\S+)"),
                                                             "cream_brick"
                                                           )
                                                           .build();


        missingMappings.forEach(mapping -> {
            if (mapping.key.getPath().equals("placeholderblock"))
            {
                mapping.ignore();
                return;
            }

            final String namespace = mapping.key.getNamespace();
            final String path = mapping.key.getPath();
            for (final Map.Entry<Pattern, String> patternToReplace : replacementPatterns.entrySet())
            {
                final Matcher patternMatcher = patternToReplace.getKey().matcher(path);
                if (patternMatcher.find()) {
                    final String replacedPath = patternMatcher.replaceAll("$1" + patternToReplace.getValue() + "$2");
                    final ResourceLocation remappedObjectKey = new ResourceLocation(namespace, replacedPath);

                    if (mapping.registry.containsKey(remappedObjectKey)) {
                        final T remappedObject = mapping.registry.getValue(remappedObjectKey);
                        mapping.remap(remappedObject);
                        break;
                    }
                }
            }
        });
    }
}
