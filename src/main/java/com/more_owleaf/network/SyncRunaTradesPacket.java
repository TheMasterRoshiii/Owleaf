package com.more_owleaf.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncRunaTradesPacket {
    private final MerchantOffers offers;

    public SyncRunaTradesPacket(MerchantOffers offers) {
        this.offers = offers;
    }

    public static void encode(SyncRunaTradesPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.offers.createTag());
    }

    public static SyncRunaTradesPacket decode(FriendlyByteBuf buffer) {
        MerchantOffers offers = new MerchantOffers(buffer.readNbt());
        return new SyncRunaTradesPacket(offers);
    }

    public static void handle(SyncRunaTradesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {

        });
        context.setPacketHandled(true);
    }
}
