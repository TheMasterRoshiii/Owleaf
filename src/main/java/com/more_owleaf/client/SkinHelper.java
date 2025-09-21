package com.more_owleaf.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinHelper {
    private static final ConcurrentHashMap<String, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, GameProfile> PROFILE_CACHE = new ConcurrentHashMap<>();

    public static ResourceLocation getPlayerSkinFromData(String playerName, String playerUUID,
                                                         String skinTextureData, String skinSignature,
                                                         boolean hasSkinData) {
        String cacheKey = playerName.toLowerCase() + "_" + playerUUID + "_" + skinTextureData.hashCode();

        if (SKIN_CACHE.containsKey(cacheKey)) {
            return SKIN_CACHE.get(cacheKey);
        }

        ResourceLocation skinLocation = null;

        if (hasSkinData && skinTextureData != null && !skinTextureData.isEmpty()) {
            skinLocation = createSkinFromTextureData(playerName, playerUUID, skinTextureData, skinSignature);
        }

        if (skinLocation == null) {
            skinLocation = getFallbackSkin(playerUUID);
        }

        SKIN_CACHE.put(cacheKey, skinLocation);
        return skinLocation;
    }

    public static ResourceLocation getPlayerSkinLocation(String playerName, String playerUUID) {
        String cacheKey = playerName.toLowerCase() + "_" + playerUUID;

        if (SKIN_CACHE.containsKey(cacheKey)) {
            return SKIN_CACHE.get(cacheKey);
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.getConnection() != null) {
            ResourceLocation skinFromWorld = getSkinFromWorld(playerName, playerUUID, mc);
            if (skinFromWorld != null) {
                SKIN_CACHE.put(cacheKey, skinFromWorld);
                return skinFromWorld;
            }

            ResourceLocation skinFromOnlinePlayers = getSkinFromOnlinePlayers(playerName, playerUUID, mc);
            if (skinFromOnlinePlayers != null) {
                SKIN_CACHE.put(cacheKey, skinFromOnlinePlayers);
                return skinFromOnlinePlayers;
            }
        }

        ResourceLocation fallbackSkin = getFallbackSkin(playerUUID);
        SKIN_CACHE.put(cacheKey, fallbackSkin);
        return fallbackSkin;
    }

    private static ResourceLocation getSkinFromWorld(String playerName, String playerUUID, Minecraft mc) {
        if (mc.level == null) return null;

        for (Player worldPlayer : mc.level.players()) {
            String worldPlayerName = worldPlayer.getName().getString();
            String worldPlayerUUID = worldPlayer.getUUID().toString();

            if (worldPlayerName.equalsIgnoreCase(playerName) || worldPlayerUUID.equals(playerUUID)) {
                GameProfile worldProfile = worldPlayer.getGameProfile();

                if (worldProfile.getProperties().containsKey("textures")) {
                    Property textureProperty = worldProfile.getProperties().get("textures").iterator().next();

                    ResourceLocation customSkin = createSkinFromProfile(worldProfile, playerName);
                    if (customSkin != null) {
                        return customSkin;
                    }
                }

                SkinManager skinManager = mc.getSkinManager();
                ResourceLocation directSkin = skinManager.getInsecureSkinLocation(worldProfile);
                return directSkin;
            }
        }
        return null;
    }

    private static ResourceLocation getSkinFromOnlinePlayers(String playerName, String playerUUID, Minecraft mc) {
        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();

        for (PlayerInfo playerInfo : players) {
            GameProfile profile = playerInfo.getProfile();
            String profileName = profile.getName();
            String profileUUID = profile.getId() != null ? profile.getId().toString() : "";

            if (profileName != null && (profileName.equalsIgnoreCase(playerName) || profileUUID.equals(playerUUID))) {
                ResourceLocation onlineSkin = playerInfo.getSkinLocation();
                PROFILE_CACHE.put(playerName.toLowerCase() + "_" + playerUUID, profile);
                return onlineSkin;
            }
        }
        return null;
    }

    private static ResourceLocation createSkinFromProfile(GameProfile profile, String playerName) {
        try {
            if (!profile.getProperties().containsKey("textures")) {
                return null;
            }

            Property textureProperty = profile.getProperties().get("textures").iterator().next();
            String textureValue = textureProperty.getValue();
            String signature = textureProperty.getSignature();

            if (textureValue == null || textureValue.isEmpty()) {
                return null;
            }

            String decoded = new String(java.util.Base64.getDecoder().decode(textureValue));
            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(decoded).getAsJsonObject();

            if (jsonObject.has("textures") && jsonObject.getAsJsonObject("textures").has("SKIN")) {
                com.google.gson.JsonObject skinObject = jsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN");
                if (skinObject.has("url")) {
                    GameProfile tempProfile = new GameProfile(profile.getId(), profile.getName());
                    tempProfile.getProperties().put("textures", new Property("textures", textureValue, signature));

                    SkinManager skinManager = Minecraft.getInstance().getSkinManager();
                    ResourceLocation customSkin = skinManager.getInsecureSkinLocation(tempProfile);
                    return customSkin;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static ResourceLocation createSkinFromTextureData(String playerName, String playerUUID,
                                                              String textureData, String signature) {
        try {
            UUID uuid;
            try {
                uuid = UUID.fromString(playerUUID);
            } catch (Exception e) {
                uuid = UUID.nameUUIDFromBytes(playerName.getBytes());
            }

            GameProfile tempProfile = new GameProfile(uuid, playerName);
            tempProfile.getProperties().put("textures", new Property("textures", textureData, signature));

            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            return skinManager.getInsecureSkinLocation(tempProfile);

        } catch (Exception e) {
            return null;
        }
    }

    private static ResourceLocation getFallbackSkin(String playerUUID) {
        try {
            UUID uuid = UUID.fromString(playerUUID);
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        } catch (Exception e) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
    }

    public static GameProfile getPlayerProfile(String playerName, String playerUUID) {
        String cacheKey = playerName.toLowerCase() + "_" + playerUUID;

        if (PROFILE_CACHE.containsKey(cacheKey)) {
            return PROFILE_CACHE.get(cacheKey);
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            for (Player worldPlayer : mc.level.players()) {
                String worldPlayerName = worldPlayer.getName().getString();
                String worldPlayerUUID = worldPlayer.getUUID().toString();

                if (worldPlayerName.equalsIgnoreCase(playerName) || worldPlayerUUID.equals(playerUUID)) {
                    GameProfile worldProfile = worldPlayer.getGameProfile();
                    PROFILE_CACHE.put(cacheKey, worldProfile);
                    return worldProfile;
                }
            }
        }

        if (mc.getConnection() != null) {
            Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo playerInfo : players) {
                GameProfile profile = playerInfo.getProfile();
                String profileName = profile.getName();
                String profileUUID = profile.getId() != null ? profile.getId().toString() : "";

                if (profileName != null && (profileName.equalsIgnoreCase(playerName) || profileUUID.equals(playerUUID))) {
                    PROFILE_CACHE.put(cacheKey, profile);
                    return profile;
                }
            }
        }

        GameProfile fallbackProfile;
        try {
            UUID uuid = UUID.fromString(playerUUID);
            fallbackProfile = new GameProfile(uuid, playerName);
        } catch (Exception e) {
            fallbackProfile = new GameProfile(null, playerName);
        }

        PROFILE_CACHE.put(cacheKey, fallbackProfile);
        return fallbackProfile;
    }

    public static void refreshPlayerSkin(String playerName, String playerUUID) {
        String cacheKey = playerName.toLowerCase() + "_" + playerUUID;
        SKIN_CACHE.remove(cacheKey);
        PROFILE_CACHE.remove(cacheKey);
    }

    public static void clearCache() {
        SKIN_CACHE.clear();
        PROFILE_CACHE.clear();
    }
}
