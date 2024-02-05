package com.ldtteam.structurize.event;

import com.ldtteam.common.language.LanguageHandler;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.datagen.BlockEntityTagProvider;
import com.ldtteam.structurize.datagen.BlockTagProvider;
import com.ldtteam.structurize.datagen.EntityTagProvider;
import com.ldtteam.structurize.network.messages.*;
import com.ldtteam.structurize.storage.ServerStructurePackLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public class LifecycleSubscriber
{
    @SubscribeEvent
    public static void onNetworkRegistry(final RegisterPayloadHandlerEvent event)
    {
        final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
        final IPayloadRegistrar registry = event.registrar(Constants.MOD_ID).versioned(modVersion);

        AbsorbBlockMessage.TYPE.register(registry);
        AddRemoveTagMessage.TYPE.register(registry);
        BlueprintSyncMessage.TYPE.register(registry);
        BuildToolPlacementMessage.TYPE.register(registry);
        ClientBlueprintRequestMessage.TYPE.register(registry);
        FillTopPlaceholderMessage.TYPE.register(registry);
        ItemMiddleMouseMessage.TYPE.register(registry);
        NotifyClientAboutStructurePacksMessage.TYPE.register(registry);
        NotifyServerAboutStructurePacksMessage.TYPE.register(registry);
        OperationHistoryMessage.TYPE.register(registry);
        RemoveBlockMessage.TYPE.register(registry);
        RemoveEntityMessage.TYPE.register(registry);
        ReplaceBlockMessage.TYPE.register(registry);
        SaveScanMessage.TYPE.register(registry);
        ScanOnServerMessage.TYPE.register(registry);
        ScanToolTeleportMessage.TYPE.register(registry);
        SetTagInTool.TYPE.register(registry);
        ShowScanMessage.TYPE.register(registry);
        SyncPreviewCacheToClient.TYPE.register(registry);
        SyncPreviewCacheToServer.TYPE.register(registry);
        SyncSettingsToServer.TYPE.register(registry);
        TransferStructurePackToClient.TYPE.register(registry);
        UndoRedoMessage.TYPE.register(registry);
        UpdateClientRender.TYPE.register(registry);
        UpdateScanToolMessage.TYPE.register(registry);
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

    @SubscribeEvent
    public static void onDedicatedServerInit(final FMLDedicatedServerSetupEvent event)
    {
        ServerStructurePackLoader.onServerStarting();
    }

    @SubscribeEvent
    public static void onDatagen(@NotNull final GatherDataEvent event)
    {
        final DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new BlockEntityTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK_ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new BlockTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new EntityTagProvider(event.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
