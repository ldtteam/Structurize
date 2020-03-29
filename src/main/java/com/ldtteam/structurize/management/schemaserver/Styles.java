package com.ldtteam.structurize.management.schemaserver;

public class Styles
{
    private static Styles stylesOfLoggedInUser;

    public static Styles getLoggedInUserStyles()
    {
        return stylesOfLoggedInUser;
    }
}
