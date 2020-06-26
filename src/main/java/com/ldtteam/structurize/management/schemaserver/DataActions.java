package com.ldtteam.structurize.management.schemaserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import com.ldtteam.server.schematics.api.ExtendedScanApi;
import com.ldtteam.server.schematics.models.ScanCreationModel;
import com.ldtteam.server.schematics.models.ScanDataCreationModel;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.gui.schemaserver.FileDiffGui;
import com.ldtteam.structurize.management.schemaserver.fm.DataHeadFile;
import com.ldtteam.structurize.management.schemaserver.fm.StylesHeadFile;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileTreeSnapshotDiff;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileWalker;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class DataActions
{
    private static Path currentUsernameDir;
    private static DataHeadFile dataHeadFile;
    private static StylesHeadFile stylesHeadFile;
    private static FileTreeSnapshotDiff lastFileTreeSnapshotDiff;

    /**
     * Private constructor to hide implicit public one.
     */
    private DataActions()
    {
    }

    private static boolean runIOOperation(final IORunnable op)
    {
        try
        {
            op.run();
            return true;
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error encoutered during schema server IO operation: " + e.getLocalizedMessage(), e);
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.ssioerror", e.getLocalizedMessage()));
        }
        return false;
    }

    /**
     * Sets up directories inside .minecraft (or actual gameDir) for current user.
     */
    private static boolean createDefaultFolders()
    {
        if (LoginHolder.INSTANCE.isUserLoggedIn())
        {
            currentUsernameDir = Minecraft.getInstance().gameDir.toPath()
                .resolve(Constants.MOD_ID)
                .resolve(LoginHolder.INSTANCE.getCurrentUsername());

            if (runIOOperation(() -> {
                dataHeadFile = DataHeadFile.read(currentUsernameDir);
                stylesHeadFile = StylesHeadFile.read(currentUsernameDir);
                if (!Files.exists(currentUsernameDir.resolve(DataHeadFile.JSON_NAME)))
                {
                    dataHeadFile.write(currentUsernameDir);
                }
                if (!Files.exists(currentUsernameDir.resolve(StylesHeadFile.JSON_NAME)))
                {
                    stylesHeadFile.write(currentUsernameDir);
                }
                Files.createDirectories(dataHeadFile.getDataFolder());
                Files.createDirectories(stylesHeadFile.getStylesFolder());
            }))
            {
                return true;
            }
        }
        currentUsernameDir = null;
        return false;
    }

    private static boolean isPrepared()
    {
        if (currentUsernameDir == null && !createDefaultFolders())
        {
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.ssioerror"));
            return false;
        }
        return true;
    }

    public static void onLogin()
    {
        createDefaultFolders();
    }

    public static void onLogout()
    {
        currentUsernameDir = null;
    }

    public static void setupDataFilesDiff()
    {
        LoginHolder.INSTANCE.runUnuthorized(DataActions::setupDataFilesDiff0, false);
    }

    private static void setupDataFilesDiff0()
    {
        if (!isPrepared())
        {
            return;
        }

        final FileWalker fileWalker = new FileWalker(dataHeadFile.getDataFolder(), dataHeadFile.getFileSnapshot());

        runIOOperation(() -> Files.walkFileTree(dataHeadFile.getDataFolder(), fileWalker));
        lastFileTreeSnapshotDiff = fileWalker.getDiff();
        Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.done"));
    }

    public static void viewLastDataFilesDiff()
    {
        LoginHolder.INSTANCE.runUnuthorized(DataActions::viewLastDataFilesDiff0, false);
    }

    private static void viewLastDataFilesDiff0()
    {
        if (!isPrepared())
        {
            return;
        }

        if (lastFileTreeSnapshotDiff == null)
        {
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.gui.ssfilediff.no_diff"));
            return;
        }

        LoginHolder.INSTANCE.runUnuthorized(() -> {
            new FileDiffGui(dataHeadFile.getDataFolder().toAbsolutePath().toString(), lastFileTreeSnapshotDiff).open();
        }, true);
    }

    public static void uploadLastDataFilesDiff()
    {
        LoginHolder.INSTANCE.runUnuthorized(DataActions::uploadLastDataFilesDiff0, false);
    }

    private static void uploadLastDataFilesDiff0()
    {
        if (!isPrepared())
        {
            return;
        }

        LoginHolder.INSTANCE.runAuthorized(accessToken -> {
            runIOOperation(() -> {
                dataHeadFile.processSnapshotDiff(lastFileTreeSnapshotDiff, (fileDiff, dataEntry) -> {
                    switch (fileDiff.getStatus())
                    {
                        case ADDED:
                            dataEntry.setSchemaServerId(
                                new ExtendedScanApi()
                                    .extendedScanCreatePost(
                                        new ScanCreationModel().name(LoginHolder.INSTANCE.getCurrentUsername() + "_" + fileDiff.getPath())
                                            .visibility(ScanCreationModel.VisibilityEnum.PUBLIC))
                                    .getId());

                        case UPDATED:
                            dataEntry.setLastTimeModified(fileDiff.getNewLastModifiedTime());
                            new ExtendedScanApi().extendedScanDataCreatePost(
                                new ScanDataCreationModel().scanId(dataEntry.getSchemaServerId()).nbtData("read file into bytes"));
                            return dataEntry;

                        case REMOVED:
                            new ExtendedScanApi().extendedScanDeleteIdDelete(dataEntry.getSchemaServerId());
                            return null;

                        default:
                            throw new RuntimeException("Missing file diff type.");
                    }
                });
            });

            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.done"));
        }, errorMessage -> Structurize.proxy.notifyClientOrServerOps(new StringTextComponent(errorMessage)), false);
    }

    @FunctionalInterface
    private static interface IORunnable
    {
        void run() throws IOException;
    }
}
