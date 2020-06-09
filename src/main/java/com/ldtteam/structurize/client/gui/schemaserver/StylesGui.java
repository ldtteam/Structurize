package com.ldtteam.structurize.client.gui.schemaserver;

import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.api.util.constant.Constants;

public class StylesGui extends Window
{
    private SwitchView pages;

    public StylesGui()
    {
        super(Constants.MOD_ID + ":gui/schemaserver/styles.xml");

        this.pages = findPaneOfTypeByID("pages", SwitchView.class);
        pages.setView("styles");
    }
}
