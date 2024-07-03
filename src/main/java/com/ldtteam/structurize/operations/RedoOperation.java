package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Operation for redo.
 */
public class RedoOperation extends BaseOperation
{
    /**
     * The change to redo.
     */
    private final ChangeStorage redoStorage;

    /**
     * Default constructor.
     *
     * @param player      the player to trigger the undo.
     * @param redoStorage the change to redo.
     */
    public RedoOperation(final Player player, final ChangeStorage redoStorage)
    {
        super(new ChangeStorage(Component.translatable("com.ldtteam.structurize.redo", redoStorage.getOperation()), player != null ? player.getUUID() : UUID.randomUUID()));
        this.redoStorage = redoStorage;
        this.redoStorage.resetUnRedo();
    }

    @Override
    public boolean apply(final ServerLevel world)
    {
        return redoStorage.redo(world);
    }
}
