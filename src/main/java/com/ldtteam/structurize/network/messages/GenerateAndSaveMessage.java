package com.ldtteam.structurize.network.messages;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Shape;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

public class GenerateAndSaveMessage extends GenerateAndPasteMessage
{
    public GenerateAndSaveMessage(FriendlyByteBuf buf)
    {
        super(buf);
    }

    public GenerateAndSaveMessage(@NotNull BlockPos pos, int length, int width, int height, int frequency, String equation, Shape shape, ItemStack block, ItemStack block2, boolean hollow, Rotation rotation, Mirror mirror)
    {
        super(pos, length, width, height, frequency, equation, shape, block, block2, hollow, rotation, mirror);
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            final Blueprint blueprint = Manager.getStructureFromFormula(width, length, height,
                    frequency, equation, shape, block, block2, hollow);
            blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), mirror ? Mirror.FRONT_BACK : Mirror.NONE, ctxIn.getSender().level);
            // in an ideal world, we'd save the original shape and rotate only after the fact.
            // but the client only has a pre-rotated blueprint to calculate the MD5 from...

            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BlueprintUtil.writeToStream(stream, blueprint);
            Structures.handleSaveSchematicMessage(stream.toByteArray(), true);
        }
    }
}
