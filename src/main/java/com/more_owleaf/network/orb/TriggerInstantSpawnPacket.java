package com.more_owleaf.network.orb;

import com.more_owleaf.entities.OrbEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerInstantSpawnPacket {
    private final int orbId;

    public TriggerInstantSpawnPacket(int orbId) {
        this.orbId = orbId;
    }

    public static void encode(TriggerInstantSpawnPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.orbId);
    }

    public static TriggerInstantSpawnPacket decode(FriendlyByteBuf buf) {
        return new TriggerInstantSpawnPacket(buf.readInt());
    }

    public static void handle(TriggerInstantSpawnPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) {
                try {
                    Entity entity = player.level().getEntity(msg.orbId);
                    if (entity instanceof OrbEntity orb) {
                        orb.startInstantSpawn();
                    }
                } catch (Exception e) {
                    System.err.println("Error triggering instant spawn: " + e.getMessage());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
