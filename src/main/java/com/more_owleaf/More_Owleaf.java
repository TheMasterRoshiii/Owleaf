package com.more_owleaf;

import com.more_owleaf.commands.CasinoReloadCommand;
import com.more_owleaf.commands.CasinoToggleCommand;
import com.more_owleaf.commands.FogataCommands;
import com.more_owleaf.commands.RunaReloadCommand;
import com.more_owleaf.config.CasinoConfig;
import com.more_owleaf.config.DeathRegistry;
import com.more_owleaf.config.FogataConfig;
import com.more_owleaf.config.RunaTradesConfig;
import com.more_owleaf.entities.FogataEntity;
import com.more_owleaf.events.PlayerDeathEvent;
import com.more_owleaf.events.SoundEvents;
import com.more_owleaf.events.UtilHandler;
import com.more_owleaf.init.EntityInit;
import com.more_owleaf.init.MenuInit;
import com.more_owleaf.network.NetworkHandler;
import com.more_owleaf.utils.SoulUtil;
import net.minecraft.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(More_Owleaf.MODID)
public class More_Owleaf {
    public static final String MODID = "more_owleaf";
    public static DeathRegistry DEATH_REGISTRY;
    public static FogataConfig FOGATA_CONFIG;

    public More_Owleaf() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        FOGATA_CONFIG = new FogataConfig();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FogataConfig.COMMON_SPEC);

        EntityInit.ENTITIES.register(bus);
        MenuInit.MENUS.register(bus);
        SoundEvents.SOUNDS.register(bus);

        bus.addListener(this::onCommonSetup);
        bus.addListener(this::registerAttributes);
        bus.addListener(SoulUtil::register);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerDeathEvent());
        MinecraftForge.EVENT_BUS.register(new UtilHandler());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CasinoConfig.loadConfig();
            RunaTradesConfig.loadConfig();
            DEATH_REGISTRY = new DeathRegistry();
            DEATH_REGISTRY.loadConfig();
            NetworkHandler.register();
            System.out.println("Casino JSON configuration loaded");
            System.out.println("Runa trades configuration loaded");
            System.out.println("Death registry loaded");
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CasinoReloadCommand.register(event.getDispatcher());
        CasinoToggleCommand.register(event.getDispatcher());
        RunaReloadCommand.register(event.getDispatcher());
        FogataCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        CasinoConfig.loadConfig();
        RunaTradesConfig.loadConfig();
        FogataCommands.register(event.getServer().getCommands().getDispatcher());
        System.out.println("Server starting - Casino config reloaded");
    }

    @SubscribeEvent
    public void registerAttributes(final EntityAttributeCreationEvent event) {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }
}