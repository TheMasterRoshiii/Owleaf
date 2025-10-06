package com.more_owleaf.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.more_owleaf.entities.orb.OrbConfig;
import com.more_owleaf.entities.orb.OrbEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrbConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, OrbConfig> ORB_CONFIGS = new HashMap<>();
    private static File configDirectory;

    public static void initialize(File modConfigDir) {
        configDirectory = new File(modConfigDir, "orbs");
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }
        loadAllConfigs();
    }

    public static void saveOrbConfig(UUID orbId, OrbConfig config) {
        ORB_CONFIGS.put(orbId, config);

        File configFile = new File(configDirectory, orbId.toString() + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static OrbConfig loadOrbConfig(UUID orbId) {
        if (ORB_CONFIGS.containsKey(orbId)) {
            return ORB_CONFIGS.get(orbId);
        }

        File configFile = new File(configDirectory, orbId.toString() + ".json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                OrbConfig config = GSON.fromJson(reader, OrbConfig.class);
                ORB_CONFIGS.put(orbId, config);
                return config;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        OrbConfig defaultConfig = new OrbConfig();
        ORB_CONFIGS.put(orbId, defaultConfig);
        return defaultConfig;
    }

    public static void updateOrbFromJson(OrbEntity orb, String jsonString) {
        try {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            OrbConfig config = orb.getConfig();

            if (json.has("mode")) {
                OrbConfig.OrbMode mode = OrbConfig.OrbMode.valueOf(
                        json.get("mode").getAsString().toUpperCase()
                );
                config.setMode(mode);
            }

            if (json.has("active")) {
                config.setActive(json.get("active").getAsBoolean());
            }

            if (json.has("mob_type")) {
                config.setMobType(json.get("mob_type").getAsString());
            }

            if (json.has("spawn_rate")) {
                config.setSpawnRate(json.get("spawn_rate").getAsInt());
            }

            if (json.has("barrier_size")) {
                config.setBarrierSize(json.get("barrier_size").getAsFloat());
            }

            if (json.has("max_mobs")) {
                config.setMaxMobs(json.get("max_mobs").getAsInt());
            }

            orb.setConfig(config);
            saveOrbConfig(orb.getUUID(), config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String configToJson(OrbConfig config) {
        return GSON.toJson(config);
    }

    public static OrbConfig jsonToConfig(String json) {
        try {
            return GSON.fromJson(json, OrbConfig.class);
        } catch (Exception e) {
            return new OrbConfig();
        }
    }

    private static void loadAllConfigs() {
        if (configDirectory.exists()) {
            File[] configFiles = configDirectory.listFiles((dir, name) -> name.endsWith(".json"));
            if (configFiles != null) {
                for (File file : configFiles) {
                    try {
                        String fileName = file.getName().replace(".json", "");
                        UUID orbId = UUID.fromString(fileName);
                        loadOrbConfig(orbId);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public static void applyConfigToOrb(Level level, UUID orbId, OrbConfig config) {
        if (level instanceof ServerLevel) {
            Entity entity = ((ServerLevel) level).getEntity(orbId);
            if (entity instanceof OrbEntity orb) {
                orb.setConfig(config);
            }
        }
    }
}
