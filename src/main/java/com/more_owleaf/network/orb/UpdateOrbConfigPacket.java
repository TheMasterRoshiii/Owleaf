package com.more_owleaf.network.orb;


import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class UpdateOrbConfigPacket {
    private final int orbId;
    private final String configJson;

    public UpdateOrbConfigPacket(int orbId, OrbConfig config) {
        this.orbId = orbId;
        this.configJson = OrbConfigManager.configToJson(config);
    }

    private UpdateOrbConfigPacket(int orbId, String json) {
        this.orbId = orbId;
        this.configJson = json;
    }

    public static void encode(UpdateOrbConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.orbId);
        buf.writeUtf(msg.configJson);
    }

    public static UpdateOrbConfigPacket decode(FriendlyByteBuf buf) {
        return new UpdateOrbConfigPacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(UpdateOrbConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(msg.orbId);
                if (entity instanceof OrbEntity orb) {
                    OrbConfig newConfig = OrbConfigManager.jsonToConfig(msg.configJson);
                    orb.setConfig(newConfig);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
