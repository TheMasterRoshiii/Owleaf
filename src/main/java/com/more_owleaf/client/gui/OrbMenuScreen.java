package com.more_owleaf.client.gui;

import com.more_owleaf.config.OrbConfig;
import com.more_owleaf.entities.OrbEntity;
import com.more_owleaf.network.NetworkHandler;
import com.more_owleaf.network.orb.TriggerInstantSpawnPacket;
import com.more_owleaf.network.orb.UpdateOrbConfigPacket;
import com.more_owleaf.utils.orb.AdminConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class OrbMenuScreen extends Screen {
    private final int orbId;
    private OrbEntity orb;
    private OrbConfig config;

    private EditBox spawnRateBox, maxMobsBox, spawnCountBox, spawnRadiusBox, mobTypeBox;
    private EditBox instantRadiusBox, instantQuantityBox, instantDelayBox;
    private EditBox healthBox, resistanceBox;

    public OrbMenuScreen(int orbId, OrbConfig config) {
        super(Component.literal("Orb Configuration"));
        this.orbId = orbId;
        this.config = config;
        Entity entity = Minecraft.getInstance().level.getEntity(orbId);
        if (entity instanceof OrbEntity) {
            this.orb = (OrbEntity) entity;
        }
    }

    @Override
    protected void init() {
        super.init();
        if (this.orb == null || this.config == null) {
            this.onClose();
            return;
        }

        int leftColX = this.width / 4 - 75;
        int midColX = this.width / 2 - 50;
        int rightColX = this.width - (this.width / 4) - 50;

        addRenderableWidget(Button.builder(Component.literal("Mode: " + config.getMode().name()), (b) -> {
            toggleMode();
            Minecraft.getInstance().setScreen(new OrbMenuScreen(this.orbId, this.config));
        }).pos(leftColX, 40).size(100, 20).build());

        if (config.getMode() == OrbConfig.OrbMode.BARRIER) {
            addRenderableWidget(Button.builder(Component.literal("Orientation: " + (config.isBarrierZAligned() ? "N-S" : "E-W")), (b) -> {
                config.setBarrierZAligned(!config.isBarrierZAligned());
                b.setMessage(Component.literal("Orientation: " + (config.isBarrierZAligned() ? "N-S" : "E-W")));
            }).pos(leftColX, 65).size(100, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("State: " + (config.isActive() ? "ACTIVE" : "INACTIVE")), (b) -> {
            config.setActive(!config.isActive());
            b.setMessage(Component.literal("State: " + (config.isActive() ? "ACTIVE" : "INACTIVE")));
        }).pos(leftColX, 90).size(100, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Copy UUID"), (b) -> {
            this.minecraft.keyboardHandler.setClipboard(this.orb.getUUID().toString());
            b.setMessage(Component.literal("Copied!"));
        }).pos(leftColX, 115).size(100, 20).build());

        if (config.getMode() == OrbConfig.OrbMode.SPAWNER) {
            addRenderableWidget(Button.builder(Component.literal("Damageable: " + (config.isDamageable() ? "YES" : "NO")), (b) -> {
                config.setDamageable(!config.isDamageable());
                b.setMessage(Component.literal("Damageable: " + (config.isDamageable() ? "YES" : "NO")));
            }).pos(leftColX, 140).size(100, 20).build());

            healthBox = new EditBox(this.font, leftColX, 170, 100, 20, Component.empty());
            healthBox.setValue(String.valueOf(config.getMaxHealth()));
            healthBox.setResponder((val) -> {
                try {
                    float health = Float.parseFloat(val);
                    if (health > 0 && health <= 1000) {
                        config.setMaxHealth(health);
                    }
                } catch (NumberFormatException e) {}
            });
            addRenderableWidget(healthBox);

            resistanceBox = new EditBox(this.font, leftColX, 200, 100, 20, Component.empty());
            resistanceBox.setValue(String.valueOf(config.getResistance()));
            resistanceBox.setResponder((val) -> {
                try {
                    float resistance = Float.parseFloat(val);
                    if (resistance >= 0.0f && resistance <= 1.0f) {
                        config.setResistance(resistance);
                    }
                } catch (NumberFormatException e) {}
            });
            addRenderableWidget(resistanceBox);
        }

        if (AdminConfigManager.isPlayerAdmin(this.minecraft.player.getGameProfile().getName())) {
            int midColY = 40;
            instantRadiusBox = new EditBox(this.font, midColX, midColY, 100, 20, Component.empty());
            instantRadiusBox.setValue(String.valueOf(config.getInstantSpawnRadius()));
            instantRadiusBox.setResponder((val) -> { try { config.setInstantSpawnRadius(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(instantRadiusBox);

            instantQuantityBox = new EditBox(this.font, midColX, midColY + 30, 100, 20, Component.empty());
            instantQuantityBox.setValue(String.valueOf(config.getInstantSpawnQuantity()));
            instantQuantityBox.setResponder((val) -> { try { config.setInstantSpawnQuantity(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(instantQuantityBox);

            instantDelayBox = new EditBox(this.font, midColX, midColY + 60, 100, 20, Component.empty());
            instantDelayBox.setValue(String.valueOf(config.getInstantSpawnDelay()));
            instantDelayBox.setResponder((val) -> { try { config.setInstantSpawnDelay(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(instantDelayBox);

            addRenderableWidget(Button.builder(Component.literal("Spawn Masivo"), (b) -> {
                saveAndClose();
                NetworkHandler.INSTANCE.sendToServer(new TriggerInstantSpawnPacket(this.orbId));
            }).pos(midColX, midColY + 90).size(100, 20).build());
        }

        if (config.getMode() == OrbConfig.OrbMode.SPAWNER) {
            int spawnerY = 40;
            maxMobsBox = new EditBox(this.font, rightColX, spawnerY, 40, 20, Component.empty());
            maxMobsBox.setValue(String.valueOf(config.getMaxMobs()));
            maxMobsBox.setResponder((val) -> { try { config.setMaxMobs(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(maxMobsBox);

            spawnCountBox = new EditBox(this.font, rightColX + 50, spawnerY, 40, 20, Component.empty());
            spawnCountBox.setValue(String.valueOf(config.getSpawnCount()));
            spawnCountBox.setResponder((val) -> { try { config.setSpawnCount(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(spawnCountBox);

            spawnerY += 30;
            spawnRadiusBox = new EditBox(this.font, rightColX, spawnerY, 40, 20, Component.empty());
            spawnRadiusBox.setValue(String.valueOf(config.getSpawnRadius()));
            spawnRadiusBox.setResponder((val) -> { try { config.setSpawnRadius(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(spawnRadiusBox);

            spawnRateBox = new EditBox(this.font, rightColX + 50, spawnerY, 40, 20, Component.empty());
            spawnRateBox.setValue(String.valueOf(config.getSpawnRate()));
            spawnRateBox.setResponder((val) -> { try { config.setSpawnRate(Integer.parseInt(val)); } catch (NumberFormatException e) {} });
            addRenderableWidget(spawnRateBox);

            spawnerY += 30;
            mobTypeBox = new EditBox(this.font, rightColX, spawnerY, 140, 20, Component.empty());
            mobTypeBox.setValue(config.getMobType());
            mobTypeBox.setResponder((text) -> config.setMobType(text));
            addRenderableWidget(mobTypeBox);

            spawnerY += 30;
            addRenderableWidget(Button.builder(
                            Component.literal("Charge Creepers: " + (config.getChargeCreepers() ? "ON" : "OFF")),
                            (button) -> {
                                config.setChargeCreepers(!config.getChargeCreepers());
                                button.setMessage(Component.literal("Charge Creepers: " + (config.getChargeCreepers() ? "ON" : "OFF")));
                            })
                    .pos(rightColX, spawnerY).size(140, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Save & Close"), (b) -> saveAndClose())
                .pos(this.width / 2 - 75, this.height - 40).size(150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (AdminConfigManager.isPlayerAdmin(this.minecraft.player.getGameProfile().getName())) {
            guiGraphics.drawString(this.font, "Radio (Masivo)", instantRadiusBox.getX(), instantRadiusBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Cantidad (Masivo)", instantQuantityBox.getX(), instantQuantityBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Ticks de Espera", instantDelayBox.getX(), instantDelayBox.getY() - 10, 0xFFFFFF);
        }

        if (config.getMode() == OrbConfig.OrbMode.SPAWNER) {
            guiGraphics.drawString(this.font, "Max Mobs", maxMobsBox.getX(), maxMobsBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Count", spawnCountBox.getX(), spawnCountBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Radius", spawnRadiusBox.getX(), spawnRadiusBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Rate", spawnRateBox.getX(), spawnRateBox.getY() - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, "Mob ID", mobTypeBox.getX(), mobTypeBox.getY() - 10, 0xFFFFFF);

            if (healthBox != null) {
                guiGraphics.drawString(this.font, "Max Health", healthBox.getX(), healthBox.getY() - 10, 0xFFFFFF);
            }
            if (resistanceBox != null) {
                guiGraphics.drawString(this.font, "Resistance (0.0-1.0)", resistanceBox.getX(), resistanceBox.getY() - 10, 0xFFFFFF);
            }
        }
    }

    private void toggleMode() {
        switch (config.getMode()) {
            case IDLE: config.setMode(OrbConfig.OrbMode.BARRIER); break;
            case BARRIER: config.setMode(OrbConfig.OrbMode.SPAWNER); break;
            case SPAWNER: config.setMode(OrbConfig.OrbMode.IDLE); break;
        }
        config.setActive(false);
    }

    private void saveAndClose() {
        if (healthBox != null) {
            try {
                float health = Float.parseFloat(healthBox.getValue());
                if (health > 0 && health <= 1000) config.setMaxHealth(health);
            } catch (NumberFormatException ignored) {}
        }

        if (resistanceBox != null) {
            try {
                float resistance = Float.parseFloat(resistanceBox.getValue());
                if (resistance >= 0.0f && resistance <= 1.0f) config.setResistance(resistance);
            } catch (NumberFormatException ignored) {}
        }

        NetworkHandler.INSTANCE.sendToServer(new UpdateOrbConfigPacket(this.orbId, this.config));
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
