package com.more_owleaf.client;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.client.renderer.CasinoRenderer;
import com.more_owleaf.client.renderer.FogataRenderer;
import com.more_owleaf.client.renderer.RunaRenderer;
import com.more_owleaf.client.renderer.OrbRenderer; // Agregar este import
import com.more_owleaf.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
    }
}
