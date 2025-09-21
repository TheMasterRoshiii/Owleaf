package com.more_owleaf.commands;

import com.more_owleaf.config.CasinoConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CasinoReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("casinoreload")
                .requires(source -> {
                    if (source.hasPermission(2)) {
                        return true;
                    } else {
                        source.sendFailure(Component.literal("No tienes permisos para usar este comando")
                                .withStyle(style -> style.withColor(0xFF5555)));
                        return false;
                    }
                })
                .executes(CasinoReloadCommand::reloadConfig)
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            boolean success = CasinoConfig.loadConfig();

            if (success) {
                String interactionItemName = CasinoConfig.INTERACTION_ITEM.getDescription().getString();
                int cooldownSeconds = CasinoConfig.PRIZE_COOLDOWN_TICKS / 20;

                Component message = Component.literal("Configuración del casino recargada\n")
                        .withStyle(style -> style.withColor(0x55FF55))
                        .append(Component.literal("Premios cargados: " + CasinoConfig.PRIZE_MAP.size() + "\n")
                                .withStyle(style -> style.withColor(0xAAAAAA)))
                        .append(Component.literal("Item de interacción: " + interactionItemName + "\n")
                                .withStyle(style -> style.withColor(0xAAAAAA)))
                        .append(Component.literal("Cooldown del premio: " + cooldownSeconds + " segundos\n")
                                .withStyle(style -> style.withColor(0xAAAAAA)))
                        .append(Component.literal("Probabilidades: " +
                                        CasinoConfig.WINNING_CHANCE + "% ganadora, " +
                                        CasinoConfig.SEMI_WINNING_CHANCE + "% semi-ganadora, " +
                                        CasinoConfig.LOSING_CHANCE + "% perdedora")
                                .withStyle(style -> style.withColor(0xAAAAAA)));

                source.sendSuccess(() -> message, true);

                return Command.SINGLE_SUCCESS;
            } else {
                throw new Exception("No se pudo cargar la configuración");
            }

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error al recargar: " + e.getMessage())
                    .withStyle(style -> style.withColor(0xFF5555)));

            System.out.println("Error reloading casino config: " + e.getMessage());
            return 0;
        }
    }
}