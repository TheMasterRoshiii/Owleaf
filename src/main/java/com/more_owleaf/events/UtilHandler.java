package com.more_owleaf.events;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.utils.SoulProvider;
import com.more_owleaf.utils.SoulUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = More_Owleaf.MODID)
public class UtilHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            int initialSouls = More_Owleaf.FOGATA_CONFIG.COMMON.vidasIniciales.get();
            event.addCapability(new ResourceLocation(More_Owleaf.MODID, "souls"), new SoulProvider(initialSouls));
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(SoulUtil.SOUL_CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(SoulUtil.SOUL_CAPABILITY).ifPresent(newStore -> {
                    newStore.setSouls(oldStore.getSouls());
                });
            });
        }
    }
}