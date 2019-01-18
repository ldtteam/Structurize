package com.structurize.api.scanning;

import com.structurize.blockout.views.Window;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public interface IScanWizardStep
{
    ResourceLocation getId();

    @SideOnly(Side.CLIENT)
    void onWindowConstructed(final IScanWizardWindowConstructor constructor);

    @SideOnly(Side.CLIENT)
    void onWindowOpened(final Window scanWindow);

    @SideOnly(Side.CLIENT)
    void onUnhandledKeyTypeDuringStep();

    @SideOnly(Side.CLIENT)
    void onDisard();

    @SideOnly(Side.CLIENT)
    void onConfirm(@NotNull final NBTTagCompound scanWizardData);

    boolean onScan(
      @NotNull final EntityPlayer player,
      @NotNull final NBTTagCompound scanWizardDataFromClient,
      @NotNull final Template template,
      @NotNull final NBTTagCompound additionalDataToSave,
      @NotNull final BlockPos from,
      @NotNull final BlockPos to,
      @NotNull final String name);
}
