package com.more_owleaf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.more_owleaf.entities.orb.OrbEntity;
import com.more_owleaf.entities.orb.OrbConfig;
import com.more_owleaf.utils.OrbConfigManager;
import com.more_owleaf.utils.WitchTowerIntegration;
import com.more_owleaf.init.EntityInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import java.util.List;

public class OrbCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("orb")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("spawn")
                        .executes(OrbCommands::spawnOrb)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(OrbCommands::spawnOrbAt)))

                .then(Commands.literal("wall")
                        .then(Commands.literal("create")
                                .executes(OrbCommands::createWall)
                                .then(Commands.argument("size", FloatArgumentType.floatArg(1.0f, 10.0f))
                                        .executes(OrbCommands::createWallWithSize)
                                        .then(Commands.argument("height", FloatArgumentType.floatArg(1.0f, 10.0f))
                                                .executes(OrbCommands::createWallWithSizeAndHeight))))
                        .then(Commands.literal("toggle")
                                .executes(OrbCommands::toggleNearbyWalls))
                        .then(Commands.literal("size")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("size", FloatArgumentType.floatArg(1.0f, 10.0f))
                                                .executes(OrbCommands::setWallSize))))
                        .then(Commands.literal("height")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("height", FloatArgumentType.floatArg(1.0f, 10.0f))
                                                .executes(OrbCommands::setWallHeight)))))

                .then(Commands.literal("spawner")
                        .then(Commands.literal("create")
                                .executes(OrbCommands::createSpawner)
                                .then(Commands.argument("mob", StringArgumentType.word())
                                        .executes(OrbCommands::createSpawnerWithMob)
                                        .then(Commands.argument("rate", IntegerArgumentType.integer(20, 1200))
                                                .executes(OrbCommands::createSpawnerWithMobAndRate)
                                                .then(Commands.argument("max", IntegerArgumentType.integer(1, 20))
                                                        .executes(OrbCommands::createSpawnerWithAll)))))
                        .then(Commands.literal("toggle")
                                .executes(OrbCommands::toggleNearbySpawners))
                        .then(Commands.literal("mob")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("mob", StringArgumentType.word())
                                                .executes(OrbCommands::setSpawnerMob))))
                        .then(Commands.literal("rate")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("rate", IntegerArgumentType.integer(20, 1200))
                                                .executes(OrbCommands::setSpawnerRate))))
                        .then(Commands.literal("max")
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .then(Commands.argument("max", IntegerArgumentType.integer(1, 20))
                                                .executes(OrbCommands::setSpawnerMax)))))

                .then(Commands.literal("mode")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .executes(OrbCommands::setOrbMode))))

                .then(Commands.literal("nearby")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .executes(OrbCommands::configureNearby)))

                .then(Commands.literal("convert")
                        .then(Commands.literal("spawners")
                                .executes(OrbCommands::convertSpawners)))

                .then(Commands.literal("list")
                        .executes(OrbCommands::listNearbyOrbs))

                .then(Commands.literal("clear")
                        .executes(OrbCommands::clearNearbyOrbs))

                .then(Commands.literal("debug")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(OrbCommands::debugOrb)))
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

        OrbConfig config = new OrbConfig();
        config.setMode(OrbConfig.OrbMode.IDLE);
        orb.setConfig(config);

        level.addFreshEntity(orb);

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        context.getSource().sendSuccess(() ->
                Component.literal("Orb spawned at " + x + ", " + y + ", " + z), true);
        return 1;
    }

    private static int createWall(CommandContext<CommandSourceStack> context) {
        return createWallInternal(context, 3.0f, 3.0f);
    }

    private static int createWallWithSize(CommandContext<CommandSourceStack> context) {
        float size = FloatArgumentType.getFloat(context, "size");
        return createWallInternal(context, size, 3.0f);
    }

    private static int createWallWithSizeAndHeight(CommandContext<CommandSourceStack> context) {
        float size = FloatArgumentType.getFloat(context, "size");
        float height = FloatArgumentType.getFloat(context, "height");
        return createWallInternal(context, size, height);
    }

    private static int createWallInternal(CommandContext<CommandSourceStack> context, float size, float height) {
        ServerLevel level = context.getSource().getLevel();
        Vec3 pos = context.getSource().getPosition();
        OrbEntity orb = new OrbEntity(EntityInit.ORB_ENTITY.get(), level);
        orb.setPos(pos.x, pos.y + 1, pos.z);

        OrbConfig config = new OrbConfig();
        config.setMode(OrbConfig.OrbMode.BARRIER);
        config.setActive(true);
        config.setBarrierSize(size);
        config.setBarrierHeight(height);
        orb.setConfig(config);

        level.addFreshEntity(orb);

        final float finalSize = size;
        final float finalHeight = height;

        context.getSource().sendSuccess(() ->
                Component.literal("Wall created with size " + finalSize + " and height " + finalHeight), true);
        return 1;
    }

    private static int toggleNearbyWalls(CommandContext<CommandSourceStack> context) {
        AABB area = new AABB(context.getSource().getPosition().add(-10, -5, -10),
                context.getSource().getPosition().add(10, 5, 10));

        List<OrbEntity> orbs = context.getSource().getLevel()
                .getEntitiesOfClass(OrbEntity.class, area);

        int wallCount = 0;
        for (OrbEntity orb : orbs) {
            if (orb.getConfig().getMode() == OrbConfig.OrbMode.BARRIER) {
                OrbConfig config = orb.getConfig();
                config.setActive(!config.isActive());
                orb.setConfig(config);
                wallCount++;
            }
        }

        final int finalWallCount = wallCount;
        context.getSource().sendSuccess(() ->
                Component.literal("Toggled " + finalWallCount + " walls"), true);
        return wallCount;
    }

    private static int setWallSize(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            float size = FloatArgumentType.getFloat(context, "size");

            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                config.setBarrierSize(size);
                orb.setConfig(config);

                final float finalSize = size;
                context.getSource().sendSuccess(() ->
                        Component.literal("Wall size set to " + finalSize), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int setWallHeight(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            float height = FloatArgumentType.getFloat(context, "height");

            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                config.setBarrierHeight(height);
                orb.setConfig(config);

                final float finalHeight = height;
                context.getSource().sendSuccess(() ->
                        Component.literal("Wall height set to " + finalHeight), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int createSpawner(CommandContext<CommandSourceStack> context) {
        return createSpawnerInternal(context, "zombie", 100, 5);
    }

    private static int createSpawnerWithMob(CommandContext<CommandSourceStack> context) {
        String mob = StringArgumentType.getString(context, "mob");
        return createSpawnerInternal(context, mob, 100, 5);
    }

    private static int createSpawnerWithMobAndRate(CommandContext<CommandSourceStack> context) {
        String mob = StringArgumentType.getString(context, "mob");
        int rate = IntegerArgumentType.getInteger(context, "rate");
        return createSpawnerInternal(context, mob, rate, 5);
    }

    private static int createSpawnerWithAll(CommandContext<CommandSourceStack> context) {
        String mob = StringArgumentType.getString(context, "mob");
        int rate = IntegerArgumentType.getInteger(context, "rate");
        int max = IntegerArgumentType.getInteger(context, "max");
        return createSpawnerInternal(context, mob, rate, max);
    }

    private static int createSpawnerInternal(CommandContext<CommandSourceStack> context, String mob, int rate, int max) {
        ServerLevel level = context.getSource().getLevel();
        Vec3 pos = context.getSource().getPosition();
        OrbEntity orb = new OrbEntity(EntityInit.ORB_ENTITY.get(), level);
        orb.setPos(pos.x, pos.y + 1, pos.z);

        OrbConfig config = new OrbConfig();
        config.setMode(OrbConfig.OrbMode.SPAWNER);
        config.setActive(true);
        config.setMobType("minecraft:" + mob);
        config.setSpawnRate(rate);
        config.setMaxMobs(max);
        orb.setConfig(config);

        level.addFreshEntity(orb);

        final String finalMob = mob;
        final int finalRate = rate;
        final int finalMax = max;

        context.getSource().sendSuccess(() ->
                Component.literal("Spawner created: " + finalMob + " every " + finalRate + " ticks, max " + finalMax), true);
        return 1;
    }

    private static int toggleNearbySpawners(CommandContext<CommandSourceStack> context) {
        AABB area = new AABB(context.getSource().getPosition().add(-10, -5, -10),
                context.getSource().getPosition().add(10, 5, 10));

        List<OrbEntity> orbs = context.getSource().getLevel()
                .getEntitiesOfClass(OrbEntity.class, area);

        int spawnerCount = 0;
        for (OrbEntity orb : orbs) {
            if (orb.getConfig().getMode() == OrbConfig.OrbMode.SPAWNER) {
                OrbConfig config = orb.getConfig();
                config.setActive(!config.isActive());
                orb.setConfig(config);
                spawnerCount++;
            }
        }

        final int finalSpawnerCount = spawnerCount;
        context.getSource().sendSuccess(() ->
                Component.literal("Toggled " + finalSpawnerCount + " spawners"), true);
        return spawnerCount;
    }

    private static int setSpawnerMob(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            String mob = StringArgumentType.getString(context, "mob");

            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                config.setMobType("minecraft:" + mob);
                orb.setConfig(config);

                final String finalMob = mob;
                context.getSource().sendSuccess(() ->
                        Component.literal("Spawner mob set to " + finalMob), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int setSpawnerRate(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            int rate = IntegerArgumentType.getInteger(context, "rate");

            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                config.setSpawnRate(rate);
                orb.setConfig(config);

                final int finalRate = rate;
                context.getSource().sendSuccess(() ->
                        Component.literal("Spawner rate set to " + finalRate + " ticks"), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int setSpawnerMax(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            int max = IntegerArgumentType.getInteger(context, "max");

            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                config.setMaxMobs(max);
                orb.setConfig(config);

                final int finalMax = max;
                context.getSource().sendSuccess(() ->
                        Component.literal("Spawner max mobs set to " + finalMax), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int setOrbMode(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            String mode = StringArgumentType.getString(context, "mode");

            if (entity instanceof OrbEntity orb) {
                OrbConfig.OrbMode orbMode = OrbConfig.OrbMode.valueOf(mode.toUpperCase());
                OrbConfig config = orb.getConfig();
                config.setMode(orbMode);
                config.setActive(true);
                orb.setConfig(config);

                final String finalMode = mode;
                context.getSource().sendSuccess(() ->
                        Component.literal("Orb mode set to " + finalMode), true);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }

    private static int configureNearby(CommandContext<CommandSourceStack> context) {
        try {
            String mode = StringArgumentType.getString(context, "mode");
            OrbConfig.OrbMode orbMode = OrbConfig.OrbMode.valueOf(mode.toUpperCase());

            AABB area = new AABB(context.getSource().getPosition().add(-10, -10, -10),
                    context.getSource().getPosition().add(10, 10, 10));

            List<OrbEntity> orbs = context.getSource().getLevel()
                    .getEntitiesOfClass(OrbEntity.class, area);

            for (OrbEntity orb : orbs) {
                OrbConfig config = orb.getConfig();
                config.setMode(orbMode);
                config.setActive(true);
                orb.setConfig(config);
            }

            final int orbCount = orbs.size();
            final String finalMode = mode;
            context.getSource().sendSuccess(() ->
                    Component.literal("Configured " + orbCount + " orbs to " + finalMode + " mode"), true);
            return orbs.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int convertSpawners(CommandContext<CommandSourceStack> context) {
        try {
            BlockPos centerPos = BlockPos.containing(context.getSource().getPosition());

            WitchTowerIntegration.scanAndConvertTowers(
                    context.getSource().getLevel(),
                    centerPos,
                    50
            );
            context.getSource().sendSuccess(() ->
                    Component.literal("Spawners converted to orbs"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Error converting spawners: " + e.getMessage()));
            return 0;
        }
    }

    private static int listNearbyOrbs(CommandContext<CommandSourceStack> context) {
        AABB area = new AABB(context.getSource().getPosition().add(-20, -10, -20),
                context.getSource().getPosition().add(20, 10, 20));

        List<OrbEntity> orbs = context.getSource().getLevel()
                .getEntitiesOfClass(OrbEntity.class, area);

        if (orbs.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("No orbs found nearby"), false);
            return 0;
        }

        final int orbCount = orbs.size();
        context.getSource().sendSuccess(() ->
                Component.literal("Found " + orbCount + " orbs nearby:"), false);

        for (OrbEntity orb : orbs) {
            OrbConfig config = orb.getConfig();
            String status = config.isActive() ? "ACTIVE" : "INACTIVE";
            BlockPos pos = orb.blockPosition();

            final String mode = config.getMode().toString();
            final String finalStatus = status;
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();

            context.getSource().sendSuccess(() ->
                    Component.literal("- " + mode + " (" + finalStatus + ") at " +
                            x + ", " + y + ", " + z), false);
        }

        return orbs.size();
    }

    private static int clearNearbyOrbs(CommandContext<CommandSourceStack> context) {
        AABB area = new AABB(context.getSource().getPosition().add(-10, -5, -10),
                context.getSource().getPosition().add(10, 5, 10));

        List<OrbEntity> orbs = context.getSource().getLevel()
                .getEntitiesOfClass(OrbEntity.class, area);

        int count = orbs.size();
        for (OrbEntity orb : orbs) {
            orb.discard();
        }

        final int finalCount = count;
        context.getSource().sendSuccess(() ->
                Component.literal("Removed " + finalCount + " orbs"), true);
        return count;
    }

    private static int debugOrb(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "target");
            if (entity instanceof OrbEntity orb) {
                OrbConfig config = orb.getConfig();
                context.getSource().sendSuccess(() ->
                        Component.literal("Orb Debug - Mode: " + config.getMode() +
                                ", Active: " + config.isActive() +
                                ", Mob: " + config.getMobType() +
                                ", Rate: " + config.getSpawnRate() +
                                ", Max: " + config.getMaxMobs()), false);
                return 1;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 0;
    }
}
