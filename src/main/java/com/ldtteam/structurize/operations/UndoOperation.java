package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Operation for undo.
 */
public class UndoOperation extends BaseOperation
{
    /**
     * The change to undo.
     */
    private final ChangeStorage undoStorage;

    /**
     * Default constructor.
     *
     * @param player      the player to trigger the undo.
     * @param undoStorage the change to undo.
     */
    public UndoOperation(final Player player, final ChangeStorage undoStorage)
    {
        super(new ChangeStorage(Component.translatable("com.ldtteam.structurize.undo", undoStorage.getOperation()), player != null ? player.getUUID() : UUID.randomUUID()));
        if (undoStorage.getOperation().toString().indexOf("undo") != 0)
        {
            this.undoStorage = undoStorage;
            this.undoStorage.resetUnRedo();
        }
        else
        {
            this.undoStorage = null;
        }
    }

    @Override
    public boolean apply(final ServerLevel world)
    {
        return undoStorage.undo(world, storage);
    }
}
