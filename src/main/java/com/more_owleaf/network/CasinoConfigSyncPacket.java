package com.more_owleaf.network;

import com.more_owleaf.config.CasinoConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class CasinoConfigSyncPacket {
    private final String interactionItem;
    private final boolean casinoEnabled;

    public CasinoConfigSyncPacket(String interactionItem, boolean casinoEnabled) {
        this.interactionItem = interactionItem;
        this.casinoEnabled = casinoEnabled;
    }

    public static CasinoConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new CasinoConfigSyncPacket(buf.readUtf(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(interactionItem);
        buf.writeBoolean(casinoEnabled);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Item serverItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(interactionItem));
            if (serverItem != null) {
                CasinoConfig.INTERACTION_ITEM = serverItem;
            }
            CasinoConfig.CASINO_ENABLED = casinoEnabled;
        });
        context.get().setPacketHandled(true);
    }
}