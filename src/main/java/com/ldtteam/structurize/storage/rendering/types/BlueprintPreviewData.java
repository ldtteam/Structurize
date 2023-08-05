package com.ldtteam.structurize.storage.rendering.types;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler.RenderingCacheKey;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToServer;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.RotationMirror;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
     * The rotation/mirror of the preview.
     */
    @NotNull
    private RotationMirror rotationMirror = RotationMirror.NONE;

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
     * Used for blueprint renderer
     */
    private RenderingCacheKey renderKey;

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
        if (StructurePacks.hasPack(packName))
        {
            blueprintFuture = StructurePacks.getBlueprintFuture(packName, blueprintPath);
        }
        else
        {
            blueprintFuture = null;
        }
        rotationMirror = RotationMirror.values()[byteBuf.readByte()];
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
            byteBuf.writeUtf(StructurePacks.selectedPack.getSubPath(blueprint.getFilePath().resolve(blueprint.getFileName() + ".blueprint")));
        }
        byteBuf.writeByte(rotationMirror.ordinal());
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
                    setBlueprint(blueprintFuture.get());
                    this.blueprintFuture = null;
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
        if (blueprint == null)
        {
            this.blueprint = null;
        }
        else if (!blueprint.equals(this.blueprint))
        {
            this.blueprint = blueprint;
            applyRotationMirrorAndSync();
        }
    }

    /**
     * Mirror the blueprint.
     */
    @OnlyIn(Dist.CLIENT)
    public void mirror()
    {
        this.rotationMirror = this.rotationMirror.mirrorate();
        applyRotationMirrorAndSync();
    }

    /**
     * Rotate the preview by a certain quantity.
     * @param rotation the rotation factor.
     */
    @OnlyIn(Dist.CLIENT)
    public void rotate(final Rotation rotation)
    {
        this.rotationMirror = this.rotationMirror.rotate(rotation);
        applyRotationMirrorAndSync();
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
     * @param by the offset to move it by.
     */
    public void move(final BlockPos by)
    {
        if (this.pos != null)
        {
            setPos(pos.offset(by));
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
    @Deprecated(since = "1.20", forRemoval = true)
    public PlacementSettings getPlacementSettings()
    {
        return new PlacementSettings(rotationMirror.mirror(), rotationMirror.rotation());
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
     * Get the placement settings for this instance.
     * @return the placement settings with mirror and rotation.
     */
    public RotationMirror getRotationMirror()
    {
        return rotationMirror;
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
        if (pos != null)
        {
            syncChangesToServer();
        }
    }

    private void applyRotationMirrorAndSync()
    {
        if (blueprint == null)
        {
            return;
        }

        blueprint.setRotationMirror(rotationMirror, Minecraft.getInstance().level);
        renderKey = new RenderingCacheKey(rotationMirror, blueprint);

        scheduleRefresh();
    }

    public RenderingCacheKey getRenderKey()
    {
        if (blueprintFuture != null)
        {
            getBlueprint();
        }
        return renderKey;
    }
}
