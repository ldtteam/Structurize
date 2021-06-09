package com.ldtteam.structurize.management.linksession;

import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Instance of one session
 */
public class LinkSession
{
    /**
     * Members of session
     */
    private final HashMap<UUID, String> members = new HashMap<UUID, String>();

    protected LinkSession()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Adds or updates a member and it's display name in this session
     * 
     * @param memberUUID {@link net.minecraft.entity.player.PlayerEntity#getUniqueID() PlayerEntity#getUniqueID()}
     * @param displayName string used in commands for displaying player's name
     */
    protected void addOrUpdateMember(@NotNull final UUID memberUUID, @Nullable String displayName)
    {
        displayName = (displayName == null) ? "null" : displayName;
        if(!members.containsKey(memberUUID))
        {
            members.put(memberUUID, displayName);
        }
        else
        {
            members.replace(memberUUID, displayName);
        }
    }

    /**
     * Removes a member from this session
     * 
     * @param memberUUID {@link net.minecraft.entity.player.PlayerEntity#getUniqueID() PlayerEntity#getUniqueID()}
     */
    protected void removeMember(@NotNull final UUID memberUUID)
    {
        members.remove(memberUUID);
    }

    /**
     * Checker whether a player is a member of this session
     * 
     * @param memberUUID {@link net.minecraft.entity.player.PlayerEntity#getUniqueID() PlayerEntity#getUniqueID()}
     * @return boolean: whether memberUUID is or not a member of this session
     */
    protected boolean isMember(@NotNull final UUID memberUUID)
    {
        return members.get(memberUUID) != null;
    }

    /**
     * Getter for player's name (nickname)
     * 
     * @param memberUUID {@link net.minecraft.entity.player.PlayerEntity#getUniqueID() PlayerEntity#getUniqueID()}
     * @return String: player's name in readable form (nickname)
     */
    @Nullable
    protected String getMemberDisplayName(@NotNull final UUID memberUUID)
    {
        return members.get(memberUUID);
    }

    /**
     * Getter for all players of this session by UUID
     * 
     * @return List UUID: list of UUIDs of session members
     */
    protected List<UUID> getMembersUUID()
    {
        return members.keySet().stream().collect(Collectors.toList());
    }

    /**
     * Getter for all players of this session by player's name
     * 
     * @return List String: list of player's name (nickname) of session members
     */
    protected List<String> getMembersDisplayNames()
    {
        return members.values().stream().collect(Collectors.toList());
    }

    /**
     * Serializale into CompoundNBT
     * 
     * @return CompoundNBT: representing this session
     */
    protected CompoundNBT writeToNBT()
    {
        final CompoundNBT data = new CompoundNBT();
        members.forEach((uuid, name) -> data.putString(uuid.toString(), name));
        return data;
    }

    /**
     * Creates a new session by deserializing CompoundNBT
     * 
     * @param in CompoundNBT to deserialize
     * @return LinkSession: representing a new session
     */
    protected static LinkSession createFromNBT(@NotNull final CompoundNBT in)
    {
        final LinkSession ls = new LinkSession();
        for(String key : in.getAllKeys())
        {
            ls.addOrUpdateMember(UUID.fromString(key), in.getString(key));
        }
        return ls;
    }
}