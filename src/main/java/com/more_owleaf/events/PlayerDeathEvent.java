package com.more_owleaf.events;

import com.more_owleaf.More_Owleaf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = More_Owleaf.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDeathEvent {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            More_Owleaf.DEATH_REGISTRY.registerDeath(player, player.getServer());
        }
    }
}