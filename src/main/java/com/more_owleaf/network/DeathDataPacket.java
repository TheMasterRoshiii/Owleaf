package com.more_owleaf.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DeathDataPacket {
    private final List<DeathPlayerData> deadPlayers;
    private final boolean showGui;

    public DeathDataPacket(List<DeathPlayerData> deadPlayers, boolean showGui) {
        this.deadPlayers = deadPlayers;
        this.showGui = showGui;
    }

    public DeathDataPacket(FriendlyByteBuf buf) {
        this.showGui = buf.readBoolean();
        int size = buf.readInt();
        this.deadPlayers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.deadPlayers.add(new DeathPlayerData(buf));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(showGui);
        buf.writeInt(deadPlayers.size());
        for (DeathPlayerData data : deadPlayers) {
            data.toBytes(buf);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                try {
                    Class<?> clientHandlerClass = Class.forName("com.more_owleaf.client.ClientPacketHandler");
                    java.lang.reflect.Method handleMethod = clientHandlerClass.getMethod("handleDeathDataPacket", DeathDataPacket.class);
                    handleMethod.invoke(null, this);
                } catch (Exception e) {
                    System.err.println("[Owleaf] Could not handle client packet: " + e.getMessage());
                }
            });
        });
        return true;
    }

    public static class DeathPlayerData {
        public final String playerName;
        public final String playerUUID;
        public final String deathTime;
        public final String dimension;
        public final String skinTextureData;
        public final String skinSignature;
        public final boolean hasSkinData;

        public DeathPlayerData(String playerName, String playerUUID, String deathTime, String dimension,
                               String skinTextureData, String skinSignature, boolean hasSkinData) {
            this.playerName = playerName != null ? playerName : "";
            this.playerUUID = playerUUID != null ? playerUUID : "";
            this.deathTime = deathTime != null ? deathTime : "";
            this.dimension = dimension != null ? dimension : "";
            this.skinTextureData = skinTextureData != null ? skinTextureData : "";
            this.skinSignature = skinSignature != null ? skinSignature : "";
            this.hasSkinData = hasSkinData;
        }

        public DeathPlayerData(FriendlyByteBuf buf) {
            this.playerName = buf.readUtf();
            this.playerUUID = buf.readUtf();
            this.deathTime = buf.readUtf();
            this.dimension = buf.readUtf();
            this.skinTextureData = buf.readUtf();
            this.skinSignature = buf.readUtf();
            this.hasSkinData = buf.readBoolean();
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeUtf(playerName != null ? playerName : "");
            buf.writeUtf(playerUUID != null ? playerUUID : "");
            buf.writeUtf(deathTime != null ? deathTime : "");
            buf.writeUtf(dimension != null ? dimension : "");
            buf.writeUtf(skinTextureData != null ? skinTextureData : "");
            buf.writeUtf(skinSignature != null ? skinSignature : "");
            buf.writeBoolean(hasSkinData);
        }
    }

    public List<DeathPlayerData> getDeadPlayers() {
        return deadPlayers;
    }

    public boolean shouldShowGui() {
        return showGui;
    }
}
