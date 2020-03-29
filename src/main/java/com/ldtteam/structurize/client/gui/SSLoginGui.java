package com.ldtteam.structurize.client.gui;

import com.ldtteam.blockout.controls.ButtonVanilla;
import com.ldtteam.blockout.controls.TextFieldVanilla;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.schemaserver.LoginHolder;

public class SSLoginGui extends Window
{
    public SSLoginGui()
    {
        super(Constants.MOD_ID + ":gui/sslogingui.xml");
        this.findPaneOfTypeByID("login", ButtonVanilla.class).setHandler(button -> {
            LoginHolder.login(this.findPaneOfTypeByID("username", TextFieldVanilla.class).getText(),
                this.findPaneOfTypeByID("password", TextFieldVanilla.class).getText());
        });
    }
}
