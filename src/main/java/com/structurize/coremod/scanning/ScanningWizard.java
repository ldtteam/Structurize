package com.structurize.coremod.scanning;

import com.structurize.api.scanning.IScanWizardStep;
import com.structurize.api.util.BlockPosUtil;
import com.structurize.api.util.LanguageHandler;
import com.structurize.api.util.constant.Constants;
import com.structurize.api.util.constant.NbtTagConstants;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.network.messages.SaveScanMessage;
import com.structurize.coremod.network.messages.ScanOnServerMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;

import static com.structurize.api.util.constant.Constants.MAX_SCHEMATIC_SIZE;
import static com.structurize.api.util.constant.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;

public class ScanningWizard
{
    private final LinkedHashMap<ResourceLocation, IScanWizardStep> steps = new LinkedHashMap<>();

    ScanningWizard()
    {
    }

    public void registerStep(@NotNull final IScanWizardStep step)
    {
        this.steps.put(step.getId(), step);
    }

    public Collection<IScanWizardStep> getSteps()
    {
        return this.steps.values();
    }

    @SideOnly(Side.CLIENT)
    public void onConfirm()
    {
        final NBTTagCompound compound = new NBTTagCompound();
        for (IScanWizardStep step :
          getSteps())
        {
            final NBTTagCompound stepCompound = new NBTTagCompound();
            step.onConfirm(stepCompound);

            compound.setTag(step.getId().toString(), stepCompound);
        }

        Structurize.getNetwork().sendToServer(new ScanOnServerMessage(compound));
    }

    public void onScan(@NotNull final EntityPlayer player, @NotNull final NBTTagCompound scanWizardCompoundFromClient)
    {
        final NBTTagCompound nameStepCompound = scanWizardCompoundFromClient.getCompoundTag(NbtTagConstants.TAG_NAME_STEP);
        final String name = nameStepCompound.getString(NbtTagConstants.TAG_NAME_STEP_NAME);

        final NBTTagCompound positionStepCompound = scanWizardCompoundFromClient.getCompoundTag(NbtTagConstants.TAG_POSITION_STEP);
        final BlockPos from = BlockPosUtil.readFromNBT(positionStepCompound, NbtTagConstants.TAG_POSITION_STEP_FROM);
        final BlockPos to = BlockPosUtil.readFromNBT(positionStepCompound, NbtTagConstants.TAG_POSITION_STEP_TO);
        final World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(positionStepCompound.getInteger(NbtTagConstants.TAG_POSITION_STEP_DIM));

        final Tuple<Template, String> scannedTemplateInformation = scanTemplate(player, world, from, to, name);

        if (scannedTemplateInformation == null)
            return;

        final NBTTagCompound templateCompound = scannedTemplateInformation.getFirst().writeToNBT(new NBTTagCompound());

        for (final IScanWizardStep step :
          getSteps())
        {
            if (!step.onScan(player, scanWizardCompoundFromClient, templateCompound.))
        }

        Structurize.getNetwork().sendTo(
          new SaveScanMessage(scannedTemplateInformation.getFirst().writeToNBT(new NBTTagCompound()), scannedTemplateInformation.getSecond()), (EntityPlayerMP) player);
    }

    public Tuple<Template, String> scanTemplate(@NotNull final EntityPlayer player, @NotNull final World world, @NotNull final BlockPos from, @NotNull final BlockPos to, @NotNull final String name)
    {
        final BlockPos blockpos =
          new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 =
          new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        final BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);

        if (size.getX() * size.getY() * size.getZ() > MAX_SCHEMATIC_SIZE)
        {
            LanguageHandler.sendPlayerMessage(player, MAX_SCHEMATIC_SIZE_REACHED, MAX_SCHEMATIC_SIZE);
            return null;
        }

        final WorldServer worldserver = (WorldServer) world;
        final MinecraftServer minecraftserver = world.getMinecraftServer();
        final TemplateManager templatemanager = worldserver.getStructureTemplateManager();

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        final String prefix = "/structurize/scans/";
        final String fileName;
        if (name.isEmpty())
        {
            fileName = LanguageHandler.format("item.scepterSteel.scanFormat", "", currentMillisString);
        }
        else
        {
            fileName = name;
        }

        final Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(prefix + fileName + ".nbt"));
        template.takeBlocksFromWorld(world, blockpos, size, true, Blocks.STRUCTURE_VOID);
        template.setAuthor(Constants.MOD_ID);

        return new Tuple<>(template, fileName);
    }
}
