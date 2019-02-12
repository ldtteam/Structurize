package com.ldtteam.structurize.management.linksession;

import java.util.HashMap;
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
    private static final String CHANNELS_TAG = "channels";

    /**
     * Instance
     */
    public  static final LinkSessionManager INSTANCE = new LinkSessionManager();

    /**
     * Storage of sessions by ownerUUID
     */
    private static final HashMap<UUID, LinkSession> sessions = new HashMap<UUID, LinkSession>();

    /**
     * Storage of muted channels by playerUUID
     */
    private static final HashMap<UUID, HashMap<Integer, Boolean>> channels = new HashMap<UUID, HashMap<Integer, Boolean>>();

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
     * Getter for unique members (which are not muted in a channel) of all sessions identified by member <p>
     * Old name: <code>getUniquePlayersInSessionsOf</code>
     * 
     * @param memberUUID {@link EntityPlayer#getUniqueID()}
     * @return Set<UUID>: list of UUIDs of session members
     */
    public Set<UUID> execute(@NotNull final UUID memberUUID, @NotNull final ChannelsEnum channel)
    {
        return sessions.entrySet().stream()
            .filter(en -> en.getValue().isMember(memberUUID))
            .flatMap(en -> getMembersOf(en.getKey()).stream())
            .filter(uuid -> !getMuteState(uuid, channel))
            .collect(Collectors.toSet());
    }

    /**
     * Adds or updates a member in a session identified by owner
     * 
     * @param ownerUUID   {@link EntityPlayer#getUniqueID()}
     * @param memberUUID  {@link EntityPlayer#getUniqueID()}
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
     * @param ownerUUID  {@link EntityPlayer#getUniqueID()}
     * @param memberUUID {@link EntityPlayer#getUniqueID()}
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
     * Setter of channel mute state identified by player
     * 
     * @param playerUUID {@link EntityPlayer#getUniqueID()}
     * @param channel    {@link ChannelsEnum}
     * @param state      true = muted, false = unmuted
     */
    public void setMuteState(@NotNull final UUID playerUUID, @NotNull final ChannelsEnum channel, @NotNull final boolean state)
    {
        if(!channels.containsKey(playerUUID))
        {
            channels.put(playerUUID, new HashMap<Integer, Boolean>());
        }
        channels.get(playerUUID).put(channel.getID(), state);
    }

    /**
     * Getter for channel mute state identified by player
     * 
     * @param playerUUID {@link EntityPlayer#getUniqueID()}
     * @param channel    {@link ChannelsEnum}
     */
    public boolean getMuteState(@NotNull final UUID playerUUID, @NotNull final ChannelsEnum channel)
    {
        if (channels.containsKey(playerUUID) && channels.get(playerUUID).containsKey(channel.getID()))
        {
            return channels.get(playerUUID).get(channel.getID());
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
        final NBTTagCompound channelz = new NBTTagCompound();

        sessions.forEach((uuid, ls) -> out.setTag(uuid.toString(), ls.writeToNBT()));
        channels.forEach((uuid, ch) -> {
            final NBTTagCompound player = new NBTTagCompound();
            
            ch.forEach((id, state) -> player.setBoolean(String.valueOf(id), state));
            channelz.setTag(uuid.toString(), player);
        });
        out.setTag(CHANNELS_TAG, channelz);
        return out;
    }

    /**
     * Deserializale from NBTTagCompound
     * 
     * @param in NBTTagCompound to deserialize
     */
    public void deserializeNBT(@NotNull final NBTTagCompound in)
    {
        if (in.hasKey(CHANNELS_TAG))
        {
            final NBTTagCompound channelz = in.getCompoundTag(CHANNELS_TAG);
            for (String key : channelz.getKeySet())
            {
                final NBTTagCompound playerTag = channelz.getCompoundTag(key);
                final UUID playerUUID = UUID.fromString(key);

                channels.put(playerUUID, new HashMap<Integer, Boolean>());
                for (String id : playerTag.getKeySet())
                {
                    channels.get(playerUUID).put(new Integer(id), playerTag.getBoolean(id));
                }
            }
            in.removeTag(CHANNELS_TAG);
        }
        for(String key : in.getKeySet())
        {
            sessions.put(UUID.fromString(key), LinkSession.createFromNBT(in.getCompoundTag(key)));
        }
    }
}