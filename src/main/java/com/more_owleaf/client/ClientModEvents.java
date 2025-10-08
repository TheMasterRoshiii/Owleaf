package com.more_owleaf.client;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.client.renderer.*;
import com.more_owleaf.client.renderer.BarrierRenderer;
import com.more_owleaf.client.renderer.OrbRenderer;
import com.more_owleaf.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = More_Owleaf.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.CASINO.get(), CasinoRenderer::new);
        event.registerEntityRenderer(EntityInit.FOGATA.get(), FogataRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_AMARILLA.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_AZUL_CLARO.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_MAGENTA.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_MORADA.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_ROJA.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.RUNA_VERDE.get(), RunaRenderer::new);
        event.registerEntityRenderer(EntityInit.ORB_ENTITY.get(), OrbRenderer::new);
        event.registerEntityRenderer(EntityInit.BARRIER_ENTITY.get(), BarrierRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
    }
}
