package com.more_owleaf.client;

import com.more_owleaf.client.gui.ReviveMenuScreen;
import com.more_owleaf.network.fogata.DeathDataPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void handleDeathDataPacket(DeathDataPacket packet) {
        if (packet.shouldShowGui()) {
            Minecraft.getInstance().setScreen(new ReviveMenuScreen(packet.getDeadPlayers()));
        }
    }
}
