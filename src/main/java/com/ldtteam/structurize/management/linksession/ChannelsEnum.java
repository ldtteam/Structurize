package com.ldtteam.structurize.management.linksession;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enums for every channel
 */
public enum ChannelsEnum
{
    COMMAND_MESSAGE(0, "messages"),
    STRUCTURE_DISPLAYER(1, "structures");
    // Add any new channels here, don't change the ids of previous ones

    private final int id;
    private final String commandName;

    private ChannelsEnum(@NotNull final int id, @NotNull final String commandName)
    {
        this.id = id;
        this.commandName = commandName;
    }

    /**
     * @return int: id of this enum
     */
    public int getID()
    {
        return this.id;
    }

    /**
     * @return String: readable command name of this enum
     */
    public String getCommandName()
    {
        return this.commandName;
    }

    /**
     * Getter for enum identified by readable command name
     * 
     * @param commandName string you want to check for
     * @return ChannelsEnum.enum: where enum is an existing enum
     * <p>     null: if nothing match give commandName 
     */
    @Nullable
    public static ChannelsEnum getEnumByCommandName(@NotNull final String commandName)
    {
        for(ChannelsEnum e : ChannelsEnum.values())
        {
            if(e.getCommandName().equals(commandName))
            {
                return e;
            }
        }
        return null;
    }
}