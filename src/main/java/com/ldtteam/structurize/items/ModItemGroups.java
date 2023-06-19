package com.ldtteam.structurize.items;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * Class used to handle the creativeTab of structurize.
 */
@Mod.EventBusSubscriber
public final class ModItemGroups
{
    public static final  DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> GENERAL = TAB_REG.register("general", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1).icon(() -> new ItemStack(ModItems.buildTool.get())).title(Component.translatable("itemGroup." + MOD_ID)).displayItems((config, output) -> {
        output.accept(ModBlocks.blockSubstitution.get());
        output.accept(ModBlocks.blockSolidSubstitution.get());
        output.accept(ModBlocks.blockFluidSubstitution.get());

        output.accept(ModItems.buildTool.get());
        output.accept(ModItems.shapeTool.get());
        output.accept(ModItems.scanTool.get());
        output.accept(ModItems.tagTool.get());
        output.accept(ModItems.caliper.get());
        output.accept(ModItems.blockTagSubstitution.get());
    }).build());

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
