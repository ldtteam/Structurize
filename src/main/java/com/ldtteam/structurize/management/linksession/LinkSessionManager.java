package com.ldtteam.structurize.management.linksession;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Storage and manager for all LinkSessions
 */
public class LinkSessionManager implements INBTSerializable<CompoundNBT>
{
    private static final String CHANNELS_TAG = "channels";

    /**
     * Instance
     */
    public static final LinkSessionManager INSTANCE = new LinkSessionManager();

    /**
     * Storage of sessions by ownerUUID
     */
    private final HashMap<UUID, LinkSession> sessions = new HashMap<>();

    /**
     * Storage of muted channels by playerUUID
     */
    private final HashMap<UUID, HashMap<Integer, Boolean>> channels = new HashMap<>();

    /**
     * Storage of invites by playerUUID
     */
    private final HashMap<UUID, UUID> invites = new HashMap<>();

    public LinkSessionManager()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Creates a session
     * 
     * @param ownerUUID {@link PlayerEntity#getUniqueID()}
     */
    public void createSession(@NotNull final UUID ownerUUID)
    {
        sessions.put(ownerUUID, new LinkSession());
    }

    /**
     * Removes a session from session list
     * 
     * @param ownerUUID {@link PlayerEntity#getUniqueID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean destroySession(@NotNull final UUID ownerUUID)
    {
        return sessions.remove(ownerUUID) != null;
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link PlayerEntity#getUniqueID()}
     * @return List UUID: list of UUIDs of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    @Nullable
    public List<UUID> getMembersOf(@NotNull final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? Collections.emptyList() : sessions.get(ownerUUID).getMembersUUID();
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link PlayerEntity#getUniqueID()}
     * @return List String: list of player's name (nickname) of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    @Nullable
    public List<String> getMembersNamesOf(@NotNull final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? Collections.emptyList() : sessions.get(ownerUUID).getMembersDisplayNames();
    }

    /**
     * Getter for session names of all sessions identified by member
     * 
     * @param memberUUID {@link PlayerEntity#getUniqueID()}
     * @return List String: list of player's name (nickname) of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    @Nullable
    public List<String> getSessionNamesOf(@NotNull final UUID memberUUID)
    {
        final List<String> ses = sessions.entrySet()
            .stream()
            .filter(en -> en.getValue().isMember(memberUUID))// && !en.getKey().equals(memberUUID))
            .map(en -> en.getValue().getMemberDisplayName(en.getKey()))
            .collect(Collectors.toList());
        return ses.isEmpty() ? Collections.emptyList() : ses;
    }

    /**
     * Getter for unique members (which are not muted in a channel) of all sessions identified by member
     * <p>
     * Old name: <code>getUniquePlayersInSessionsOf</code>
     * 
     * @param memberUUID {@link PlayerEntity#getUniqueID()}
     * @param channel    {@link ChannelsEnum}
     * @return Set UUID: list of UUIDs of session members
     */
    public Set<UUID> execute(@NotNull final UUID memberUUID, @NotNull final ChannelsEnum channel)
    {
        return sessions.entrySet()
            .stream()
            .filter(en -> en.getValue().isMember(memberUUID))
            .flatMap(en -> getMembersOf(en.getKey()).stream())
            .filter(uuid -> !getMuteState(uuid, channel))
            .collect(Collectors.toSet());
    }

    /**
     * Adds or updates a member in a session identified by owner
     * 
     * @param ownerUUID   {@link PlayerEntity#getUniqueID()}
     * @param memberUUID  {@link PlayerEntity#getUniqueID()}
     * @param displayName string used in commands for displaying player's name
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean addOrUpdateMemberInSession(@NotNull final UUID ownerUUID, @NotNull final UUID memberUUID, @Nullable final String displayName)
    {
        if (sessions.containsKey(ownerUUID))
        {
            sessions.get(ownerUUID).addOrUpdateMember(memberUUID, displayName);
            return true;
        }
        return false;
    }

    /**
     * Removes a member from a session identified by owner
     * 
     * @param ownerUUID  {@link PlayerEntity#getUniqueID()}
     * @param memberUUID {@link PlayerEntity#getUniqueID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean removeMemberOfSession(@NotNull final UUID ownerUUID, @NotNull final UUID memberUUID)
    {
        if (sessions.containsKey(ownerUUID))
        {
            sessions.get(ownerUUID).removeMember(memberUUID);
            return true;
        }
        return false;
    }

    /**
     * Setter of channel mute state identified by player
     * 
     * @param playerUUID {@link PlayerEntity#getUniqueID()}
     * @param channel    {@link ChannelsEnum}
     * @param state      true = muted, false = unmuted
     */
    public void setMuteState(@NotNull final UUID playerUUID, @NotNull final ChannelsEnum channel, @NotNull final boolean state)
    {
        if (!channels.containsKey(playerUUID))
        {
            channels.put(playerUUID, new HashMap<Integer, Boolean>());
        }
        channels.get(playerUUID).put(channel.getID(), state);
    }

    /**
     * Getter for channel mute state identified by player
     * 
     * @param playerUUID {@link PlayerEntity#getUniqueID()}
     * @param channel    {@link ChannelsEnum}
     * @return boolean: mute state (returns false if checking fails), true = muted, false = unmuted
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
     * Serializale into CompoundNBT
     * 
     * @return CompoundNBT: representing storage of this manager
     */
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT out = new CompoundNBT();
        final CompoundNBT channelz = new CompoundNBT();

        sessions.forEach((uuid, ls) -> out.put(uuid.toString(), ls.writeToNBT()));
        channels.forEach((uuid, ch) -> {
            final CompoundNBT player = new CompoundNBT();

            ch.forEach((id, state) -> player.putBoolean(String.valueOf(id), state));
            channelz.put(uuid.toString(), player);
        });
        out.put(CHANNELS_TAG, channelz);
        return out;
    }

    /**
     * Deserializale from CompoundNBT
     * 
     * @param in CompoundNBT to deserialize
     */
    public void deserializeNBT(@NotNull final CompoundNBT in)
    {
        reset();

        final CompoundNBT channelz = in.getCompound(CHANNELS_TAG);
        for (String key : channelz.keySet())
        {
            final CompoundNBT playerTag = channelz.getCompound(key);
            final UUID playerUUID = UUID.fromString(key);

            channels.put(playerUUID, new HashMap<Integer, Boolean>());
            for (String id : playerTag.keySet())
            {
                channels.get(playerUUID).put(Integer.valueOf(id), playerTag.getBoolean(id));
            }
        }
        in.remove(CHANNELS_TAG);

        for (String key : in.keySet())
        {
            sessions.put(UUID.fromString(key), LinkSession.createFromNBT(in.getCompound(key)));
        }
    }

    /**
     * Adds or replaces an invite for given player
     * 
     * @param playerUUID {@link PlayerEntity#getUniqueID()}
     * @param ownerUUID  {@link PlayerEntity#getUniqueID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean createInvite(@NotNull final UUID playerUUID, @NotNull final UUID ownerUUID)
    {
        if (sessions.containsKey(ownerUUID))
        {
            invites.put(playerUUID, ownerUUID);
            return true;
        }
        return false;
    }

    /**
     * Check if a player has open invite
     * 
     * @param playerUUID {@link PlayerEntity#getUniqueID()}
     * @return String: display name of an owner of invite's target session
     *         <p>
     *         null: if player has not an open invite
     */
    @Nullable
    public String hasInvite(@NotNull final UUID playerUUID)
    {
        if (invites.containsKey(playerUUID))
        {
            final UUID ownerUUID = invites.get(playerUUID);
            if (sessions.containsKey(ownerUUID))
            {
                return sessions.get(ownerUUID).getMemberDisplayName(ownerUUID);
            }
            else
            {
                invites.remove(playerUUID);
            }
        }
        return null;
    }

    /**
     * Comsumes player's invites and makes him a member in appropriate session
     * 
     * @param playerUUID {@link PlayerEntity#getUniqueID()}
     * @param playerName string used in commands for displaying player's name
     * @return String: owner's display name of playerUUID session
     *         <p>
     *         null: if playerUUID was not found or session no longer exists
     */
    @Nullable
    public String consumeInvite(@NotNull final UUID playerUUID, @Nullable final String playerName)
    {
        if (!invites.containsKey(playerUUID))
        {
            return null;
        }

        final UUID ownerUUID = invites.get(playerUUID);
        invites.remove(playerUUID);

        if (!addOrUpdateMemberInSession(ownerUUID, playerUUID, playerName))
        {
            return null;
        }
        return sessions.get(ownerUUID).getMemberDisplayName(ownerUUID);
    }

    /**
     * Comsumes player's invites and makes him a member in appropriate session
     * 
     * @param playerUUID  {@link PlayerEntity#getUniqueID()}
     * @param playerName  string used in commands for displaying player's name
     * @param UUIDtoCheck UUID to check against current invite
     * @return String: owner's display name of playerUUID session
     *         <p>
     *         null: if UUIDtoCheck does not match or if playerUUID was not found or session no longer exists
     */
    @Nullable
    public String consumeInviteWithCheck(@NotNull final UUID playerUUID, @Nullable final String playerName, @NotNull final UUID UUIDtoCheck)
    {
        if (invites.containsKey(playerUUID) && invites.get(playerUUID).equals(UUIDtoCheck))
        {
            return consumeInvite(playerUUID, playerName);
        }
        return null;
    }

    /**
     * Clears every storage of this manager
     */
    private void reset()
    {
        channels.clear();
        sessions.clear();
        invites.clear();
    }
}