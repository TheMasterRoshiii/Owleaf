package com.more_owleaf.utils.orb;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.more_owleaf.config.OrbConfig;
import net.minecraftforge.fml.loading.FMLPaths;

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

    public static void initialize() {
        configDirectory = new File(FMLPaths.CONFIGDIR.get().toFile(), "more_owleaf/orbs");
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
        }
        loadAllConfigs();
    }

    public static void saveOrbConfig(UUID orbId, OrbConfig config) {
        if (configDirectory == null) {
            initialize();
        }

        ORB_CONFIGS.put(orbId, config);

        File configFile = new File(configDirectory, orbId.toString() + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Error saving orb config: " + e.getMessage());
        }
    }

    public static OrbConfig loadOrbConfig(UUID orbId) {
        if (configDirectory == null) {
            initialize();
        }

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
                System.err.println("Error loading orb config: " + e.getMessage());
            }
        }

        OrbConfig defaultConfig = new OrbConfig();
        defaultConfig.setMode(OrbConfig.OrbMode.IDLE);
        defaultConfig.setActive(false);
        return defaultConfig;
    }

    public static String configToJson(OrbConfig config) {
        return GSON.toJson(config);
    }

    public static OrbConfig jsonToConfig(String json) {
        try {
            return GSON.fromJson(json, OrbConfig.class);
        } catch (Exception e) {
            OrbConfig defaultConfig = new OrbConfig();
            defaultConfig.setMode(OrbConfig.OrbMode.IDLE);
            defaultConfig.setActive(false);
            return defaultConfig;
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
                        try (FileReader reader = new FileReader(file)) {
                            OrbConfig config = GSON.fromJson(reader, OrbConfig.class);
                            ORB_CONFIGS.put(orbId, config);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    public static void deleteOrbConfig(UUID orbId) {
        ORB_CONFIGS.remove(orbId);
        File configFile = new File(configDirectory, orbId.toString() + ".json");
        if (configFile.exists()) {
            configFile.delete();
        }
    }
}