package com.more_owleaf.commands;

import com.more_owleaf.config.RunaTradesConfig;
import com.more_owleaf.entities.RunaEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class RunaReloadCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("runareload")
                .requires(source -> source.hasPermission(2))
                .executes(RunaReloadCommand::reloadRunaConfig)
        );
    }

    private static int reloadRunaConfig(CommandContext<CommandSourceStack> context) {
        try {
            context.getSource().sendSuccess(() ->
                            Component.literal("§eRecargando configuración de trades de runas..."),
                    false
            );

            RunaTradesConfig.printAllConfiguredRunas();

            boolean configLoaded = RunaTradesConfig.loadConfig();

            if (!configLoaded) {
                context.getSource().sendFailure(
                        Component.literal("§cError al cargar la configuración de trades de runas")
                );
                return 0;
            }

            int runasUpdated = 0;

            for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof RunaEntity runa) {
                        runasUpdated++;
                    }
                }
            }

            final int finalRunasUpdated = runasUpdated;
            final int finalLoadedRunaCount = RunaTradesConfig.getLoadedRunaCount();

            context.getSource().sendSuccess(() ->
                            Component.literal("§aConfiguración de trades de runas recargada correctamente")
                                    .append("\n§7Runas configuradas en archivo: " + finalLoadedRunaCount)
                                    .append("\n§7Runas encontradas en el mundo: " + finalRunasUpdated),
                    true
            );

            return Command.SINGLE_SUCCESS;

        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("§cError al recargar configuración: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }
}