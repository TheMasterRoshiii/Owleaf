package com.more_owleaf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
    private static final int MAX_DEATHS = 15;

    private List<DeathRecord> deathRecords = new ArrayList<>();
    private Set<String> ignoredPlayers = new HashSet<>();

    public static class DeathRecord {
        public String playerName;
        public String playerUUID;
        public String deathTime;
        public String dimension;
        public String skinTextureData;
        public String skinSignature;
        public boolean hasSkinData;

        public DeathRecord(String playerName, String playerUUID, String deathTime, String dimension,
                           String skinTextureData, String skinSignature, boolean hasSkinData) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.deathTime = deathTime;
            this.dimension = dimension;
            this.skinTextureData = skinTextureData;
            this.skinSignature = skinSignature;
            this.hasSkinData = hasSkinData;
        }

        public DeathRecord(String playerName, String playerUUID, String deathTime, String dimension) {
            this(playerName, playerUUID, deathTime, dimension, "", "", false);
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
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        String playerName = player.getName().getString();
        if (ignoredPlayers.contains(playerName)) return;

        String playerUUID = player.getUUID().toString();
        String deathTime = java.time.LocalDateTime.now().toString();
        String dimension = player.level().dimension().location().toString();

        String skinTextureData = "";
        String skinSignature = "";
        boolean hasSkinData = false;

        try {
            GameProfile gameProfile = serverPlayer.getGameProfile();
            if (gameProfile != null && gameProfile.getProperties().containsKey("textures")) {
                Property textureProperty = gameProfile.getProperties().get("textures").iterator().next();
                if (textureProperty != null && textureProperty.getValue() != null && !textureProperty.getValue().isEmpty()) {
                    skinTextureData = textureProperty.getValue();
                    skinSignature = textureProperty.getSignature() != null ? textureProperty.getSignature() : "";
                    hasSkinData = true;
                }
            }
        } catch (Exception e) {
            System.err.println("[Owleaf] Error capturing skin data for " + playerName + ": " + e.getMessage());
        }

        DeathRecord record = new DeathRecord(playerName, playerUUID, deathTime, dimension,
                skinTextureData, skinSignature, hasSkinData);
        deathRecords.add(0, record);

        if (deathRecords.size() > MAX_DEATHS) {
            deathRecords = deathRecords.subList(0, MAX_DEATHS);
        }

        saveConfig();
    }


    public List<DeathRecord> getDeathRecords() {
        return new ArrayList<>(deathRecords);
    }
}
