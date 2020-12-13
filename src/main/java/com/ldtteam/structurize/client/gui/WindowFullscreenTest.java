package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.api.util.constant.Constants;

public class WindowFullscreenTest extends AbstractWindowSkeleton
{
    private static final String XML_LOC = ":gui/test.xml";

    public WindowFullscreenTest()
    {
        super(Constants.MOD_ID + XML_LOC);
        findPaneOfTypeByID("testList", ScrollingList.class).setDataProvider(() -> 10, (id, pane) -> {});
    }
}
