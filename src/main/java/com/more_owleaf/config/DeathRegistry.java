package com.more_owleaf.config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeathRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/more_owleaf/death_registry.json");

    private List<DeathRecord> deathRecords = new ArrayList<>();
    private Set<String> ignoredPlayers = new HashSet<>();

    public static class DeathRecord {
        public String playerName;
        public String playerUUID;
        public String deathTime;
        public String dimension;

        public DeathRecord(String playerName, String playerUUID, String deathTime, String dimension) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.deathTime = deathTime;
            this.dimension = dimension;
        }
    }

    public static class DeathRegistryConfig {
        public List<DeathRecord> deaths = new ArrayList<>();
        public Set<String> ignoredPlayers = new HashSet<>();
    }

    public void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                DeathRegistryConfig config = GSON.fromJson(json, DeathRegistryConfig.class);
                if (config != null) {
                    this.deathRecords = config.deaths != null ? config.deaths : new ArrayList<>();
                    this.ignoredPlayers = config.ignoredPlayers != null ? config.ignoredPlayers : new HashSet<>();

                    for (DeathRecord record : this.deathRecords) {
                        if (record.playerUUID == null || record.playerUUID.isEmpty()) {
                            System.out.println("Death record for " + record.playerName + " has no UUID");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load death registry: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            DeathRegistryConfig config = new DeathRegistryConfig();
            config.deaths = this.deathRecords;
            config.ignoredPlayers = this.ignoredPlayers;
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            System.err.println("Failed to save death registry: " + e.getMessage());
        }
    }

    public void registerDeath(Player player, MinecraftServer server) {
        String playerName = player.getName().getString();

        if (ignoredPlayers.contains(playerName)) {
            return;
        }

        String playerUUID = player.getUUID().toString();
        String deathTime = java.time.LocalDateTime.now().toString();
        String dimension = player.level().dimension().location().toString();

        DeathRecord record = new DeathRecord(playerName, playerUUID, deathTime, dimension);

        deathRecords.add(record);
        saveConfig();
    }

    public List<DeathRecord> getDeathRecords() {
        return new ArrayList<>(deathRecords);
    }
}