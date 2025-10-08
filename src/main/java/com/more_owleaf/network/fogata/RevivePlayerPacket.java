package com.more_owleaf.network.fogata;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.config.FogataConfig;
import com.more_owleaf.utils.fogata.SoulUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RevivePlayerPacket {

    private final UUID targetPlayerUUID;

    public RevivePlayerPacket(UUID targetPlayerUUID) {
        this.targetPlayerUUID = targetPlayerUUID;
    }

    public RevivePlayerPacket(FriendlyByteBuf buf) {
        this.targetPlayerUUID = buf.readUUID();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(targetPlayerUUID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer reviver = context.getSender();
            if (reviver == null) return;

            ItemStack heldItem = reviver.getMainHandItem();
            Item cuchara = More_Owleaf.FOGATA_CONFIG.getItemCuchara();
            Item tenedor = More_Owleaf.FOGATA_CONFIG.getItemTenedor();

            boolean usedCuchara = heldItem.getItem() == cuchara;
            boolean usedTenedor = heldItem.getItem() == tenedor;

            if (!usedCuchara && !usedTenedor) {
                reviver.sendSystemMessage(Component.literal("¡Necesitas el ítem correcto en la mano!"));
                return;
            }

            if (usedCuchara) {
                reviver.getCapability(SoulUtil.SOUL_CAPABILITY).ifPresent(soul -> {
                    int almasRequeridas = FogataConfig.COMMON.almasRequeridas.get();
                    if (soul.getSouls() >= almasRequeridas) {
                        soul.removeSouls(almasRequeridas);
                        revivePlayer(reviver, targetPlayerUUID);
                    } else {
                        reviver.sendSystemMessage(Component.literal("¡No tienes suficientes almas!"));
                    }
                });
            }

            if (usedTenedor) {
                revivePlayer(reviver, targetPlayerUUID);
            }
        });
        return true;
    }

    private void revivePlayer(ServerPlayer reviver, UUID targetUUID) {
        var deathRecord = More_Owleaf.DEATH_REGISTRY.getRecordByUUID(targetUUID.toString());
        if (deathRecord.isEmpty()) {
            reviver.sendSystemMessage(Component.literal("Ese jugador ya no está en el registro."));
            return;
        }

        String targetName = deathRecord.get().playerName;
        var server = reviver.getServer();
        if (server == null) return;

        FogataConfig.MetodoExpulsion metodo = FogataConfig.COMMON.metodoExpulsion.get();
        String command = "";
        switch (metodo) {
            case WHITELIST -> command = "whitelist add " + targetName;
            case BAN -> command = "pardon " + targetName;
            case BANIP -> {
                command = "pardon " + targetName;
            }
        }

        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
        reviver.sendSystemMessage(Component.literal("¡Has revivido a " + targetName + "!"));

        More_Owleaf.DEATH_REGISTRY.removeDeathRecord(targetUUID.toString());

        reviver.closeContainer();
    }
}