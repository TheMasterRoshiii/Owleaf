package com.more_owleaf.commands;

import com.more_owleaf.config.RunaTradesConfig;
import com.more_owleaf.entities.RunaEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MerchantMenu;

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
                        runa.reloadTrades();
                        runasUpdated++;


                        if (runa.getTradingPlayer() instanceof ServerPlayer serverPlayer) {
                            if (serverPlayer.containerMenu instanceof MerchantMenu) {
                                serverPlayer.connection.send(new ClientboundMerchantOffersPacket(
                                        serverPlayer.containerMenu.containerId,
                                        runa.getOffers(),
                                        0,
                                        runa.getVillagerXp(),
                                        runa.showProgressBar(),
                                        runa.canRestock()
                                ));
                            }
                        }
                    }
                }
            }

            context.getSource().sendSuccess(() ->
                            Component.literal("§aConfiguración de trades de runas recargada correctamente"),
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
