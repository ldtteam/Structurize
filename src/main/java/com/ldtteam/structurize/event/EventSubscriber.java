package com.ldtteam.structurize.event;

import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
    /**
     * If the manager finished loading already.
     */
    private static boolean loaded = false;

    /**
     * Private constructor to hide implicit public one.
     */
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Called when world is about to load.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent event)
    {
        Log.getLogger().warn("FMLServerStartingEvent");
        EntryPoint.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(@NotNull final WorldEvent.Load event)
    {
        if (!event.getWorld().isRemote())
        {
            if (!loaded)
            {
                Log.getLogger().warn("FMLServerStartedEvent");
                Structures.init();
                loaded = true;
            }
        }
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.event.entity.player.PlayerEvent}
     */
    @SubscribeEvent
    public static void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayerEntity) event.getPlayer());
            Network.getNetwork().sendToPlayer(new StructurizeStylesMessage(), (ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }
        Manager.onWorldTick((ServerWorld) event.world);
    }
    
    /**
     * Event used to render the schematics. Only render the schematic if there is one in the settings.
     *
     * @param event Object containing event details.
     */
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderWorldLast(final RenderWorldLastEvent event)
    {
        final Structure structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            StructureClientHandler.renderStructure(structure, event.getPartialTicks(), Settings.instance.getPosition());
        }
    }

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final Structure structure = Settings.instance.getActiveStructure();
        final ClientPlayerEntity player = Minecraft.getInstance().player;

        if (structure != null)
        {
            final BlockPos primaryOffset = BlueprintUtils.getPrimaryBlockOffset(structure.getBluePrint());
            final BlockPos offset = primaryOffset.rotate(BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
            BlockPos pos = Settings.instance.getPosition().subtract(offset);

            BlockPos size = structure.getSize(BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()), Settings.instance.getMirror());

            final BlockPos smallOffset;
            final boolean mirrored = Settings.instance.getMirror() != Mirror.NONE;
            switch (Settings.instance.getRotation())
            {
                case 1:
                    size = new BlockPos(-size.getX(), size.getY(), size.getZ());
                    smallOffset = new BlockPos(-1, 1, 1);
                    if (mirrored)
                    {
                        pos = pos.add(0, 0, -size.getZ() + (2 * offset.getZ()) + 1);
                    }
                    break;
                case 2:
                    if (mirrored)
                    {
                        pos = pos.add(size.getX() - (2 * primaryOffset.getX()) - 1, 0, 0);
                    }
                    size = new BlockPos(-size.getX(), size.getY(), -size.getZ());
                    smallOffset = new BlockPos(-1, 1, -1);
                    break;
                case 3:
                    if (mirrored)
                    {
                        pos = pos.add(0, 0, size.getZ() + (2 * offset.getZ()) - 1);
                    }
                    size = new BlockPos(size.getX(), size.getY(), -size.getZ());
                    smallOffset = new BlockPos(1, 1, -1);
                    break;
                default:
                    if (mirrored)
                    {
                        pos = pos.add(-size.getX() + (2 * primaryOffset.getX()) + 1, 0, 0);
                    }
                    smallOffset = new BlockPos(1, 1, 1);
                    break;
            }

            renderBox(pos, pos.add(size).subtract(smallOffset), player, event);
        }

        if (Settings.instance.getBox() != null)
        {
            renderBox(Settings.instance.getBox().getA(), Settings.instance.getBox().getB(), player, event);
        }
    }

    private static void renderBox(final BlockPos posA, final BlockPos posB, final ClientPlayerEntity player, final RenderWorldLastEvent event)
    {
        int x1 = posA.getX();
        int y1 = posA.getY();
        int z1 = posA.getZ();

        int x2 = posB.getX();
        int y2 = posB.getY();
        int z2 = posB.getZ();

        if (x1 > x2)
        {
            x1++;
        }
        else
        {
            x2++;
        }

        if (y1 > y2)
        {
            y1++;
        }
        else
        {
            y2++;
        }

        if (z1 > z2)
        {
            z1++;
        }
        else
        {
            z2++;
        }

        final double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
        final double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
        final double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(2.0F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);

        final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1-player.getEyeHeight(), z1, x2, y2-player.getEyeHeight(), z2);
        WorldRenderer.drawSelectionBoundingBox(axisalignedbb.grow(0.002D).offset(-renderPosX, -renderPosY, -renderPosZ), 1.0F, 1.0F, 1.0F, 1.0F);


        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }
}
