package com.ldtteam.structurize.items;

import static com.ldtteam.structurize.api.util.constant.Constants.MAX_SCHEMATIC_SIZE;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structures.helpers.Structure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemStructurize
{
    /**
     * Creates instance of item.
     */
    public ItemScanTool()
    {
        super("scepterSteel");

        super.setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        this.setMaxStackSize(1);
    }

    @Override
    public float getDestroySpeed(final ItemStack stack, final IBlockState state)
    {
        return Float.MAX_VALUE;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, @NotNull final EnumHand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = stack.getTagCompound();

        @NotNull final BlockPos pos1 = BlockPosUtil.readFromNBT(compound, FIRST_POS_STRING);
        @NotNull final BlockPos pos2 = BlockPosUtil.readFromNBT(compound, SECOND_POS_STRING);

        if (!worldIn.isRemote)
        {
            if (playerIn.isSneaking())
            {
                saveStructure(worldIn, pos1, pos2, playerIn, null);
            }
        }
        else
        {
            if (!playerIn.isSneaking())
            {
                final WindowScan window = new WindowScan(pos1, pos2);
                window.open();
            }
        }

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(
      final EntityPlayer playerIn,
      final World worldIn,
      final BlockPos pos,
      final EnumHand hand,
      final EnumFacing facing,
      final float hitX,
      final float hitY,
      final float hitZ)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = stack.getTagCompound();

        @NotNull final BlockPos pos1 = BlockPosUtil.readFromNBT(compound, FIRST_POS_STRING);
        @NotNull final BlockPos pos2 = pos;
        if (pos2.distanceSq(pos1) > 0)
        {
            BlockPosUtil.writeToNBT(compound, SECOND_POS_STRING, pos2);
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.scepterSteel.point2", pos.getX(), pos.getY(), pos.getZ());
            }

            stack.setTagCompound(compound);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world  Current world.
     * @param from   First corner.
     * @param to     Second corner.
     * @param player causing this action.
     * @param name   the name of it.
     */
    public static void saveStructure(@NotNull final World world, @NotNull final BlockPos from, @NotNull final BlockPos to, @NotNull final EntityPlayer player, final String name)
    {
        final BlockPos blockpos =
          new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 =
          new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        final BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);
        if (size.getX() * size.getY() * size.getZ() > MAX_SCHEMATIC_SIZE)
        {
            LanguageHandler.sendPlayerMessage(player, MAX_SCHEMATIC_SIZE_REACHED, MAX_SCHEMATIC_SIZE);
            return;
        }

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        final String fileName;
        if (name == null || name.isEmpty())
        {
            fileName = LanguageHandler.format("item.scepterSteel.scanFormat", "", currentMillisString);
        }
        else
        {
            fileName = name;
        }

        final Blueprint bp = BlueprintUtil.createBlueprint(world, blockpos, (short) size.getX(), (short) size.getY(), (short) size.getZ(), name);
        Structurize.getNetwork().sendTo(
          new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(bp), fileName), (EntityPlayerMP) player);
    }

    /**
     * Save a structure on the server.
     * @param world the world.
     * @param from the start position.
     * @param to the end position.
     * @param name the name.
     * @return true if succesful.
     */
    public static boolean saveStructureOnServer(@NotNull final World world, @NotNull final BlockPos from, @NotNull final BlockPos to, final String name)
    {
        final BlockPos blockpos =
          new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 =
          new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        final BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);
        if (size.getX() * size.getY() * size.getZ() > MAX_SCHEMATIC_SIZE)
        {
            Log.getLogger().warn("Saving too large schematic for:" + name);
        }

        final String prefix = "cache";
        final String fileName;
        if (name == null || name.isEmpty())
        {
            fileName = LanguageHandler.format("item.scepterSteel.scanFormat");
        }
        else
        {
            fileName = name;
        }

        final StructureName structureName = new StructureName(prefix, "backup", fileName);

        final List<File> folder = Structure.getCachedSchematicsFolders();
        if (folder == null || folder.isEmpty())
        {
            Log.getLogger().warn("Unable to save schematic in cache since no folder was found.");
            return false;
        }

        final Blueprint bp = BlueprintUtil.createBlueprint(world, blockpos, (short) size.getX(), (short) size.getY(), (short) size.getZ(), name);


        final File file = new File(folder.get(0), structureName.toString() + Structures.SCHEMATIC_EXTENSION_NEW);
        Utils.checkDirectory(file.getParentFile());

        try (OutputStream outputstream = new FileOutputStream(file))
        {
        	CompressedStreamTools.writeCompressed(BlueprintUtil.writeBlueprintToNBT(bp), outputstream);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
}
