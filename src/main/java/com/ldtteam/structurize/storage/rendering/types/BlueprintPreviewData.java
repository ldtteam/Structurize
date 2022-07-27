package com.ldtteam.structurize.storage.rendering.types;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToServer;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.api.util.constant.Constants.SHARE_PREVIEWS;

/**
 * Necessary data for blueprint preview.
 */
public class BlueprintPreviewData
{
    /**
     * The rotation of the preview.
     */
    @NotNull
    private Rotation rotation = Rotation.NONE;

    /**
     * The mirror of the preview.
     */
    @NotNull
    private Mirror mirror = Mirror.NONE;

    /**
     * The position of the preview.
     */
    @Nullable
    private BlockPos pos;

    /**
     * The offset to the ground.
     */
    private int groundOffset  = 0;

    /**
     * If data has to be refreshed.
     */
    private boolean shouldRefresh = false;

    /**
     * Holds the blueprint to be rendered that is still loading.
     */
    private Future<Blueprint> blueprintFuture;

    /**
     * Holds the blueprint to be rendered.
     */
    private Blueprint blueprint;

    /**
     * Data for syncing.
     */
    private String blueprintPath = "";
    private String packName = "";

    /**
     * Default constructor to create a new setup.
     */
    public BlueprintPreviewData()
    {
        // Intentionally left empty.
    }

    /**
     * Create blueprint preview data from byteBuf.
     * @param byteBuf the buffer data.
     */
    public BlueprintPreviewData(final FriendlyByteBuf byteBuf)
    {
        pos = byteBuf.readBlockPos();
        this.packName = byteBuf.readUtf(32767);
        this.blueprintPath = byteBuf.readUtf(32767);
        if (StructurePacks.packMetas.containsKey(packName))
        {
            blueprintFuture = StructurePacks.getBlueprintFuture(packName, blueprintPath);
        }
        else
        {
            blueprintFuture = null;
        }
        rotation = Rotation.values()[byteBuf.readInt()];
        mirror = Mirror.values()[byteBuf.readInt()];
    }

    /**
     * Write this preview cache to bytebuf.
     * @param byteBuf the buf to write it to.
     */
    public void writeToBuf(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeBlockPos(pos == null ? BlockPos.ZERO : pos);

        if (blueprint == null)
        {
            byteBuf.writeUtf(packName);
            byteBuf.writeUtf(blueprintPath);
        }
        else
        {
            byteBuf.writeUtf(StructurePacks.selectedPack.getName());
            byteBuf.writeUtf(StructurePacks.selectedPack.getSubPath(blueprint.getFilePath().resolve(blueprint.getFilePath() + ".blueprint")));
        }
        byteBuf.writeInt(rotation.ordinal());
        byteBuf.writeInt(mirror.ordinal());
    }

    /**
     * Set the blueprint future.
     * @param blueprintFuture the future.
     */
    public void setBlueprintFuture(final Future<Blueprint> blueprintFuture)
    {
        this.blueprintFuture = blueprintFuture;
    }

    /**
     * set the y offset between current and original position
     * this also actually adjusts the position -- ensure that's set first
     * @param offset the new offset
     */
    public void setGroundOffset(final int offset)
    {
        pos = pos.below(groundOffset);
        groundOffset = offset;
        pos = pos.above(groundOffset);
    }

    /**
     * Get the current blueprint to render.
     * @return the blueprint or null if not ready yet.
     */
    @OnlyIn(Dist.CLIENT)
    public Blueprint getBlueprint()
    {
        if (pos == null)
        {
            return null;
        }

        if (blueprintFuture != null && blueprintFuture.isDone())
        {
            try
            {
                if (blueprintFuture.get() != null)
                {
                    this.blueprint = blueprintFuture.get();
                    this.blueprintFuture = null;
                    this.blueprint.rotateWithMirror(this.rotation, this.mirror, Minecraft.getInstance().level);
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        return blueprint;
    }

    /**
     * Set a blueprint that is alreayd loaded.
     * @param blueprint the blueprint to set.
     */
    @OnlyIn(Dist.CLIENT)
    public void setBlueprint(final Blueprint blueprint)
    {
        this.blueprintFuture = null;
        if (blueprint != null && !blueprint.equals(this.blueprint))
        {
            this.blueprint = blueprint;
            this.blueprint.rotateWithMirror(this.rotation, this.mirror, Minecraft.getInstance().level);
            syncChangesToServer();
        }
        else
        {
            this.blueprint = blueprint;
        }
    }

    /**
     * Mirror the blueprint.
     */
    @OnlyIn(Dist.CLIENT)
    public void mirror()
    {
        if (blueprint == null)
        {
            return;
        }

        if (mirror == Mirror.NONE)
        {
            mirror = this.rotation.ordinal() % 2 == 0 ? Mirror.FRONT_BACK : Mirror.LEFT_RIGHT;
        }
        else
        {
            mirror = Mirror.NONE;
        }
        this.blueprint.rotateWithMirror(this.rotation, this.mirror, Minecraft.getInstance().level);

        scheduleRefresh();
    }

    /**
     * Rotate the preview by a certain quantity.
     * @param rotation the rotation factor.
     */
    @OnlyIn(Dist.CLIENT)
    public void rotate(final Rotation rotation)
    {
        this.rotation = this.rotation.getRotated(rotation);
        if (blueprint != null)
        {
            blueprint.rotateWithMirror(this.rotation, this.mirror, Minecraft.getInstance().level);
        }
        scheduleRefresh();
    }

    /**
     * Sync the changes to the server.
     */
    private void syncChangesToServer()
    {
        if (BlueprintRenderSettings.instance.renderSettings.get(SHARE_PREVIEWS) && (blueprint == null || blueprint.getName() != null))
        {
            Network.getNetwork().sendToServer(new SyncPreviewCacheToServer(this));
        }
    }

    /**
     * Move this preview by an offset.
     * @param pos the offset to move it by.
     */
    public void move(final BlockPos pos)
    {
        if (this.pos != null)
        {
            this.pos = this.pos.offset(pos);
            syncChangesToServer();
        }
    }

    /**
     * Check if the blueprint rendered should refresh the cache.
     * @return true if so.
     */
    public boolean shouldRefresh()
    {
        final boolean ret = shouldRefresh;
        shouldRefresh = false;
        return ret;
    }

    /**
     * Tell the structurize renderer to refresh the cache.
     */
    public void scheduleRefresh()
    {
        shouldRefresh = true;
        syncChangesToServer();
    }

    /**
     * Get the placement settings for this instance.
     * @return the placement settings with mirror and rotation.
     */
    public PlacementSettings getPlacementSettings()
    {
        return new PlacementSettings(mirror, rotation);
    }

    /**
     * Check if this is an invalid preview data object.
     * @return true if so.
     */
    public boolean isEmpty()
    {
        return blueprintFuture == null && blueprint == null;
    }

    /**
     * Get the rotation of the preview.
     * @return the rotation.
     */
    public Rotation getRotation()
    {
        return rotation;
    }

    /**
     * Get the mirror of the preview.
     * @return the mirror.
     */
    public Mirror getMirror()
    {
        return mirror;
    }

    /**
     * Get the pos of the preview.
     * @return the pos.
     */
    public BlockPos getPos()
    {
        return pos;
    }

    /**
     * Set the position of the preview.
     * @param pos the pos to set.
     */
    public void setPos(final BlockPos pos)
    {
        this.pos = pos;
    }
}
