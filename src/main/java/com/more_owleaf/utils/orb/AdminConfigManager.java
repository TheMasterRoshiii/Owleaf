package com.more_owleaf.utils.orb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.more_owleaf.config.AdminConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AdminConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    private static AdminConfig CONFIG;

    public static void initialize() {
        File configDir = FMLPaths.CONFIGDIR.get().toFile();
        configFile = new File(configDir, "more_owleaf/admins.json");
    }

    public static void loadConfig() {
        if (configFile == null) initialize();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                CONFIG = GSON.fromJson(reader, AdminConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CONFIG = new AdminConfig();
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPlayerAdmin(String playerName) {
        if (CONFIG == null || CONFIG.getAllowedPlayers() == null) {
            return false;
        }
        return CONFIG.getAllowedPlayers().stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
    }
}