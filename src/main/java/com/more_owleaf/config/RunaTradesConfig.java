package com.more_owleaf.config;

import com.google.gson.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunaTradesConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/more_owleaf/runa_trades.json");
    private static final Map<UUID, MerchantOffers> RUNA_TRADES = new HashMap<>();
    private static boolean configLoaded = false;

    public static boolean loadConfig() {
        System.out.println("=== LOADING RUNA TRADES CONFIG ===");
        System.out.println("Called from: " + Thread.currentThread().getStackTrace()[2].toString());
        System.out.println("Config path: " + CONFIG_PATH.toAbsolutePath());
        System.out.println("Current map size before clearing: " + RUNA_TRADES.size());

        try {
            Map<UUID, MerchantOffers> backupTrades = new HashMap<>(RUNA_TRADES);
            RUNA_TRADES.clear();

            if (Files.exists(CONFIG_PATH)) {
                System.out.println("Config file exists, reading...");

                String jsonContent = Files.readString(CONFIG_PATH);
                System.out.println("JSON Content length: " + jsonContent.length());
                System.out.println("JSON Content: " + jsonContent);

                if (jsonContent.trim().equals("{}")) {
                    System.out.println("JSON file is empty, restoring backup trades...");
                    RUNA_TRADES.putAll(backupTrades);
                    saveConfig();
                    configLoaded = true;
                    return true;
                }

                JsonObject json = GSON.fromJson(jsonContent, JsonObject.class);
                System.out.println("JSON object parsed, keys: " + json.size());

                int tradeCount = 0;
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    System.out.println("Processing entry: " + entry.getKey());
                    try {
                        UUID runaId = UUID.fromString(entry.getKey());
                        JsonArray tradesArray = entry.getValue().getAsJsonArray();
                        System.out.println("Trades array size: " + tradesArray.size());

                        MerchantOffers offers = parseOffers(tradesArray);
                        RUNA_TRADES.put(runaId, offers);
                        tradeCount += offers.size();

                        System.out.println("Successfully loaded " + offers.size() + " trades for runa: " + runaId);
                    } catch (IllegalArgumentException e) {
                        System.err.println("UUID inválido en configuración: " + entry.getKey());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("Error processing entry " + entry.getKey() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                System.out.println("Loaded " + tradeCount + " total trades for " + RUNA_TRADES.size() + " runas");
            } else {
                System.out.println("Config file doesn't exist, creating default...");
                createDefaultConfig();
                System.out.println("Created new empty runa trades config");
            }

            configLoaded = true;
            System.out.println("Config loading completed successfully");
            System.out.println("Final map size: " + RUNA_TRADES.size());
            System.out.println("=== END LOADING RUNA TRADES CONFIG ===");
            return true;

        } catch (Exception e) {
            System.err.println("Error loading runa trades config: " + e.getMessage());
            e.printStackTrace();
            createDefaultConfig();
            configLoaded = false;
            return false;
        }
    }

    public static boolean isConfigLoaded() {
        return configLoaded;
    }

    public static int getLoadedRunaCount() {
        return RUNA_TRADES.size();
    }

    public static void saveConfig() {
        System.out.println("=== SAVING RUNA TRADES CONFIG ===");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject json = new JsonObject();

            for (Map.Entry<UUID, MerchantOffers> entry : RUNA_TRADES.entrySet()) {
                json.add(entry.getKey().toString(), serializeOffers(entry.getValue()));
                System.out.println("Saved " + entry.getValue().size() + " trades for runa: " + entry.getKey());
            }

            String jsonString = GSON.toJson(json);
            Files.writeString(CONFIG_PATH, jsonString);
            System.out.println("Config saved to: " + CONFIG_PATH.toAbsolutePath());
            System.out.println("Total entries saved: " + json.size());

        } catch (Exception e) {
            System.err.println("Error saving runa trades config: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== END SAVING RUNA TRADES CONFIG ===");
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, "{}");
            System.out.println("Created empty runa trades config at: " + CONFIG_PATH.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error creating default config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MerchantOffers getTradesForRuna(UUID runaId) {
        System.out.println("getTradesForRuna called for UUID: " + runaId);
        System.out.println("Current RUNA_TRADES map size: " + RUNA_TRADES.size());
        System.out.println("Config loaded status: " + configLoaded);

        if (!configLoaded) {
            System.out.println("Config not loaded, attempting to load...");
            loadConfig();
        }

        boolean keyExists = RUNA_TRADES.containsKey(runaId);
        System.out.println("Key exists in map: " + keyExists);

        if (keyExists) {
            MerchantOffers offers = RUNA_TRADES.get(runaId);
            System.out.println("Found trades in map: " + offers.size());
            return offers;
        } else {
            System.out.println("No trades found for UUID: " + runaId);
            System.out.println("Available UUIDs in map:");
            for (UUID uuid : RUNA_TRADES.keySet()) {
                System.out.println("  - " + uuid + " (" + RUNA_TRADES.get(uuid).size() + " trades)");
            }
            return new MerchantOffers();
        }
    }

    public static void setTradesForRuna(UUID runaId, MerchantOffers offers) {
        System.out.println("setTradesForRuna(" + runaId + ") with " + offers.size() + " trades");
        RUNA_TRADES.put(runaId, offers);
        saveConfig();

        System.out.println("Verification - RUNA_TRADES now contains: " + RUNA_TRADES.size() + " entries");
        MerchantOffers verification = RUNA_TRADES.get(runaId);
        System.out.println("Verification - Direct map lookup for " + runaId + ": " + (verification != null ? verification.size() : "null") + " trades");
    }

    public static void printAllConfiguredRunas() {
        System.out.println("=== CONFIGURED RUNAS ===");
        for (Map.Entry<UUID, MerchantOffers> entry : RUNA_TRADES.entrySet()) {
            System.out.println("Runa " + entry.getKey() + ": " + entry.getValue().size() + " trades");
        }
        System.out.println("=== END CONFIGURED RUNAS ===");
    }

    private static MerchantOffers parseOffers(JsonArray jsonArray) {
        MerchantOffers offers = new MerchantOffers();
        System.out.println("Parsing " + jsonArray.size() + " offers from JSON");

        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                JsonElement element = jsonArray.get(i);
                JsonObject trade = element.getAsJsonObject();

                System.out.println("Parsing trade " + i + ": " + trade.toString());

                ItemStack price1 = parseItemStack(trade.getAsJsonObject("price1"));
                ItemStack price2 = trade.has("price2") ? parseItemStack(trade.getAsJsonObject("price2")) : ItemStack.EMPTY;
                ItemStack result = parseItemStack(trade.getAsJsonObject("result"));

                int maxUses = trade.get("maxUses").getAsInt();
                int xp = trade.get("xp").getAsInt();
                float priceMultiplier = trade.get("priceMultiplier").getAsFloat();

                MerchantOffer offer = new MerchantOffer(price1, price2, result, maxUses, xp, priceMultiplier);
                offers.add(offer);

                System.out.println("Successfully parsed trade: " + price1.getDisplayName().getString() +
                        " -> " + result.getDisplayName().getString());

            } catch (Exception e) {
                System.err.println("Error parsing trade " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Finished parsing, total offers: " + offers.size());
        return offers;
    }

    private static JsonArray serializeOffers(MerchantOffers offers) {
        JsonArray array = new JsonArray();
        for (MerchantOffer offer : offers) {
            JsonObject trade = new JsonObject();
            trade.add("price1", serializeItemStack(offer.getBaseCostA()));
            trade.add("price2", serializeItemStack(offer.getCostB()));
            trade.add("result", serializeItemStack(offer.getResult()));
            trade.addProperty("maxUses", offer.getMaxUses());
            trade.addProperty("xp", offer.getXp());
            trade.addProperty("priceMultiplier", offer.getPriceMultiplier());
            array.add(trade);
        }
        return array;
    }

    private static ItemStack parseItemStack(JsonObject json) {
        try {
            String itemId = json.get("item").getAsString();
            int count = json.get("count").getAsInt();

            System.out.println("Parsing ItemStack: " + itemId + " x" + count);

            Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(itemId));
            if (item == null || item == Items.AIR) {
                System.err.println("Item not found or is AIR: " + itemId);
                return ItemStack.EMPTY;
            }

            ItemStack stack = new ItemStack(item, count);
            System.out.println("Created ItemStack: " + stack.getDisplayName().getString() + " x" + stack.getCount());
            return stack;

        } catch (Exception e) {
            System.err.println("Error parsing ItemStack from JSON: " + json.toString());
            e.printStackTrace();
            return ItemStack.EMPTY;
        }
    }

    private static JsonObject serializeItemStack(ItemStack stack) {
        JsonObject json = new JsonObject();
        if (stack.isEmpty()) {
            json.addProperty("item", "minecraft:air");
            json.addProperty("count", 0);
        } else {
            json.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
            json.addProperty("count", stack.getCount());
        }
        return json;
    }
}