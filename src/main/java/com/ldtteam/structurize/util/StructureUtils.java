package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.Log;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.ldtteam.structurize.api.util.constant.Constants.BUFFER_SIZE;

/**
 * Utility methods for structures.
 */
public final class StructureUtils
{
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Private constructor to hide public one.
     */
    private StructureUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Calculate the MD5 hash of a byte array
     *
     * @param bytes array
     * @return the MD5 hash string or null
     */
    public static String calculateMD5(final byte[] bytes)
    {
        if (bytes == null || bytes.length == 0)
        {
            return null;
        }

        try
        {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            return bytesToHex(md.digest(bytes));
        }
        catch (@NotNull final NoSuchAlgorithmException e)
        {
            Log.getLogger().trace(e);
        }

        return null;
    }

    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] compress(final byte[] data)
    {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
        try (
            GZIPOutputStream zipStream = new GZIPOutputStream(byteStream))
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
        try (
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
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
     * Calculate the MD5 hash for a blueprint from an inputstream.
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
        return calculateMD5(StructureLoadingUtils.getStreamAsByteArray(stream));
    }

    /**
     * Transform a Vec3d with placement settings.
     *
     * @param settings the settings.
     * @param vec      the vector.
     * @return the new vector.
     */
    public static Vec3 transformedVector3d(final PlacementSettings settings, final Vec3 vec)
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
                return new Vec3(zCoord, yCoord, 1.0D - xCoord);
            case CLOCKWISE_90:
                return new Vec3(1.0D - zCoord, yCoord, xCoord);
            case CLOCKWISE_180:
                return new Vec3(1.0D - xCoord, yCoord, 1.0D - zCoord);
            default:
                return flag ? new Vec3(xCoord, yCoord, zCoord) : vec;
        }
    }
}
