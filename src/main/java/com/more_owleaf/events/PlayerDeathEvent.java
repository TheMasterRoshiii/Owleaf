package com.more_owleaf.events;


import com.more_owleaf.More_Owleaf;
import com.more_owleaf.config.FogataConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanListEntry;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = More_Owleaf.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDeathEvent {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            if (More_Owleaf.DEATH_REGISTRY.getIgnoredPlayers().contains(player.getUUID().toString())) {
                return;
            }

            More_Owleaf.DEATH_REGISTRY.registerDeath(player);

            var server = player.getServer();
            if (server == null) return;

            FogataConfig.MetodoExpulsion metodo = FogataConfig.COMMON.metodoExpulsion.get();

            switch (metodo) {
                case WHITELIST:
                    server.getPlayerList().getWhiteList().remove(player.getGameProfile());
                    break;
                case BAN:
                    server.getPlayerList().getBans().add(new UserBanListEntry(player.getGameProfile(), null, "More_Owleaf", null, "Has muerto."));
                    break;
                case BANIP:
                    String ip = player.getIpAddress();
                    if (ip != null && !ip.isBlank()) {
                        server.getPlayerList().getIpBans().add(new IpBanListEntry(ip, null, "More_Owleaf", null, "Has muerto."));
                    }
                    break;
            }

            player.connection.disconnect(Component.literal("Has Muerto."));
        }
    }
}