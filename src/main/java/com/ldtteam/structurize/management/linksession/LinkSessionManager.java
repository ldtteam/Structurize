package com.ldtteam.structurize.management.linksession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Storage and manager for all LinkSessions
 */
public class LinkSessionManager implements INBTSerializable<NBTTagCompound>
{
    /**
     * Storage of sessions
     */
    private static final HashMap<UUID, LinkSession> sessions = new HashMap<UUID, LinkSession>();

    public LinkSessionManager()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Creates a session
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     */
    public void createSession(@NotNull final UUID ownerUUID)
    {
        sessions.put(ownerUUID, new LinkSession());
    }

    /**
     * Removes a session from session list
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean destroySession(@NotNull final UUID ownerUUID)
    {
        return sessions.remove(ownerUUID) == null ? false : true;
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @return List<UUID>: list of UUIDs of session members
     * <p>     null:       if ownerUUID was not found
     */
    @Nullable
    public List<UUID> getMembersOf(@NotNull final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? null : sessions.get(ownerUUID).getMembersUUID();
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @return List<String>: list of player's name (nickname) of session members
     * <p>     null:         if ownerUUID was not found
     */
    @Nullable
    public List<String> getMembersNamesOf(@NotNull final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? null : sessions.get(ownerUUID).getMembersDisplayNames();
    }

    /**
     * Getter for session names of all sessions identified by member
     * 
     * @param memberUUID {@link EntityPlayer#getUniqueID()}
     * @return List<String>: list of player's name (nickname) of session members
     * <p>     null: if ownerUUID was not found
     */
    @Nullable
    public List<String> getSessionNamesOf(@NotNull final UUID memberUUID)
    {
        final List<String> ses = sessions.entrySet().stream()
            .filter(en -> en.getValue().isMember(memberUUID) && !en.getKey().equals(memberUUID))
            .map(en -> en.getValue().getMemberName(en.getKey()))
            .collect(Collectors.toList());
        return ses.isEmpty() ? null : ses;
    }

    /**
     * Getter for unique members of all sessions identified by member
     * 
     * @param memberUUID {@link EntityPlayer#getUniqueID()}
     * @return Set<UUID>: list of UUIDs of session members
     */
    public Set<UUID> getUniquePlayersInSessionsOf(@NotNull final UUID memberUUID)
    {
        final Set<UUID> uniqueMembers = new HashSet<UUID>();
        sessions.entrySet().stream()
            .filter(en -> en.getValue().isMember(memberUUID))
            .map(en -> en.getKey())
            .forEach(uuid -> uniqueMembers.addAll(getMembersOf(uuid)));
        return uniqueMembers;
    }

    /**
     * Adds or updates a member in a session identified by owner
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @param memberUUID {@link EntityPlayer#getUniqueID()}
     * @param displayName string used in commands for displaying player's name
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean addOrUpdateMemberInSession(@NotNull final UUID ownerUUID, @NotNull final UUID memberUUID, @Nullable final String displayName)
    {
        if(sessions.containsKey(ownerUUID))
        {
            sessions.get(ownerUUID).addOrUpdateMember(memberUUID, displayName);
            return true;
        }
        return false;
    }

    /**
     * Removes a member from a session identified by owner
     * 
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @param ownerUUID {@link EntityPlayer#getUniqueID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean removeMemberOfSession(@NotNull final UUID ownerUUID, @NotNull final UUID memberUUID)
    {
        if(sessions.containsKey(ownerUUID))
        {
            sessions.get(ownerUUID).removeMember(memberUUID);
            return true;
        }
        return false;
    }

    /**
     * Serializale into NBTTagCompound
     * 
     * @return NBTTagCompound: representing storage of this manager
     */
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound out = new NBTTagCompound();
        sessions.forEach((uuid, ls) -> out.setTag(uuid.toString(), ls.writeToNBT()));
        return out;
    }

    /**
     * Deserializale from NBTTagCompound
     * 
     * @param in NBTTagCompound to deserialize
     */
    public void deserializeNBT(@NotNull final NBTTagCompound in)
    {
        for(String key : in.getKeySet())
        {
            sessions.put(UUID.fromString(key), LinkSession.createFromNBT(in.getCompoundTag(key)));
        }
    }
}