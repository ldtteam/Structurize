package com.ldtteam.structurize.client.gui.schemaserver;

import com.ldtteam.blockout.controls.ButtonVanilla;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.controls.TextFieldVanilla;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.schemaserver.LoginHolder;
import com.ldtteam.structurize.util.LanguageHandler;

public class LoginGui extends Window
{
    public LoginGui()
    {
        super(Constants.MOD_ID + ":gui/schemaserver/login.xml");

        findPaneOfTypeByID("login", ButtonVanilla.class).setHandler(button -> {
            final String username = this.findPaneOfTypeByID("username", TextFieldVanilla.class).getText();
            LoginHolder.INSTANCE.login(username,
                this.findPaneOfTypeByID("password", TextFieldVanilla.class).getText(),
                (res, rea) -> this.loginCallback(res, rea, username));
        });
        findPaneOfTypeByID("username", TextFieldVanilla.class).setFocus();
    }

    private void loginCallback(final boolean result, final String reason, final String username)
    {
        if (result)
        {
            Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.sslogin.success", username));
            close();
        }
        else
        {
            findPaneOfTypeByID("notification", Label.class)
                .setLabelText(LanguageHandler.prepareMessage("structurize.sslogin.login_error", reason).getString());
        }
    }
}
