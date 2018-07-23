package com.structurize.coremod.network.messages;

import com.structurize.api.util.Log;
import com.structurize.coremod.util.ClientStructureWrapper;
import com.structurize.structures.helpers.Settings;
import com.structurize.structures.helpers.Structure;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.structurize.coremod.network.messages.SaveScanMessage.TAG_SCHEMATIC;

/**
 * Handles a structure message to the client..
 */
public class SendStructureMessage extends AbstractMessage<SendStructureMessage, IMessage>
{
    private NBTTagCompound nbttagcompound;

    /**
     * Public standard constructor.
     */
    public SendStructureMessage()
    {
        super();
    }

    /**
     * Send a template to the client..
     *
     * @param nbttagcompound the stream.
     */
    public SendStructureMessage(final NBTTagCompound nbttagcompound)
    {
        this.nbttagcompound = nbttagcompound;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final NBTTagCompound wrapperCompound = CompressedStreamTools.readCompressed(stream);
            nbttagcompound = wrapperCompound.getCompoundTag(TAG_SCHEMATIC);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        final NBTTagCompound wrapperCompound = new NBTTagCompound();
        wrapperCompound.setTag(TAG_SCHEMATIC, nbttagcompound);

        final PacketBuffer buffer = new PacketBuffer(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            CompressedStreamTools.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    protected void messageOnClientThread(final SendStructureMessage message, final MessageContext ctx)
    {
        final Template template = new Template();
        template.read(DataFixesManager.createFixer().process(FixTypes.STRUCTURE, message.nbttagcompound));
        final Structure structure = new Structure(ctx.getClientHandler().world);
        structure.setTemplate(template);
        structure.setPlacementSettings(new PlacementSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE));
        Settings.instance.setActiveSchematic(structure);
    }
}
