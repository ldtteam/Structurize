package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Class used to handle the creativeTab of structurize.
 */
@Mod.EventBusSubscriber
public final class ModItemGroups
{
    private static final ResourceLocation CREATIVE_TAB = new ResourceLocation(Constants.MOD_ID, Constants.MOD_ID);

    @SubscribeEvent
    public static void CreativeTabEvent(final CreativeModeTabEvent.Register event)
    {
        event.registerCreativeModeTab(CREATIVE_TAB, (cf) -> cf.icon(() -> new ItemStack(ModItems.buildTool.get())).withSearchBar().title(Component.literal(Constants.MOD_ID)).displayItems((flagSet, output, ifSth) -> {
            output.accept(ModBlocks.blockSubstitution.get());
            output.accept(ModBlocks.blockSolidSubstitution.get());
            output.accept(ModBlocks.blockFluidSubstitution.get());

            output.accept(ModItems.buildTool.get());
            output.accept(ModItems.shapeTool.get());
            output.accept(ModItems.scanTool.get());
            output.accept(ModItems.tagTool.get());
            output.accept(ModItems.caliper.get());
            output.accept(ModItems.blockTagSubstitution.get());
        }));
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModItemGroups()
    {
        /*
         * Intentionally left empty.
         */
    }
}
