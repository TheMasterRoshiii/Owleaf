package com.more_owleaf.client;

import com.more_owleaf.client.gui.OrbMenuScreen;
import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.utils.orb.OrbConfigManager;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    public static void handleOpenOrbScreen(int entityId, String configJson) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            try {
                OrbConfig config = OrbConfigManager.jsonToConfig(configJson);
                mc.setScreen(new OrbMenuScreen(entityId, config));
            } catch (Exception e) {
                System.err.println("Error opening orb screen from client handler: " + e.getMessage());
            }
        }
    }
}
