package com.more_owleaf.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class FogataConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public enum MetodoExpulsion {
        WHITELIST, BAN, BANIP
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue almasRequeridas;
        public final ForgeConfigSpec.IntValue vidasIniciales;
        public final ForgeConfigSpec.ConfigValue<String> itemCuchara;
        public final ForgeConfigSpec.ConfigValue<String> itemTenedor;
        public final ForgeConfigSpec.EnumValue<MetodoExpulsion> metodoExpulsion;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Configuración de la Fogata")
                    .push("fogata");

            almasRequeridas = builder
                    .comment("Número de almas")
                    .defineInRange("almasRequeridas", 1, 0, 10);

            vidasIniciales = builder
                    .comment("Número de vidas")
                    .defineInRange("vidasIniciales", 1, 1, 10);

            itemCuchara = builder
                    .comment("Uso de alma (formato: modid:item)")
                    .define("itemCuchara", "minecraft:stick");

            itemTenedor = builder
                    .comment("No uso de alma (formato: modid:item)")
                    .define("itemTenedor", "minecraft:diamond");

            metodoExpulsion = builder
                    .comment("Define el método por el cual un jugador es expulsado al morir y cómo es reingresado al ser revivido.",
                            "WHITELIST: Se remueve de la whitelist.",
                            "BAN: Se banea al jugador.",
                            "BANIP: Se banea la IP del jugador.")
                    .defineEnum("metodoExpulsion", MetodoExpulsion.WHITELIST);

            builder.pop();
        }
    }

    public Item getItemCuchara() {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(COMMON.itemCuchara.get()));
    }

    public Item getItemTenedor() {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(COMMON.itemTenedor.get()));
    }
}