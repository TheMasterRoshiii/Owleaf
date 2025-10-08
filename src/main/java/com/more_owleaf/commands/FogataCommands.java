package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.more_owleaf.More_Owleaf;
import com.more_owleaf.config.DeathRegistry;
import com.more_owleaf.utils.fogata.SoulUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FogataCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fogata")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("ignore")
                        .then(Commands.argument("player", StringArgumentType.string())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    More_Owleaf.DEATH_REGISTRY.addIgnoredPlayer(player);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Jugador " + player + " añadido a la lista de ignorados"), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("unignore")
                        .then(Commands.argument("player", StringArgumentType.string())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    More_Owleaf.DEATH_REGISTRY.removeIgnoredPlayer(player);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Jugador " + player + " removido de la lista de ignorados"), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("listadeaths")
                        .executes(context -> {
                            DeathRegistry deathRegistry = More_Owleaf.DEATH_REGISTRY;
                            if (deathRegistry.getDeathRecords().isEmpty()) {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("No hay jugadores muertos"), true);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("Jugadores muertos:"), true);
                                for (DeathRegistry.DeathRecord record : deathRegistry.getDeathRecords()) {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("- " + record.playerName + " (" + record.playerUUID + ")"), true);
                                }
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("removefromregistry")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(context -> {
                                    String uuid = StringArgumentType.getString(context, "uuid");
                                    More_Owleaf.DEATH_REGISTRY.removeDeathRecord(uuid);
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Jugador con UUID " + uuid + " removido del registro"), true);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("setlives")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .then(Commands.argument("lives", IntegerArgumentType.integer(0, 10))
                                        .executes(context -> {
                                            String uuid = StringArgumentType.getString(context, "uuid");
                                            int lives = IntegerArgumentType.getInteger(context, "lives");
                                            More_Owleaf.DEATH_REGISTRY.setPlayerLives(uuid, lives);
                                            context.getSource().sendSuccess(() ->
                                                    Component.literal("Vidas de " + uuid + " establecidas a " + lives), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("listignored")
                        .executes(context -> {
                            DeathRegistry deathRegistry = More_Owleaf.DEATH_REGISTRY;
                            if (deathRegistry.getIgnoredPlayers().isEmpty()) {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("No hay jugadores ignorados"), true);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("Jugadores ignorados:"), true);
                                for (String ignored : deathRegistry.getIgnoredPlayers()) {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("- " + ignored), true);
                                }
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("listlives")
                        .executes(context -> {
                            DeathRegistry deathRegistry = More_Owleaf.DEATH_REGISTRY;
                            if (deathRegistry.getPlayerLivesMap().isEmpty()) {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("No hay información de vidas"), true);
                            } else {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("Vidas de jugadores:"), true);
                                for (var entry : deathRegistry.getPlayerLivesMap().entrySet()) {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("- " + entry.getKey() + ": " + entry.getValue() + " vidas"), true);
                                }
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("daralma")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("cantidad", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "cantidad");
                                            player.getCapability(SoulUtil.SOUL_CAPABILITY).ifPresent(soul -> {
                                                soul.addSouls(amount);
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("Se dieron " + amount + " almas a " + player.getName().getString()), true);
                                            });
                                            return 1;
                                        })
                                )
                        )
                )

                .then(Commands.literal("quitaralma")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("cantidad", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "cantidad");
                                            player.getCapability(SoulUtil.SOUL_CAPABILITY).ifPresent(soul -> {
                                                soul.removeSouls(amount);
                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("Se quitaron " + amount + " almas a " + player.getName().getString()), true);
                                            });
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
