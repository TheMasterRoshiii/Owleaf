package com.more_owleaf.network.orb;

import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.utils.orb.AdminConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
            if (player != null) {
                if (!AdminConfigManager.isPlayerAdmin(player.getGameProfile().getName())) {
                    player.sendSystemMessage(Component.literal("No tienes permisos para usar esta función.").withStyle(ChatFormatting.RED));
                    return;
                }

                Entity entity = player.level().getEntity(msg.orbId);
                if (entity instanceof OrbEntity orb) {
                    orb.startInstantSpawn();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}