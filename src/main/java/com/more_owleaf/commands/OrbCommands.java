package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.init.EntityInit;
import com.more_owleaf.network.NetworkHandler;
import com.more_owleaf.network.orb.OpenOrbScreenPacket;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrbCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orb")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(OrbCommands::spawnOrb)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(OrbCommands::spawnOrbAt)))
                .then(Commands.literal("reload")
                        .executes(OrbCommands::reloadOrbs))
                .then(Commands.literal("open")
                        .then(Commands.argument("uuid", StringArgumentType.greedyString())
                                .executes(OrbCommands::openOrbRemote)))
                .then(Commands.literal("list")
                        .executes(OrbCommands::listOrbs))
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

    private static int openOrbRemote(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String uuidString = StringArgumentType.getString(context, "uuid");

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cThis command can only be used by players"));
            return 0;
        }

        try {
            UUID orbUUID = UUID.fromString(uuidString);
            ServerLevel level = source.getLevel();

            Entity entity = level.getEntity(orbUUID);
            if (entity instanceof OrbEntity orbEntity) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new OpenOrbScreenPacket(orbEntity.getId(), orbEntity.getConfig())
                );
                source.sendSuccess(() ->
                        Component.literal("§aOpened orb configuration remotely for UUID: " + uuidString), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cOrb with UUID " + uuidString + " not found in current dimension"));
                return 0;
            }
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cInvalid UUID format: " + uuidString));
            return 0;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cError opening orb: " + e.getMessage()));
            return 0;
        }
    }

    private static int listOrbs(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        List<OrbEntity> orbs = new ArrayList<>();

        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof OrbEntity orbEntity) {
                orbs.add(orbEntity);
            }
        }

        if (orbs.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§eNo orbs found in current dimension"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Orbs in current dimension ==="), false);
        source.sendSuccess(() -> Component.literal("§7Total found: §e" + orbs.size()), false);
        source.sendSuccess(() -> Component.literal(""), false);

        for (int i = 0; i < orbs.size(); i++) {
            OrbEntity orb = orbs.get(i);
            BlockPos pos = orb.blockPosition();
            String mode = orb.getConfig().getMode().toString();
            String status = orb.getConfig().isActive() ? "§aACTIVE" : "§cINACTIVE";
            String uuid = orb.getUUID().toString();

            int finalI = i;
            source.sendSuccess(() -> Component.literal(
                    String.format("§f%d. §e%s §7| %s §7| [%d, %d, %d]",
                            finalI + 1, mode, status, pos.getX(), pos.getY(), pos.getZ())
            ), false);

            MutableComponent uuidComponent = Component.literal("   §7UUID: §f" + uuid)
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.literal("§aClick to copy UUID\n§7" + uuid)))
                    );

            source.sendSuccess(() -> uuidComponent, false);
            source.sendSuccess(() -> Component.literal(""), false);
        }

        return orbs.size();
    }
}
