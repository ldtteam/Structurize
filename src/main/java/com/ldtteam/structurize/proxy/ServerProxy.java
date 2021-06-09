package com.ldtteam.structurize.proxy;

import java.io.File;
import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
