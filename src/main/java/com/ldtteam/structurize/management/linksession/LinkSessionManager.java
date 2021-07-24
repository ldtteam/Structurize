package com.ldtteam.structurize.management.linksession;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Storage and manager for all LinkSessions
 */
public class LinkSessionManager implements INBTSerializable<CompoundTag>
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
     * @param ownerUUID {@link Player#getUUID()}
     */
    public void createSession(final UUID ownerUUID)
    {
        sessions.put(ownerUUID, new LinkSession());
    }

    /**
     * Removes a session from session list
     * 
     * @param ownerUUID {@link Player#getUUID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean destroySession(final UUID ownerUUID)
    {
        return sessions.remove(ownerUUID) != null;
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link Player#getUUID()}
     * @return List UUID: list of UUIDs of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    public List<UUID> getMembersOf(final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? Collections.emptyList() : sessions.get(ownerUUID).getMembersUUID();
    }

    /**
     * Getter for members of a session identified by owner
     * 
     * @param ownerUUID {@link Player#getUUID()}
     * @return List String: list of player's name (nickname) of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    public List<String> getMembersNamesOf(final UUID ownerUUID)
    {
        return sessions.get(ownerUUID) == null ? Collections.emptyList() : sessions.get(ownerUUID).getMembersDisplayNames();
    }

    /**
     * Getter for session names of all sessions identified by member
     * 
     * @param memberUUID {@link Player#getUUID()}
     * @return List String: list of player's name (nickname) of session members
     *         <p>
     *         empty list: if ownerUUID was not found
     */
    public List<String> getSessionNamesOf(final UUID memberUUID)
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
     * @param memberUUID {@link Player#getUUID()}
     * @param channel    {@link ChannelsEnum}
     * @return Set UUID: list of UUIDs of session members
     */
    public Set<UUID> execute(final UUID memberUUID, final ChannelsEnum channel)
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
     * @param ownerUUID   {@link Player#getUUID()}
     * @param memberUUID  {@link Player#getUUID()}
     * @param displayName string used in commands for displaying player's name
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean addOrUpdateMemberInSession(final UUID ownerUUID, final UUID memberUUID, @Nullable final String displayName)
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
     * @param ownerUUID  {@link Player#getUUID()}
     * @param memberUUID {@link Player#getUUID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean removeMemberOfSession(final UUID ownerUUID, final UUID memberUUID)
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
     * @param playerUUID {@link Player#getUUID()}
     * @param channel    {@link ChannelsEnum}
     * @param state      true = muted, false = unmuted
     */
    public void setMuteState(final UUID playerUUID, final ChannelsEnum channel, final boolean state)
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
     * @param playerUUID {@link Player#getUUID()}
     * @param channel    {@link ChannelsEnum}
     * @return boolean: mute state (returns false if checking fails), true = muted, false = unmuted
     */
    public boolean getMuteState(final UUID playerUUID, final ChannelsEnum channel)
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
    public CompoundTag serializeNBT()
    {
        final CompoundTag out = new CompoundTag();
        final CompoundTag channelz = new CompoundTag();

        sessions.forEach((uuid, ls) -> out.put(uuid.toString(), ls.writeToNBT()));
        channels.forEach((uuid, ch) -> {
            final CompoundTag player = new CompoundTag();

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
    public void deserializeNBT(final CompoundTag in)
    {
        reset();

        final CompoundTag channelz = in.getCompound(CHANNELS_TAG);
        for (String key : channelz.getAllKeys())
        {
            final CompoundTag playerTag = channelz.getCompound(key);
            final UUID playerUUID = UUID.fromString(key);

            channels.put(playerUUID, new HashMap<Integer, Boolean>());
            for (String id : playerTag.getAllKeys())
            {
                channels.get(playerUUID).put(Integer.valueOf(id), playerTag.getBoolean(id));
            }
        }
        in.remove(CHANNELS_TAG);

        for (String key : in.getAllKeys())
        {
            sessions.put(UUID.fromString(key), LinkSession.createFromNBT(in.getCompound(key)));
        }
    }

    /**
     * Adds or replaces an invite for given player
     * 
     * @param playerUUID {@link Player#getUUID()}
     * @param ownerUUID  {@link Player#getUUID()}
     * @return boolean: whether ownerUUID was found or not
     */
    public boolean createInvite(final UUID playerUUID, final UUID ownerUUID)
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
     * @param playerUUID {@link Player#getUUID()}
     * @return String: display name of an owner of invite's target session
     *         <p>
     *         null: if player has not an open invite
     */
    @Nullable
    public String hasInvite(final UUID playerUUID)
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
     * @param playerUUID {@link Player#getUUID()}
     * @param playerName string used in commands for displaying player's name
     * @return String: owner's display name of playerUUID session
     *         <p>
     *         null: if playerUUID was not found or session no longer exists
     */
    @Nullable
    public String consumeInvite(final UUID playerUUID, @Nullable final String playerName)
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
     * @param playerUUID  {@link Player#getUUID()}
     * @param playerName  string used in commands for displaying player's name
     * @param UUIDtoCheck UUID to check against current invite
     * @return String: owner's display name of playerUUID session
     *         <p>
     *         null: if UUIDtoCheck does not match or if playerUUID was not found or session no longer exists
     */
    @Nullable
    public String consumeInviteWithCheck(final UUID playerUUID, @Nullable final String playerName, final UUID UUIDtoCheck)
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