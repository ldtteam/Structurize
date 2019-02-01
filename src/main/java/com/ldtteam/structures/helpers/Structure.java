package com.ldtteam.structures.helpers;

import static com.structurize.api.util.constant.Suppression.RESOURCES_SHOULD_BE_CLOSED;
import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION;
import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION_NEW;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;

import com.structurize.structures.blueprints.v1.Blueprint;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.structurize.api.configuration.Configurations;
import com.structurize.api.util.Log;
import com.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Structure class, used to store, create, get structures.
 */
public class Structure
{
    /**
     * Rotation by 90°.
     */
    private static final double NINETY_DEGREES = 90D;

    /**
     * Size of the buffer.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Required Datafixer
     */
    private static DataFixer fixer;

    /**
     * The list of origin folders.
     */
    public static List<String> originFolders = new ArrayList<>();

    /**
     * Template of the structure.
     */
    private Template          template;
    private PlacementSettings settings;
    private String            md5;

    /**
     * Constuctor of Structure, tries to create a new structure.
     *
     * @param world         with world.
     * @param structureName name of the structure (at stored location).
     * @param settings      it's settings.
     */
    public Structure(@Nullable final World world, final String structureName, final PlacementSettings settings)
    {
        String correctStructureName = structureName;
        if (world == null || world.isRemote)
        {
            this.settings = settings;
        }

        InputStream inputStream = null;
        try
        {

            //Try the cache first
            if (Structures.hasMD5(correctStructureName))
            {
                inputStream = Structure.getStream(Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName));
                if (inputStream != null)
                {
                    correctStructureName = Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName);
                }
            }

            if (inputStream == null)
            {
                inputStream = Structure.getStream(correctStructureName);
            }

            if (inputStream == null)
            {
                return;
            }

            try
            {
                this.md5 = Structure.calculateMD5(Structure.getStream(correctStructureName));
                final String ending = Structures.getFileExtension(correctStructureName);
                if (ending != null)
                {
                    if (ending.endsWith(SCHEMATIC_EXTENSION))
                    {
                        this.template = readTemplateFromStream(inputStream, getFixer());
                    }
                    else if (ending.endsWith(SCHEMATIC_EXTENSION_NEW))
                    {
                        final NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(inputStream);
                        final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbttagcompound, getFixer());
                        if (blueprint != null)
                        {
                            this.template = BlueprintUtil.toTemplate(blueprint);
                        }
                        else
                        {
                            if (!nbttagcompound.hasKey("DataVersion", 99))
                            {
                                nbttagcompound.setInteger("DataVersion", 500);
                            }

                            final Template template = new Template();
                            template.read(getFixer().process(FixTypes.STRUCTURE, nbttagcompound));
                            this.template = template;
                        }
                    }
                }
            }
            catch (final IOException e)
            {
                Log.getLogger().warn(String.format("Failed to load template %s", correctStructureName), e);
            }
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Get the Datafixer and instantiate if not existing.
     * @return the datafixer.
     */
    public static DataFixer getFixer()
    {
        if (fixer == null)
        {
            fixer = DataFixesManager.createFixer();
            final ModFixs fixs = ((CompoundDataFixer) fixer).init(Constants.MOD_ID, 1);
            fixs.registerFix(FixTypes.STRUCTURE, new IFixableData()
            {
                @Override
                public int getFixVersion()
                {
                    return 1;
                }

                @NotNull
                @Override
                public NBTTagCompound fixTagCompound(@NotNull final NBTTagCompound compound)
                {
                    if (compound.hasKey("palette"))
                    {
                        NBTTagList list = compound.getTagList("palette", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
                        for (final NBTBase listCompound : list)
                        {
                            if (listCompound instanceof NBTTagCompound && ((NBTTagCompound) listCompound).hasKey("Name"))
                            {
                                String name = ((NBTTagCompound) listCompound).getString("Name");
                                if (name.contains("minecolonies"))
                                {
                                    if (Block.getBlockFromName(name) == null)
                                    {
                                        final String structurizeName = "structurize" + name.substring(Constants.MINECOLONIES_MOD_ID.length());
                                        if (Block.getBlockFromName(structurizeName) != null)
                                        {
                                            ((NBTTagCompound) listCompound).setString("Name", structurizeName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return compound;
                }
            });
        }
        return fixer;
    }

    /**
     * get a InputStream for a give structureName.
     * <p>
     * Look into the following director (in order):
     * - scan
     * - cache
     * - schematics folder
     * - jar
     * It should be the exact opposite that the way used to build the list.
     * <p>
     * Suppressing Sonar Rule squid:S2095
     * This rule enforces "Close this InputStream"
     * But in this case the rule does not apply because
     * We are returning the stream and that is reasonable
     *
     * @param structureName name of the structure to load
     * @return the input stream or null
     */
    @SuppressWarnings(RESOURCES_SHOULD_BE_CLOSED)
    @Nullable
    public static InputStream getStream(final String structureName)
    {
        final StructureName sn = new StructureName(structureName);
        InputStream inputstream = null;
        if (Structures.SCHEMATICS_CACHE.equals(sn.getPrefix()))
        {
            for (final File cachedFile : Structure.getCachedSchematicsFolders())
            {
                final InputStream stream = Structure.getStreamFromFolder(cachedFile, structureName);
                if (stream != null)
                {
                    return stream;
                }
            }
        }
        else if (Structures.SCHEMATICS_SCAN.equals(sn.getPrefix()))
        {
            for (final File cachedFile : Structure.getClientSchematicsFolders())
            {
                final InputStream stream = Structure.getStreamFromFolder(cachedFile, structureName);
                if (stream != null)
                {
                    return stream;
                }
            }
        }
        else if (!Structures.SCHEMATICS_PREFIX.equals(sn.getPrefix()))
        {
            return null;
        }
        else
        {
            //Look in the folder first
            inputstream = Structure.getStreamFromFolder(Structurize.proxy.getSchematicsFolder(), structureName);
            if (inputstream == null && !Configurations.gameplay.ignoreSchematicsFromJar)
            {
                for (final InputStream stream : Structure.getStreamsFromJar(structureName))
                {
                    if (stream != null)
                    {
                        inputstream = stream;
                    }
                }
            }
        }

        return inputstream;
    }

    /**
     * Calculate the MD5 hash for a template from an inputstream.
     *
     * @param stream to which we want the MD5 hash
     * @return the MD5 hash string or null
     */
    public static String calculateMD5(final InputStream stream)
    {
        if (stream == null)
        {
            Log.getLogger().error("Structure.calculateMD5: stream is null, this should not happen");
            return null;
        }
        return calculateMD5(getStreamAsByteArray(stream));
    }

    /**
     * Reads a template from an inputstream.
     */
    private static Template readTemplateFromStream(final InputStream stream, final DataFixer fixer) throws IOException
    {
        final NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(stream);

        if (!nbttagcompound.hasKey("DataVersion", 99))
        {
            nbttagcompound.setInteger("DataVersion", 500);
        }

        final Template template = new Template();
        template.read(fixer.process(FixTypes.STRUCTURE, nbttagcompound));
        return template;
    }

    /**
     * get a input stream for a schematic within a specif folder.
     *
     * @param folder        where to load it from.
     * @param structureName name of the structure to load.
     * @return the input stream or null
     */
    @Nullable
    private static InputStream getStreamFromFolder(@Nullable final File folder, final String structureName)
    {
        if (folder == null)
        {
            return null;
        }
        final File nbtFile = new File(folder.getPath() + "/" + structureName + SCHEMATIC_EXTENSION);
        final File blueprintFile = new File(folder.getPath() + "/" + structureName + SCHEMATIC_EXTENSION_NEW);
        try
        {
            if (folder.exists())
            {
                //We need to check that we stay within the correct folder
                if (!nbtFile.toURI().normalize().getPath().startsWith(folder.toURI().normalize().getPath()))
                {
                    Log.getLogger().error("Structure: Illegal structure name \"" + structureName + "\"");
                    return null;
                }
                if (nbtFile.exists())
                {
                    return new FileInputStream(nbtFile);
                }
                else if (blueprintFile.exists())
                {
                    return new FileInputStream(blueprintFile);
                }
            }
        }
        catch (final FileNotFoundException e)
        {
            //we should will never go here
            Log.getLogger().error("Structure.getStreamFromFolder", e);
        }
        return null;
    }

    /**
     * Get the file representation of the cached schematics' folder.
     *
     * @return the folder for the cached schematics
     */
    public static List<File> getCachedSchematicsFolders()
    {
        final List<File> cachedSchems = new ArrayList<>();
        for (final String origin : originFolders)
        {
            if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                if (Manager.getServerUUID() != null)
                {
                    cachedSchems.add(new File(Minecraft.getMinecraft().gameDir, origin + "/" + Manager.getServerUUID()));
                }
                else
                {
                    Log.getLogger().error("Manager.getServerUUID() => null this should not happen");
                    return null;
                }
            }
            else
            {
                cachedSchems.add(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory() + "/" + Constants.MOD_ID));
            }
        }
        return cachedSchems;
    }

    /**
     * get the schematic folder for the client.
     *
     * @return the client folder.
     */
    public static List<File> getClientSchematicsFolders()
    {
        final List<File> clientSchems = new ArrayList<>();
        for (final String origin : originFolders)
        {
            clientSchems.add(new File(Minecraft.getMinecraft().gameDir, origin));
        }
        return clientSchems;
    }

    /**
     * get a input stream for a schematic from jar.
     *
     * @param structureName name of the structure to load from the jar.
     * @return the input stream or null
     */
    private static List<InputStream> getStreamsFromJar(final String structureName)
    {
        final List<InputStream> streamsFromJar = new ArrayList<>();
        for (final String origin : originFolders)
        {
            streamsFromJar.add(MinecraftServer.class.getResourceAsStream("/assets/" + origin + '/' + structureName + SCHEMATIC_EXTENSION));
            streamsFromJar.add(MinecraftServer.class.getResourceAsStream("/assets/" + origin + '/' + structureName + SCHEMATIC_EXTENSION_NEW));
        }
        return streamsFromJar;
    }

    /**
     * Calculate the MD5 hash of a byte array
     *
     * @param bytes array
     * @return the MD5 hash string or null
     */
    public static String calculateMD5(final byte[] bytes)
    {
        try
        {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            return DatatypeConverter.printHexBinary(md.digest(bytes));
        }
        catch (@NotNull final NoSuchAlgorithmException e)
        {
            Log.getLogger().trace(e);
        }

        return null;
    }

    /**
     * Convert an InputStream into and array of bytes.
     *
     * @param stream to be converted to bytes array
     * @return the array of bytes, array is size 0 when the stream is null
     */
    public static byte[] getStreamAsByteArray(final InputStream stream)
    {
        if (stream == null)
        {
            Log.getLogger().info("Structure.getStreamAsByteArray: stream is null this should not happen");
            return new byte[0];
        }
        try
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            final byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = stream.read(data, 0, data.length)) != -1)
            {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().trace(e);
        }
        return new byte[0];
    }

    /**
     * Constuctor of Structure, tries to create a new structure.
     * creates a plain Structure to append rendering later.
     *
     * @param world with world.
     */
    public Structure(@Nullable final World world)
    {
        super();
    }

    public static byte[] compress(final byte[] data)
    {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream))
        {
            zipStream.write(data);
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().error("Could not compress the data", e);
        }
        return byteStream.toByteArray();
    }

    public static byte[] uncompress(final byte[] data)
    {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             GZIPInputStream zipStream = new GZIPInputStream(byteStream))
        {
            int len;
            while ((len = zipStream.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().warn("Could not uncompress data", e);
        }

        return out.toByteArray();
    }

    /**
     * get the Template from the structure.
     *
     * @return The templae for the structure
     */
    public Template getTemplate()
    {
        return this.template;
    }

    /**
     * Set the template externally.
     * @param template the template to set.
     */
    public void setTemplate(final Template template)
    {
        this.template = template;
    }

    /**
     * Compare the md5 from the structure with an other md5 hash.
     *
     * @param otherMD5 to compare with
     * @return whether the otherMD5 match, return false if md5 is null
     */
    public boolean isCorrectMD5(final String otherMD5)
    {
        Log.getLogger().info("isCorrectMD5: md5:" + this.md5 + " other:" + otherMD5);
        if (this.md5 == null || otherMD5 == null)
        {
            return false;
        }
        return this.md5.compareTo(otherMD5) == 0;
    }

    /**
     * Checks if the template is null.
     *
     * @return true if the template is null.
     */
    public boolean isTemplateMissing()
    {
        return this.template == null;
    }

    public Template.BlockInfo[] getBlockInfo()
    {
        Template.BlockInfo[] blockList = new Template.BlockInfo[this.template.blocks.size()];
        blockList = this.template.blocks.toArray(blockList);
        return blockList;
    }

    /**
     * Get entity array at position in world.
     *
     * @param world the world.
     * @param pos   the position.
     * @return the entity array.
     */
    public Entity[] getEntityInfo(final World world, final BlockPos pos)
    {
        Template.EntityInfo[] entityInfoList = new Template.EntityInfo[this.template.entities.size()];
        entityInfoList = this.template.blocks.toArray(entityInfoList);

        final Entity[] entityList = null;

        for (int i = 0; i < entityInfoList.length; i++)
        {
            final Entity finalEntity = EntityList.createEntityFromNBT(entityInfoList[i].entityData, world);
            final Vec3d entityVec = entityInfoList[i].pos.add(new Vec3d(pos));
            finalEntity.setPosition(entityVec.x, entityVec.y, entityVec.z);
        }

        return entityList;
    }

    /**
     * Get size of structure.
     *
     * @param rotation with rotation.
     * @return size as blockPos (x = length, z = width, y = height).
     */
    public BlockPos getSize(final Rotation rotation)
    {
        return this.template.transformedSize(rotation);
    }

    public void setPlacementSettings(final PlacementSettings settings)
    {
        this.settings = settings;
    }

    /**
     * Get blockInfo of structure with a specific setting.
     *
     * @param settings the setting.
     * @return the block info array.
     */
    public ImmutableList<Template.BlockInfo> getBlockInfoWithSettings(final PlacementSettings settings)
    {
        final ImmutableList.Builder<Template.BlockInfo> builder = ImmutableList.builder();

        this.template.blocks.forEach(blockInfo -> {
            final IBlockState finalState = blockInfo.blockState.withMirror(settings.getMirror()).withRotation(settings.getRotation());
            final BlockPos finalPos = Template.transformedBlockPos(settings, blockInfo.pos);
            final Template.BlockInfo finalInfo = new Template.BlockInfo(finalPos, finalState, blockInfo.tileentityData);
            builder.add(finalInfo);
        });

        return builder.build();
    }

    /**
     * Get entity info with specific setting.
     *
     * @param entityInfo the entity to transform.
     * @param world      world the entity is in.
     * @param pos        the position it is at.
     * @param settings   the settings.
     * @return the entity info aray.
     */
    public Template.EntityInfo transformEntityInfoWithSettings(final Template.EntityInfo entityInfo, final World world, final BlockPos pos, final PlacementSettings settings)
    {
        final Entity finalEntity = EntityList.createEntityFromNBT(entityInfo.entityData, world);

        //err might be here? only use pos? or don't add?
        final Vec3d entityVec = Structure.transformedVec3d(settings, entityInfo.pos).add(new Vec3d(pos));

        if (finalEntity != null)
        {
            finalEntity.prevRotationYaw = (float) (finalEntity.getMirroredYaw(settings.getMirror()) - NINETY_DEGREES);
            final double rotationYaw
              = finalEntity.getMirroredYaw(settings.getMirror()) + ((double) finalEntity.rotationYaw - (double) finalEntity.getRotatedYaw(settings.getRotation()));

            finalEntity.setLocationAndAngles(entityVec.x, entityVec.y, entityVec.z,
              (float) rotationYaw, finalEntity.rotationPitch);

            final NBTTagCompound nbttagcompound = new NBTTagCompound();
            finalEntity.writeToNBTOptional(nbttagcompound);
            return new Template.EntityInfo(entityInfo.pos, entityInfo.blockPos, nbttagcompound);
        }

        return null;
    }

    /**
     * Transform a Vec3d with placement settings.
     *
     * @param settings the settings.
     * @param vec      the vector.
     * @return the new vector.
     */
    public static Vec3d transformedVec3d(final PlacementSettings settings, final Vec3d vec)
    {
        final Mirror mirrorIn = settings.getMirror();
        final Rotation rotationIn = settings.getRotation();
        double xCoord = vec.x;
        final double yCoord = vec.y;
        double zCoord = vec.z;
        boolean flag = true;

        switch (mirrorIn)
        {
            case LEFT_RIGHT:
                zCoord = 1.0D - zCoord;
                break;
            case FRONT_BACK:
                xCoord = 1.0D - xCoord;
                break;
            default:
                flag = false;
        }

        switch (rotationIn)
        {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(zCoord, yCoord, 1.0D - xCoord);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - zCoord, yCoord, xCoord);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - xCoord, yCoord, 1.0D - zCoord);
            default:
                return flag ? new Vec3d(xCoord, yCoord, zCoord) : vec;
        }
    }

    /**
     * Get all additional entities.
     *
     * @return list of entities.
     */
    public List<Template.EntityInfo> getTileEntities()
    {
        return this.template.entities;
    }

    /**
     * Get the Placement settings of the structure.
     *
     * @return the settings.
     */
    public PlacementSettings getSettings()
    {
        return this.settings;
    }
}
