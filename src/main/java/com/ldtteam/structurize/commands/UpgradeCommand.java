package com.ldtteam.structurize.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class UpgradeCommand
{
    /**
     * Command for creating a new session for sender
     */
    protected static class ToDO extends AbstractCommand
    {
        private final static String NAME = "DO";

        /**
         * Position 1 command argument.
         */
        private static final String START = "start";

        /**
         * Position 2 command argument.
         */
        private static final String END = "end";

        /**
         *
         */
        private static final String WORLD = "world";

        protected static LiteralArgumentBuilder<CommandSourceStack> build()
        {
            return newLiteral(NAME)
                     .then(newArgument(START, BlockPosArgument.blockPos())
                             .then(newArgument(END, BlockPosArgument.blockPos())
                                     .then(newArgument(WORLD, DimensionArgument.dimension())
                                             .executes(ToDO::onExecute)
                                     )
                             )
                     );
        }

        private static int onExecute(final CommandContext<CommandSourceStack> command) throws CommandSyntaxException
        {
            final BlockPos start = BlockPosArgument.getSpawnablePos(command, START);
            final BlockPos end = BlockPosArgument.getSpawnablePos(command, END);
            final ServerLevel level = DimensionArgument.getDimension(command, WORLD);

            final BlockPos iterationStart = new BlockPos(
              Math.min(start.getX(), end.getX()),
              Math.min(start.getY(), end.getY()),
              Math.min(start.getZ(), end.getZ())
            );

            final BlockPos iterationEnd = new BlockPos(
              Math.max(start.getX(), end.getX()),
              Math.max(start.getY(), end.getY()),
              Math.max(start.getZ(), end.getZ())
            );

            BlockPos.MutableBlockPos iterationPos = iterationStart.mutable();

            while (iterationPos.getX() <= iterationEnd.getX()) {
                while(iterationPos.getY() <= iterationEnd.getY()) {
                    while(iterationPos.getZ() <= iterationEnd.getZ()) {
                        final BlockState blockState = level.getBlockState(iterationPos);
                        System.out.println(blockState);

                        iterationPos.move(Direction.SOUTH);
                    }

                    iterationPos.move(Direction.UP);
                }

                iterationPos.move(Direction.EAST);
            }

            return 0;
        }
    }
}
