package com.ldtteam.structurize.client.gui.util;

import com.ldtteam.blockui.controls.TextField;

public class InputFilters
{
    public static TextField.Filter ONLY_NUMBERS = new TextField.Filter()
    {
        @Override
        public String filter(final String s)
        {
            return s;
        }

        @Override
        public boolean isAllowedCharacter(final char c)
        {
            return Character.isDigit(c) || c == '-' || c == '.';
        }
    };

    public static TextField.Filter ONLY_POSITIVE_NUMBERS_MAX1k = new TextField.Filter()
    {
        @Override
        public String filter(final String s)
        {
            try
            {
                int i = Integer.parseInt(s);
                if (i > 0)
                {
                    if (i > 999)
                    {
                        i = 999;
                    }

                    return String.valueOf(i);
                }
            }
            catch (Exception ignored)
            {
            }
            return "";
        }

        @Override
        public boolean isAllowedCharacter(final char c)
        {
            return Character.isDigit(c);
        }
    };

    public static TextField.Filter PERCENT = new TextField.Filter()
    {
        @Override
        public String filter(final String s)
        {
            try
            {
                int i = Integer.parseInt(s);
                if (i >= 0)
                {
                    if (i > 100)
                    {
                        i = 100;
                    }

                    return String.valueOf(i);
                }
            }
            catch (Exception ignored)
            {
            }
            return "0";
        }

        @Override
        public boolean isAllowedCharacter(final char c)
        {
            return Character.isDigit(c);
        }
    };
}
