package com.more_owleaf.network.orb;

import com.more_owleaf.client.gui.OrbMenuScreen;
import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenOrbScreenPacket {
    private final int entityId;
    private final String configJson;

    public OpenOrbScreenPacket(int entityId, OrbConfig config) {
        this.entityId = entityId;
        this.configJson = OrbConfigManager.configToJson(config);
    }

    private OpenOrbScreenPacket(int entityId, String configJson) {
        this.entityId = entityId;
        this.configJson = configJson;
    }

    public static void encode(OpenOrbScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.configJson, 32767);
    }

    public static OpenOrbScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenOrbScreenPacket(buf.readInt(), buf.readUtf(32767));
    }

    public static void handle(OpenOrbScreenPacket msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide().isClient()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    try {
                        OrbConfig config = OrbConfigManager.jsonToConfig(msg.configJson);
                        mc.setScreen(new OrbMenuScreen(msg.entityId, config));
                    } catch (Exception e) {
                        System.err.println("Error opening orb screen: " + e.getMessage());
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
