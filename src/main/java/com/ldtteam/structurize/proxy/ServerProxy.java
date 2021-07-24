package com.ldtteam.structurize.proxy;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

import java.io.File;

/**
 * Proxy to the server.
 */
public class ServerProxy implements IProxy
{
    @Override
    public File getSchematicsFolder()
    {
        return new File(ServerLifecycleHooks.getCurrentServer().getServerDirectory() + "/" + Constants.MOD_ID);
    }
}
