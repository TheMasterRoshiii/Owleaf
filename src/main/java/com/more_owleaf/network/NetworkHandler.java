package com.more_owleaf.network;

import com.more_owleaf.More_Owleaf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(More_Owleaf.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, DeathDataPacket.class,
                DeathDataPacket::toBytes, DeathDataPacket::new, DeathDataPacket::handle);

        INSTANCE.registerMessage(packetId++, CasinoConfigSyncPacket.class,
                CasinoConfigSyncPacket::encode,
                CasinoConfigSyncPacket::decode,
                CasinoConfigSyncPacket::handle);
    }
}