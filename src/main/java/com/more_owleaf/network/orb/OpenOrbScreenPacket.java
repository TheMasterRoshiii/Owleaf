package com.more_owleaf.network.orb;

import com.more_owleaf.client.gui.OrbMenuScreen;
import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class OpenOrbScreenPacket {
    private final int orbId;
    private final String configJson;

    public OpenOrbScreenPacket(int orbId, OrbConfig config) {
        this.orbId = orbId;
        this.configJson = OrbConfigManager.configToJson(config);
    }

    private OpenOrbScreenPacket(int orbId, String json) {
        this.orbId = orbId;
        this.configJson = json;
    }

    public static void encode(OpenOrbScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.orbId);
        buf.writeUtf(msg.configJson);
    }

    public static OpenOrbScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenOrbScreenPacket(buf.readInt(), buf.readUtf());
    }

    public static void handle(OpenOrbScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                OrbConfig config = OrbConfigManager.jsonToConfig(msg.configJson);
                if (config == null) {
                    return;
                }
                Minecraft.getInstance().setScreen(new OrbMenuScreen(msg.orbId, config));
            } catch (Exception ignored) {
            }
        });
        ctx.get().setPacketHandled(true);
    }
}