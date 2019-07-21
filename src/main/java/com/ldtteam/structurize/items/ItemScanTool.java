package com.ldtteam.structurize.items;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.gui.WindowScan;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.MAX_SCHEMATIC_SIZE;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemStructurize
{
    /**
     * Creates instance of item.
     * @param properties the properties.
     */
    public ItemScanTool(final Properties properties)
    {
        super("scepterSteel", properties.maxStackSize(1));
    }

    @Override
    public float getDestroySpeed(final ItemStack stack, final BlockState state)
    {
        return Float.MAX_VALUE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final Hand handIn)
    {
        final ItemStack stack = playerIn.getHeldItem(handIn);
        if (!stack.hasTag())
        {
            stack.setTag(new CompoundNBT());
        }
        final CompoundNBT compound = stack.getTag();

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

        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final PlayerEntity playerIn = context.getPlayer();
        final Hand hand = context.getHand();
        final ItemStack stack = playerIn.getHeldItem(hand);
        final BlockPos pos = context.getPos();
        final World world = context.getWorld();

        if (!stack.hasTag())
        {
            stack.setTag(new CompoundNBT());
        }
        final CompoundNBT compound = stack.getTag();

        @NotNull final BlockPos pos1 = BlockPosUtil.readFromNBT(compound, FIRST_POS_STRING);
        @NotNull final BlockPos pos2 = pos;
        if (pos2.distanceSq(pos1) > 0)
        {
            BlockPosUtil.writeToNBT(compound, SECOND_POS_STRING, pos2);
            if (world.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "item.scepterSteel.point2", pos.getX(), pos.getY(), pos.getZ());
            }

            stack.setTag(compound);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
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
    public static void saveStructure(@NotNull final World world, @NotNull final BlockPos from, @NotNull final BlockPos to, @NotNull final PlayerEntity player, final String name)
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
        Structurize.getNetwork().sendToPlayer(new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(bp), fileName), (ServerPlayerEntity) player);
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

        final List<File> folder = StructureLoadingUtils.getCachedSchematicsFolders();
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
