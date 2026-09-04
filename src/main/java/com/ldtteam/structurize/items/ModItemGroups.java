package com.ldtteam.structurize.items;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * Class used to handle the creativeTab of structurize.
 */
public final class ModItemGroups
{
    public static final  DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENERAL = TAB_REG.register("general", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1).icon(() -> new ItemStack(ModItems.buildTool.get())).title(Component.translatable("itemGroup." + MOD_ID)).displayItems((config, output) -> {
        // NeoForge 26.2 validates creative-tab entries as single-item stacks.
        // Construct the stacks explicitly so custom item max-stack settings do
        // not leak into the tab callback.
        output.accept(single(ModBlocks.blockSubstitution.get()));
        output.accept(single(ModBlocks.blockSolidSubstitution.get()));
        output.accept(single(ModBlocks.blockFluidSubstitution.get()));

        output.accept(single(ModItems.buildTool.get()));
        output.accept(single(ModItems.shapeTool.get()));
        output.accept(single(ModItems.scanTool.get()));
        output.accept(single(ModItems.tagTool.get()));
        output.accept(single(ModItems.caliper.get()));
        output.accept(single(ModItems.blockTagSubstitution.get()));
    }).build());

    private static ItemStack single(final ItemLike item)
    {
        final ItemStack stack = new ItemStack(item);
        stack.setCount(1);
        return stack;
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
