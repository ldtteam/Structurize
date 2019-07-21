package com.ldtteam.structurize.proxy;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;

/**
 * Proxy to the server.
 */
public class ServerProxy extends CommonProxy
{
    @Override
    public File getSchematicsFolder()
    {
        return new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory() + "/" + Constants.MOD_ID);
    }
}
