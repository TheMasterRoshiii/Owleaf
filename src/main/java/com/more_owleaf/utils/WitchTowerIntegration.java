package com.more_owleaf.utils;

import com.more_owleaf.entities.orb.OrbEntity;
import com.more_owleaf.entities.orb.OrbConfig;
import com.more_owleaf.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.Blocks;

import javax.swing.text.html.parser.Entity;

public class WitchTowerIntegration {

    public static void convertSpawnerToOrb(Level level, BlockPos spawnerPos) {
        if (level.getBlockState(spawnerPos).getBlock() == Blocks.SPAWNER) {
            SpawnerBlockEntity spawner = (SpawnerBlockEntity) level.getBlockEntity(spawnerPos);

            level.setBlock(spawnerPos, Blocks.AIR.defaultBlockState(), 3);

            OrbEntity orb = new OrbEntity(EntityInit.ORB_ENTITY.get(), level);
            orb.setPos(spawnerPos.getX() + 0.5, spawnerPos.getY() + 1, spawnerPos.getZ() + 0.5);

            OrbConfig config = new OrbConfig();
            config.setMode(OrbConfig.OrbMode.SPAWNER);
            config.setActive(true);
            config.setMobType("minecraft:witch");
            config.setSpawnRate(200);
            config.setMaxMobs(3);

            orb.setConfig(config);
            level.addFreshEntity(orb);
        }
    }

    public static void scanAndConvertTowers(Level level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.getBlockState(pos).getBlock() == Blocks.SPAWNER) {
                        convertSpawnerToOrb(level, pos);
                    }
                }
            }
        }
    }
}
