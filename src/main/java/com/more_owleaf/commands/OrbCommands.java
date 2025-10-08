package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.init.EntityInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class OrbCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orb")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(OrbCommands::spawnOrb)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(OrbCommands::spawnOrbAt)))
        );
    }

    private static int spawnOrb(CommandContext<CommandSourceStack> context) {
        return spawnOrbInternal(context, BlockPos.containing(context.getSource().getPosition()));
    }

    private static int spawnOrbAt(CommandContext<CommandSourceStack> context) {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        return spawnOrbInternal(context, pos);
    }

    private static int spawnOrbInternal(CommandContext<CommandSourceStack> context, BlockPos pos) {
        ServerLevel level = context.getSource().getLevel();
        OrbEntity orb = new OrbEntity(EntityInit.ORB_ENTITY.get(), level);
        orb.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        level.addFreshEntity(orb);

        context.getSource().sendSuccess(() ->
                Component.literal("Orb spawned at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);
        return 1;
    }
}