package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OrbReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orb")
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(OrbReloadCommand::reloadOrbs)
                )
        );
    }

    private static int reloadOrbs(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            OrbConfigManager.reloadAllConfigs();
            source.sendSuccess(() -> Component.literal("§aOrb configurations reloaded successfully!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cFailed to reload orb configurations: " + e.getMessage()));
            return 0;
        }
    }
}
