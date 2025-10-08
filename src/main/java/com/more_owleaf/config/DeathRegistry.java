package com.more_owleaf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerPlayer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class DeathRegistry {
    private static final Path CONFIG_PATH = Paths.get("config", "more_owleaf_deaths.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Contenedor para todos los datos que guardaremos en el JSON
    private DeathRegistryData data;

    // Clase interna que contiene TODA la información
    private static class DeathRegistryData {
        List<DeathRecord> deathRecords = new ArrayList<>();
        List<String> ignoredPlayers = new ArrayList<>(); // Lista para jugadores ignorados
        Map<String, Integer> playerLives = new HashMap<>(); // Mapa para las vidas
    }

    public static class DeathRecord {
        public String playerName;
        public String playerUUID;
        public String deathTime;
        public String dimension;
        public String skinTextureData;
        public String skinSignature;
        public boolean hasSkinData;

        public DeathRecord() {}

        public DeathRecord(ServerPlayer player) {
            this.playerName = player.getGameProfile().getName();
            this.playerUUID = player.getUUID().toString();
            this.deathTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.dimension = player.level().dimension().location().toString();

            var skinProperty = player.getGameProfile().getProperties().get("textures").stream().findFirst();
            if (skinProperty.isPresent()) {
                this.skinTextureData = skinProperty.get().getValue();
                this.skinSignature = skinProperty.get().getSignature();
                this.hasSkinData = true;
            } else {
                this.hasSkinData = false;
            }
        }
    }

    public void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                FileReader reader = new FileReader(CONFIG_PATH.toFile());
                this.data = GSON.fromJson(reader, DeathRegistryData.class);
                reader.close();
                // Si el archivo existe pero está vacío o malformado, creamos data nueva
                if (this.data == null) {
                    this.data = new DeathRegistryData();
                }
            } else {
                // Si el archivo no existe, creamos data nueva y la guardamos
                this.data = new DeathRegistryData();
                saveConfig();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.data = new DeathRegistryData();
        }
    }

    public void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            // Esta es la línea clave
            GSON.toJson(this.data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDeath(ServerPlayer player) {
        if (data != null) {
            // Evita duplicados en la lista de muertes
            data.deathRecords.removeIf(record -> record.playerUUID.equals(player.getUUID().toString()));
            data.deathRecords.add(new DeathRecord(player));
            saveConfig();
        }
    }

    public List<DeathRecord> getDeathRecords() {
        return this.data != null ? this.data.deathRecords : Collections.emptyList();
    }

    public Optional<DeathRecord> getRecordByUUID(String uuid) {
        if (this.data == null) return Optional.empty();
        return data.deathRecords.stream()
                .filter(record -> record.playerUUID.equals(uuid))
                .findFirst();
    }

    public void removeDeathRecord(String uuid) {
        if (this.data != null) {
            boolean removed = data.deathRecords.removeIf(record -> record.playerUUID.equals(uuid));
            if (removed) {
                saveConfig(); // Guarda los cambios solo si algo fue removido
            }
        }
    }

    // --- Métodos para Jugadores Ignorados ---
    public void addIgnoredPlayer(String uuid) {
        if (this.data != null && !this.data.ignoredPlayers.contains(uuid)) {
            this.data.ignoredPlayers.add(uuid);
            saveConfig();
        }
    }

    public void removeIgnoredPlayer(String uuid) {
        if (this.data != null) {
            boolean removed = this.data.ignoredPlayers.remove(uuid);
            if (removed) {
                saveConfig();
            }
        }
    }

    public List<String> getIgnoredPlayers() {
        return this.data != null ? this.data.ignoredPlayers : Collections.emptyList();
    }

    // --- Métodos para Vidas de Jugadores ---
    public void setPlayerLives(String uuid, int lives) {
        if (this.data != null) {
            this.data.playerLives.put(uuid, lives);
            saveConfig();
        }
    }

    public Map<String, Integer> getPlayerLivesMap() {
        return this.data != null ? this.data.playerLives : Collections.emptyMap();
    }
}