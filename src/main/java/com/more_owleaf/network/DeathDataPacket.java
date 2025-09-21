package com.more_owleaf.network;

import com.more_owleaf.client.gui.ReviveMenuScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DeathDataPacket {
    private final List<DeathPlayerData> deadPlayers;
    private final boolean hasDeaths;

    public DeathDataPacket(List<DeathPlayerData> deadPlayers, boolean hasDeaths) {
        this.deadPlayers = deadPlayers;
        this.hasDeaths = hasDeaths;
    }

    public static class DeathPlayerData {
        public final String playerName;
        public final String playerUUID;
        public final String deathTime;
        public final String dimension;

        public DeathPlayerData(String playerName, String playerUUID, String deathTime, String dimension) {
            this.playerName = playerName != null ? playerName : "Unknown";
            this.playerUUID = playerUUID != null ? playerUUID : "";
            this.deathTime = deathTime != null ? deathTime : "";
            this.dimension = dimension != null ? dimension : "minecraft:overworld";
        }
    }

    public static void encode(DeathDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hasDeaths);
        if (packet.hasDeaths) {
            buffer.writeInt(packet.deadPlayers.size());
            for (DeathPlayerData data : packet.deadPlayers) {
                buffer.writeUtf(data.playerName != null ? data.playerName : "Unknown");
                buffer.writeUtf(data.playerUUID != null ? data.playerUUID : "");
                buffer.writeUtf(data.deathTime != null ? data.deathTime : "");
                buffer.writeUtf(data.dimension != null ? data.dimension : "minecraft:overworld");
            }
        }
    }

    public static DeathDataPacket decode(FriendlyByteBuf buffer) {
        boolean hasDeaths = buffer.readBoolean();
        List<DeathPlayerData> deadPlayers = new ArrayList<>();

        if (hasDeaths) {
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                String playerName = buffer.readUtf();
                String playerUUID = buffer.readUtf();
                String deathTime = buffer.readUtf();
                String dimension = buffer.readUtf();
                deadPlayers.add(new DeathPlayerData(playerName, playerUUID, deathTime, dimension));
            }
        }

        return new DeathDataPacket(deadPlayers, hasDeaths);
    }

    public static void handle(DeathDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.hasDeaths) {
                ReviveMenuScreen.openWithDeathData(packet.deadPlayers);
            }
        });
        context.setPacketHandled(true);
    }
}