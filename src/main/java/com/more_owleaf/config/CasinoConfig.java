package com.more_owleaf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CasinoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/more_owleaf/casino_config.json");

    public static int WINNING_CHANCE = 10;
    public static int SEMI_WINNING_CHANCE = 30;
    public static int LOSING_CHANCE = 60;

    public static Item INTERACTION_ITEM = Items.BEDROCK;
    public static int PRIZE_COOLDOWN_TICKS = 100;

    public static boolean CASINO_ENABLED = true;

    public static String WIN_MESSAGE = "¡Ganaste %quantity%x %item%!";
    public static String WIN_MESSAGE_COLOR = "#55FF55";

    public static final Map<String, PrizeEntry> PRIZE_MAP = new HashMap<>();

    public static class PrizeEntry {
        public final List<WeightedPrize> prizes;

        public PrizeEntry() {
            this.prizes = new ArrayList<>();
        }

        public void addPrize(ItemStack item, int weight) {
            prizes.add(new WeightedPrize(item, weight));
        }

        public ItemStack getRandomPrize() {
            if (prizes.isEmpty()) return ItemStack.EMPTY;

            int totalWeight = prizes.stream().mapToInt(p -> p.weight).sum();
            if (totalWeight <= 0) return ItemStack.EMPTY;

            Random random = new Random();
            int randomValue = random.nextInt(totalWeight);
            int currentWeight = 0;

            for (WeightedPrize prize : prizes) {
                currentWeight += prize.weight;
                if (randomValue < currentWeight) {
                    return prize.item.copy();
                }
            }

            return prizes.get(0).item.copy();
        }

        public boolean isEmpty() {
            return prizes.isEmpty();
        }
    }

    public static class WeightedPrize {
        public final ItemStack item;
        public final int weight;

        public WeightedPrize(ItemStack item, int weight) {
            this.item = item;
            this.weight = weight;
        }
    }

    public static boolean loadConfig() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
                System.out.println("Created default casino config file");
                return true;
            }

            String jsonContent = Files.readString(CONFIG_PATH);
            JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();

            if (json.has("probabilities")) {
                JsonObject probabilities = json.getAsJsonObject("probabilities");
                WINNING_CHANCE = getIntValue(probabilities, "winning_chance", 8);
                SEMI_WINNING_CHANCE = getIntValue(probabilities, "semi_winning_chance", 32);
                LOSING_CHANCE = getIntValue(probabilities, "losing_chance", 60);

                int total = WINNING_CHANCE + SEMI_WINNING_CHANCE + LOSING_CHANCE;
                if (total > 100) {
                    System.out.println("Warning: Probabilities sum exceeds 100%, normalizing...");
                    WINNING_CHANCE = (WINNING_CHANCE * 100) / total;
                    SEMI_WINNING_CHANCE = (SEMI_WINNING_CHANCE * 100) / total;
                    LOSING_CHANCE = 100 - WINNING_CHANCE - SEMI_WINNING_CHANCE;
                }
            }

            if (json.has("settings")) {
                JsonObject settings = json.getAsJsonObject("settings");

                if (settings.has("interaction_item")) {
                    String itemString = settings.get("interaction_item").getAsString();
                    ItemStack itemStack = parseItemStack(itemString + "*1");
                    if (!itemStack.isEmpty()) {
                        INTERACTION_ITEM = itemStack.getItem();
                        System.out.println("Loaded interaction item: " + INTERACTION_ITEM);
                    } else {
                        System.out.println("Failed to load interaction item, using default: paper");
                        INTERACTION_ITEM = Items.PAPER;
                    }
                } else {
                    INTERACTION_ITEM = Items.PAPER;
                }

                PRIZE_COOLDOWN_TICKS = getIntValue(settings, "prize_cooldown_seconds", 5) * 20;
                if (PRIZE_COOLDOWN_TICKS < 0) PRIZE_COOLDOWN_TICKS = 100;

                CASINO_ENABLED = getBooleanValue(settings, "casino_enabled", true);
            } else {
                INTERACTION_ITEM = Items.PAPER;
                PRIZE_COOLDOWN_TICKS = 100;
                CASINO_ENABLED = true;
            }

            if (json.has("messages")) {
                JsonObject messages = json.getAsJsonObject("messages");
                WIN_MESSAGE = getStringValue(messages, "win_message", "¡Ganaste %quantity%x %item%!");
                WIN_MESSAGE_COLOR = getStringValue(messages, "win_message_color", "#55FF55");
            } else {
                WIN_MESSAGE = "¡Ganaste %quantity%x %item%!";
                WIN_MESSAGE_COLOR = "#55FF55";
            }

            // Cargar premios
            if (json.has("prizes")) {
                JsonObject prizes = json.getAsJsonObject("prizes");
                PRIZE_MAP.clear();

                for (String key : prizes.keySet()) {
                    String prizeString = prizes.get(key).getAsString();
                    PrizeEntry prizeEntry = parsePrizeEntry(prizeString);
                    if (prizeEntry != null) {
                        PRIZE_MAP.put(key, prizeEntry);
                        System.out.println("Loaded prize: " + key + " -> " + prizeString);
                    } else {
                        System.out.println("Failed to load prize: " + key + " -> " + prizeString);
                    }
                }
            }

            System.out.println("Casino config loaded successfully:");
            System.out.println("  Casino enabled: " + CASINO_ENABLED);
            System.out.println("  Winning chance: " + WINNING_CHANCE + "%");
            System.out.println("  Semi-winning chance: " + SEMI_WINNING_CHANCE + "%");
            System.out.println("  Losing chance: " + LOSING_CHANCE + "%");
            System.out.println("  Interaction item: " + INTERACTION_ITEM);
            System.out.println("  Prize cooldown: " + (PRIZE_COOLDOWN_TICKS / 20) + " seconds");
            System.out.println("  Win message: " + WIN_MESSAGE);
            System.out.println("  Win message color: " + WIN_MESSAGE_COLOR);
            System.out.println("  Total prizes: " + PRIZE_MAP.size());

            return true;

        } catch (Exception e) {
            System.out.println("Error loading casino config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static int getIntValue(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsInt();
        }
        return defaultValue;
    }

    private static boolean getBooleanValue(JsonObject json, String key, boolean defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsBoolean();
        }
        return defaultValue;
    }

    private static String getStringValue(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }

    private static void createDefaultConfig() {
        try {
            JsonObject config = new JsonObject();

            JsonObject settings = new JsonObject();
            settings.addProperty("interaction_item", "minecraft:paper");
            settings.addProperty("prize_cooldown_seconds", 9);
            settings.addProperty("casino_enabled", true);
            config.add("settings", settings);

            JsonObject messages = new JsonObject();
            messages.addProperty("win_message", "¡Ganaste %quantity%x %item%!");
            messages.addProperty("win_message_color", "#55FF55");
            config.add("messages", messages);

            JsonObject probabilities = new JsonObject();
            probabilities.addProperty("winning_chance", 10);
            probabilities.addProperty("semi_winning_chance", 30);
            probabilities.addProperty("losing_chance", 60);
            config.add("probabilities", probabilities);

            JsonObject prizes = new JsonObject();
            prizes.addProperty("animacion_ganadora_pluma", "minecraft:emerald*3");
            prizes.addProperty("animacion_ganadora_eon", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_brocoli", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_dedita", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_nutria", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_corazon", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_oro", "minecraft:diamond*1");
            prizes.addProperty("animacion_ganadora_pocion", "minecraft:diamond*1");

            for (int i = 1; i <= 56; i++) {
                prizes.addProperty("animacion_semi_ganadora_" + i, "minecraft:iron_ingot*2");
            }

            for (int i = 1; i <= 60; i++) {
                prizes.addProperty("animacion_aleatoria_" + i, "NA");
            }

            config.add("prizes", prizes);

            Files.writeString(CONFIG_PATH, GSON.toJson(config));
            System.out.println("Default casino config created at: " + CONFIG_PATH);

        } catch (IOException e) {
            System.out.println("Error creating default config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ItemStack getPrizeForAnimation(String animationName) {
        PrizeEntry prizeEntry = PRIZE_MAP.get(animationName);
        if (prizeEntry != null && !prizeEntry.isEmpty()) {
            ItemStack prize = prizeEntry.getRandomPrize();
            if (!prize.isEmpty()) {
                return prize;
            }
        }

        if (animationName.equals("animacion_ganadora_pluma")) {
            return new ItemStack(Items.EMERALD, 3);
        } else if (animationName.equals("animacion_ganadora_eon") ||
                animationName.equals("animacion_ganadora_brocoli") ||
                animationName.equals("animacion_ganadora_dedita") ||
                animationName.equals("animacion_ganadora_nutria") ||
                animationName.equals("animacion_ganadora_corazon") ||
                animationName.equals("animacion_ganadora_oro") ||
                animationName.equals("animacion_ganadora_pocion")) {
            return new ItemStack(Items.DIAMOND, 1);
        } else if (animationName.startsWith("animacion_semi_ganadora")) {
            return new ItemStack(Items.IRON_INGOT, 2);
        }

        return ItemStack.EMPTY;
    }

    public static Component formatWinMessage(ItemStack prize) {
        String itemName = prize.getDisplayName().getString();
        int quantity = prize.getCount();

        String formattedMessage = WIN_MESSAGE
                .replace("%quantity%", String.valueOf(quantity))
                .replace("%item%", itemName);

        int color = parseHexColor(WIN_MESSAGE_COLOR);

        return Component.literal(formattedMessage)
                .withStyle(style -> style.withColor(color));
    }

    public static int parseHexColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            return Integer.parseInt(hexColor, 16);
        } catch (NumberFormatException e) {
            System.out.println("Invalid hex color: " + hexColor + ", using default green");
            return 0x55FF55;
        }
    }

    public static PrizeEntry parsePrizeEntry(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return null;
            }

            input = input.trim();

            if ("NA".equalsIgnoreCase(input)) {
                return new PrizeEntry();
            }

            PrizeEntry prizeEntry = new PrizeEntry();
            String[] prizeOptions = input.split(",");

            for (String option : prizeOptions) {
                option = option.trim();

                if (option.contains("%")) {
                    String[] parts = option.split("%", 2);
                    if (parts.length == 2) {
                        try {
                            int percentage = Integer.parseInt(parts[0].trim());
                            String itemString = parts[1].trim();

                            ItemStack item = parseItemStack(itemString);
                            if (!item.isEmpty()) {
                                prizeEntry.addPrize(item, percentage);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid percentage in: " + option);
                        }
                    }
                } else {
                    ItemStack item = parseItemStack(option);
                    if (!item.isEmpty()) {
                        prizeEntry.addPrize(item, 100);
                    }
                }
            }

            return prizeEntry.isEmpty() ? null : prizeEntry;

        } catch (Exception e) {
            System.out.println("Error parsing prize entry: " + input + " - " + e.getMessage());
            return null;
        }
    }

    public static ItemStack parseItemStack(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return ItemStack.EMPTY;
            }

            String[] parts = input.split("\\*");
            String itemId = parts[0].trim();
            int count = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;

            if (count < 1) count = 1;
            if (count > 64) count = 64;

            ResourceLocation resourceLocation = ResourceLocation.tryParse(itemId);
            if (resourceLocation == null) {
                System.out.println("Invalid resource location: " + itemId);
                return ItemStack.EMPTY;
            }

            Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item, count);
            }
            System.out.println("Item not found: " + itemId);
            return ItemStack.EMPTY;

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in: " + input);
            return ItemStack.EMPTY;
        } catch (Exception e) {
            System.out.println("Error parsing item stack: " + input + " - " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    public static boolean saveConfig() {
        try {
            JsonObject config = new JsonObject();

            JsonObject settings = new JsonObject();
            settings.addProperty("interaction_item", ForgeRegistries.ITEMS.getKey(INTERACTION_ITEM).toString());
            settings.addProperty("prize_cooldown_seconds", PRIZE_COOLDOWN_TICKS / 20);
            settings.addProperty("casino_enabled", CASINO_ENABLED);
            config.add("settings", settings);

            JsonObject messages = new JsonObject();
            messages.addProperty("win_message", WIN_MESSAGE);
            messages.addProperty("win_message_color", WIN_MESSAGE_COLOR);
            config.add("messages", messages);

            JsonObject probabilities = new JsonObject();
            probabilities.addProperty("winning_chance", WINNING_CHANCE);
            probabilities.addProperty("semi_winning_chance", SEMI_WINNING_CHANCE);
            probabilities.addProperty("losing_chance", LOSING_CHANCE);
            config.add("probabilities", probabilities);

            JsonObject prizes = new JsonObject();
            for (Map.Entry<String, PrizeEntry> entry : PRIZE_MAP.entrySet()) {
                PrizeEntry prizeEntry = entry.getValue();
                if (prizeEntry.isEmpty()) {
                    prizes.addProperty(entry.getKey(), "NA");
                } else {
                    StringBuilder prizeString = new StringBuilder();
                    for (int i = 0; i < prizeEntry.prizes.size(); i++) {
                        WeightedPrize weightedPrize = prizeEntry.prizes.get(i);
                        if (i > 0) prizeString.append(",");

                        prizeString.append(weightedPrize.weight).append("%")
                                .append(ForgeRegistries.ITEMS.getKey(weightedPrize.item.getItem()).toString())
                                .append("*").append(weightedPrize.item.getCount());
                    }
                    prizes.addProperty(entry.getKey(), prizeString.toString());
                }
            }
            config.add("prizes", prizes);

            Files.writeString(CONFIG_PATH, GSON.toJson(config));
            System.out.println("Casino config saved successfully");
            return true;

        } catch (IOException e) {
            System.out.println("Error saving config: " + e.getMessage());
            return false;
        }
    }

    public static String getConfigPath() {
        return CONFIG_PATH.toAbsolutePath().toString();
    }

    public static boolean validateProbabilities() {
        int total = WINNING_CHANCE + SEMI_WINNING_CHANCE + LOSING_CHANCE;
        return total <= 100;
    }
}