package com.more_owleaf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AdminConfig INSTANCE;
    private static File configFile;

    @SerializedName("allowed_players")
    private List<String> allowedPlayers = new ArrayList<>();

    public static void initialize() {
        File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "more_owleaf");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "admin.json");
        loadConfig();
    }

    private static void loadConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                INSTANCE = GSON.fromJson(reader, AdminConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new AdminConfig();
                }
            } catch (IOException e) {
                LOGGER.error("Error loading admin config: " + e.getMessage());
                INSTANCE = new AdminConfig();
            }
        } else {
            INSTANCE = new AdminConfig();
            saveConfig();
        }
    }

    private static void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            LOGGER.error("Error saving admin config: " + e.getMessage());
        }
    }

    public static void reload() {
        loadConfig();
        LOGGER.info("Admin configuration reloaded successfully");
    }

    public static List<String> getAllowedPlayers() {
        if (INSTANCE == null) {
            initialize();
        }
        return INSTANCE.allowedPlayers;
    }

    public static void addAllowedPlayer(String playerName) {
        if (INSTANCE == null) {
            initialize();
        }
        if (!INSTANCE.allowedPlayers.contains(playerName)) {
            INSTANCE.allowedPlayers.add(playerName);
            saveConfig();
        }
    }

    public static void removeAllowedPlayer(String playerName) {
        if (INSTANCE == null) {
            initialize();
        }
        INSTANCE.allowedPlayers.remove(playerName);
        saveConfig();
    }

    public static boolean isPlayerAllowed(String playerName) {
        if (INSTANCE == null) {
            initialize();
        }
        return INSTANCE.allowedPlayers.contains(playerName);
    }
}
