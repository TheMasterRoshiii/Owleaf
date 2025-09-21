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
import java.util.*;

public class RunaTradesConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/more_owleaf/runa_trades.json");
    private static final Map<String, MerchantOffers> RUNA_TYPE_TRADES = new HashMap<>();
    private static final Set<String> REGISTERED_RUNA_TYPES = new HashSet<>();
    private static boolean configLoaded = false;

    public static void registerRunaType(String runaType) {
        REGISTERED_RUNA_TYPES.add(runaType);
        if (configLoaded && !RUNA_TYPE_TRADES.containsKey(runaType)) {
            RUNA_TYPE_TRADES.put(runaType, createDefaultTradesForType(runaType));
            saveConfig();
        }
    }

    public static boolean loadConfig() {
        try {
            RUNA_TYPE_TRADES.clear();

            if (Files.exists(CONFIG_PATH)) {
                String jsonContent = Files.readString(CONFIG_PATH);

                if (jsonContent.trim().equals("{}") || jsonContent.trim().isEmpty()) {
                    createDefaultConfig();
                    configLoaded = true;
                    return true;
                }

                try {
                    JsonObject json = GSON.fromJson(jsonContent, JsonObject.class);
                    if (json == null) {
                        createDefaultConfig();
                        configLoaded = true;
                        return true;
                    }

                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        String runaType = entry.getKey();

                        if (!entry.getValue().isJsonArray()) {
                            continue;
                        }

                        JsonArray tradesArray = entry.getValue().getAsJsonArray();
                        MerchantOffers offers = parseOffersArray(tradesArray);

                        if (!offers.isEmpty()) {
                            RUNA_TYPE_TRADES.put(runaType, offers);
                        }
                    }

                    ensureAllRunaTypesHaveConfig();

                } catch (JsonSyntaxException e) {
                    createDefaultConfig();
                    configLoaded = true;
                    return true;
                }
            } else {
                createDefaultConfig();
            }

            configLoaded = true;
            return true;

        } catch (Exception e) {
            createDefaultConfig();
            configLoaded = false;
            return false;
        }
    }

    private static MerchantOffers parseOffersArray(JsonArray tradesArray) {
        MerchantOffers offers = new MerchantOffers();

        for (int i = 0; i < tradesArray.size(); i++) {
            try {
                if (!tradesArray.get(i).isJsonObject()) {
                    continue;
                }

                JsonObject tradeObj = tradesArray.get(i).getAsJsonObject();
                MerchantOffer offer = parseTradeObject(tradeObj);
                if (offer != null) {
                    offers.add(offer);
                }
            } catch (Exception e) {
            }
        }

        return offers;
    }

    private static MerchantOffer parseTradeObject(JsonObject tradeObj) {
        try {
            if (!tradeObj.has("price1") || !tradeObj.has("result")) {
                return null;
            }

            if (!tradeObj.get("price1").isJsonObject() || !tradeObj.get("result").isJsonObject()) {
                return null;
            }

            ItemStack price1 = parseItemStack(tradeObj.getAsJsonObject("price1"));
            if (price1.isEmpty()) {
                return null;
            }

            ItemStack price2 = ItemStack.EMPTY;
            if (tradeObj.has("price2") && tradeObj.get("price2").isJsonObject()) {
                JsonObject price2Obj = tradeObj.getAsJsonObject("price2");
                if (price2Obj.has("item") && !price2Obj.get("item").getAsString().equals("minecraft:air")) {
                    ItemStack parsedPrice2 = parseItemStack(price2Obj);
                    if (!parsedPrice2.isEmpty()) {
                        price2 = parsedPrice2;
                    }
                }
            }

            ItemStack result = parseItemStack(tradeObj.getAsJsonObject("result"));
            if (result.isEmpty()) {
                return null;
            }

            int maxUses = Integer.MAX_VALUE;
            if (tradeObj.has("maxUses") && tradeObj.get("maxUses").isJsonPrimitive()) {
                try {
                    maxUses = tradeObj.get("maxUses").getAsInt();
                } catch (Exception e) {
                    maxUses = Integer.MAX_VALUE;
                }
            }

            int xp = 0;
            if (tradeObj.has("xp") && tradeObj.get("xp").isJsonPrimitive()) {
                try {
                    xp = tradeObj.get("xp").getAsInt();
                } catch (Exception e) {
                    xp = 0;
                }
            }

            float priceMultiplier = 0.0f;
            if (tradeObj.has("priceMultiplier") && tradeObj.get("priceMultiplier").isJsonPrimitive()) {
                try {
                    priceMultiplier = tradeObj.get("priceMultiplier").getAsFloat();
                } catch (Exception e) {
                    priceMultiplier = 0.0f;
                }
            }

            return new MerchantOffer(price1, price2, result, maxUses, xp, priceMultiplier);

        } catch (Exception e) {
            return null;
        }
    }

    private static void ensureAllRunaTypesHaveConfig() {
        boolean needsSave = false;

        for (String runaType : REGISTERED_RUNA_TYPES) {
            if (!RUNA_TYPE_TRADES.containsKey(runaType)) {
                RUNA_TYPE_TRADES.put(runaType, createDefaultTradesForType(runaType));
                needsSave = true;
            }
        }

        if (needsSave) {
            saveConfig();
        }
    }

    private static void createDefaultConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            Set<String> allRunaTypes = new HashSet<>(REGISTERED_RUNA_TYPES);
            if (allRunaTypes.isEmpty()) {
                allRunaTypes.addAll(Arrays.asList(
                        "runa_amarilla", "runa_azul_claro", "runa_magenta",
                        "runa_morada", "runa_roja", "runa_verde"
                ));
            }

            JsonObject defaultConfig = new JsonObject();

            for (String runaType : allRunaTypes) {
                JsonArray tradesArray = createDefaultTradesJson(runaType);
                defaultConfig.add(runaType, tradesArray);

                MerchantOffers offers = parseOffersArray(tradesArray);
                RUNA_TYPE_TRADES.put(runaType, offers);
            }

            Files.writeString(CONFIG_PATH, GSON.toJson(defaultConfig));

        } catch (Exception e) {
        }
    }

    private static JsonArray createDefaultTradesJson(String runaType) {
        JsonArray tradesArray = new JsonArray();

        JsonObject trade1 = new JsonObject();
        JsonObject price1_1 = new JsonObject();
        price1_1.addProperty("item", "minecraft:emerald");
        price1_1.addProperty("count", 1);
        JsonObject result1 = new JsonObject();
        result1.addProperty("item", "minecraft:diamond");
        result1.addProperty("count", 1);
        trade1.add("price1", price1_1);
        trade1.add("result", result1);
        trade1.addProperty("maxUses", Integer.MAX_VALUE);
        trade1.addProperty("xp", 0);
        trade1.addProperty("priceMultiplier", 0.0);
        tradesArray.add(trade1);

        JsonObject trade2 = new JsonObject();
        JsonObject price1_2 = new JsonObject();
        price1_2.addProperty("item", "minecraft:iron_ingot");
        price1_2.addProperty("count", 3);
        JsonObject result2 = new JsonObject();
        result2.addProperty("item", "minecraft:emerald");
        result2.addProperty("count", 1);
        trade2.add("price1", price1_2);
        trade2.add("result", result2);
        trade2.addProperty("maxUses", Integer.MAX_VALUE);
        trade2.addProperty("xp", 0);
        trade2.addProperty("priceMultiplier", 0.0);
        tradesArray.add(trade2);

        return tradesArray;
    }

    private static MerchantOffers createDefaultTradesForType(String runaType) {
        MerchantOffers offers = new MerchantOffers();

        offers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                ItemStack.EMPTY,
                new ItemStack(Items.DIAMOND, 1),
                Integer.MAX_VALUE, 0, 0.0f
        ));

        offers.add(new MerchantOffer(
                new ItemStack(Items.IRON_INGOT, 3),
                ItemStack.EMPTY,
                new ItemStack(Items.EMERALD, 1),
                Integer.MAX_VALUE, 0, 0.0f
        ));

        return offers;
    }

    public static MerchantOffers getTradesForRunaType(String runaType) {
        if (!configLoaded) {
            loadConfig();
        }

        MerchantOffers offers = RUNA_TYPE_TRADES.get(runaType);
        if (offers == null || offers.isEmpty()) {
            offers = createDefaultTradesForType(runaType);
            RUNA_TYPE_TRADES.put(runaType, offers);
            saveConfig();
        }

        return offers;
    }

    public static void setTradesForRunaType(String runaType, MerchantOffers offers) {
        RUNA_TYPE_TRADES.put(runaType, offers);
        saveConfig();
    }

    public static void saveConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            JsonObject json = new JsonObject();

            for (Map.Entry<String, MerchantOffers> entry : RUNA_TYPE_TRADES.entrySet()) {
                json.add(entry.getKey(), serializeOffers(entry.getValue()));
            }

            Files.writeString(CONFIG_PATH, GSON.toJson(json));

        } catch (Exception e) {
        }
    }

    private static JsonArray serializeOffers(MerchantOffers offers) {
        JsonArray array = new JsonArray();
        for (MerchantOffer offer : offers) {
            JsonObject trade = new JsonObject();
            trade.add("price1", serializeItemStack(offer.getBaseCostA()));
            if (!offer.getCostB().isEmpty()) {
                trade.add("price2", serializeItemStack(offer.getCostB()));
            }
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
            if (!json.has("item") || !json.has("count")) {
                return ItemStack.EMPTY;
            }

            if (!json.get("item").isJsonPrimitive() || !json.get("count").isJsonPrimitive()) {
                return ItemStack.EMPTY;
            }

            String itemId = json.get("item").getAsString().trim();
            int count = json.get("count").getAsInt();

            if (itemId.isEmpty() || itemId.equals("minecraft:air") || count <= 0) {
                return ItemStack.EMPTY;
            }

            Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(itemId));
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(item, count);

        } catch (Exception e) {
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

    public static boolean isConfigLoaded() {
        return configLoaded;
    }

    public static int getLoadedRunaCount() {
        return RUNA_TYPE_TRADES.size();
    }

    public static MerchantOffers getTradesForRuna(UUID runaId) {
        return new MerchantOffers();
    }

    public static void setTradesForRuna(UUID runaId, MerchantOffers offers) {
    }
}
