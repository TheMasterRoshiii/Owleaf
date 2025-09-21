package com.more_owleaf.commands;

import com.more_owleaf.config.CasinoConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CasinoToggleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("casino")
                .requires(source -> {
                    if (source.hasPermission(2)) {
                        return true;
                    } else {
                        source.sendFailure(Component.literal("No tienes permisos para usar este comando")
                                .withStyle(style -> style.withColor(0xFF5555)));
                        return false;
                    }
                })
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(CasinoToggleCommand::toggleCasino))
        );
    }

    private static int toggleCasino(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        try {
            CasinoConfig.CASINO_ENABLED = enabled;
            boolean success = CasinoConfig.saveConfig();

            if (success) {
                String status = enabled ? "habilitado" : "deshabilitado";
                int color = enabled ? 0x55FF55 : 0xFF5555;

                Component message = Component.literal("Casino " + status + " en todo el servidor")
                        .withStyle(style -> style.withColor(color));

                source.sendSuccess(() -> message, true);

                System.out.println("Casino " + status + " by " + source.getTextName());

                return Command.SINGLE_SUCCESS;
            } else {
                throw new Exception("No se pudo guardar la configuración");
            }

        } catch (Exception e) {
            source.sendFailure(Component.literal("Error al cambiar estado del casino: " + e.getMessage())
                    .withStyle(style -> style.withColor(0xFF5555)));

            System.out.println("Error toggling casino: " + e.getMessage());
            return 0;
        }
    }
}