package com.ldtteam.structurize.management.linksession;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Instance of one session
 */
public class LinkSession
{
    /**
     * Members of session
     */
    private final HashMap<UUID, String> members = new HashMap<>();

    protected LinkSession()
    {
    }

    /**
     * Adds or updates a member and it's display name in this session
     * 
     * @param memberUUID {@link Player#getUUID()}
     * @param displayName string used in commands for displaying player's name
     */
    protected void addOrUpdateMember(final UUID memberUUID, @Nullable String displayName)
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
     * @param memberUUID {@link Player#getUUID()}
     */
    protected void removeMember(final UUID memberUUID)
    {
        members.remove(memberUUID);
    }

    /**
     * Checker whether a player is a member of this session
     * 
     * @param memberUUID {@link Player#getUUID()}
     * @return boolean: whether memberUUID is or not a member of this session
     */
    protected boolean isMember(final UUID memberUUID)
    {
        return members.get(memberUUID) != null;
    }

    /**
     * Getter for player's name (nickname)
     * 
     * @param memberUUID {@link Player#getUUID()}
     * @return String: player's name in readable form (nickname)
     */
    @Nullable
    protected String getMemberDisplayName(final UUID memberUUID)
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
        return new ArrayList<>(members.keySet());
    }

    /**
     * Getter for all players of this session by player's name
     * 
     * @return List String: list of player's name (nickname) of session members
     */
    protected List<String> getMembersDisplayNames()
    {
        return new ArrayList<>(members.values());
    }

    /**
     * Serializale into CompoundNBT
     * 
     * @return CompoundNBT: representing this session
     */
    protected CompoundTag writeToNBT()
    {
        final CompoundTag data = new CompoundTag();
        members.forEach((uuid, name) -> data.putString(uuid.toString(), name));
        return data;
    }

    /**
     * Creates a new session by deserializing CompoundNBT
     * 
     * @param in CompoundNBT to deserialize
     * @return LinkSession: representing a new session
     */
    protected static LinkSession createFromNBT(final CompoundTag in)
    {
        final LinkSession ls = new LinkSession();
        for(String key : in.getAllKeys())
        {
            ls.addOrUpdateMember(UUID.fromString(key), in.getString(key));
        }
        return ls;
    }
}