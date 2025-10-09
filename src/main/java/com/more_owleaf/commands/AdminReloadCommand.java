package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.more_owleaf.config.AdminConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AdminReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("admin")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(4))
                        .executes(AdminReloadCommand::reloadAdmin)
                )
        );
    }

    private static int reloadAdmin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            AdminConfig.reload();
            source.sendSuccess(() -> Component.literal("§aAdmin configuration reloaded successfully!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cFailed to reload admin configuration: " + e.getMessage()));
            return 0;
        }
    }
}
