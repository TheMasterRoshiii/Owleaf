package com.more_owleaf.client.gui;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.client.SkinHelper;
import com.more_owleaf.network.DeathDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = More_Owleaf.MODID, value = Dist.CLIENT)
public class ReviveMenuScreen extends Screen {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu.png");
    private static final ResourceLocation ANGEL_BUTTON = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_angel.png");
    private static final ResourceLocation ANGEL_BUTTON_HOVERED = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_angel_presionado.png");
    private static final ResourceLocation LEFT_BUTTON = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_flecha_izquierda.png");
    private static final ResourceLocation LEFT_BUTTON_HOVERED = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_flecha_izquierda_presionado.png");
    private static final ResourceLocation RIGHT_BUTTON = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_flecha_derecha.png");
    private static final ResourceLocation RIGHT_BUTTON_HOVERED = ResourceLocation.tryBuild("more_owleaf", "textures/gui/revive_menu_flecha_derecha_presionado.png");

    private final int textureWidth = 188;
    private final int textureHeight = 150;
    private final int imageWidth = 188;
    private final int imageHeight = 150;
    private int leftPos;
    private int topPos;
    private final int angelButtonWidth = 24;
    private final int angelButtonHeight = 21;
    private final int leftButtonWidth = 24;
    private final int leftButtonHeight = 21;
    private final int rightButtonWidth = 24;
    private final int rightButtonHeight = 21;

    private static final int HEAD_SIZE = 32;

    private List<DeathDataPacket.DeathPlayerData> deadPlayers;
    private int currentPlayerIndex = 0;

    public ReviveMenuScreen(List<DeathDataPacket.DeathPlayerData> deadPlayers) {
        super(Component.translatable("gui.more_owleaf.revive_menu"));
        this.deadPlayers = deadPlayers;
    }

    public static void openWithDeathData(List<DeathDataPacket.DeathPlayerData> deadPlayers) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreen(new ReviveMenuScreen(deadPlayers));
        });
    }

    private DeathDataPacket.DeathPlayerData getCurrentPlayer() {
        if (deadPlayers.isEmpty()) return null;
        return deadPlayers.get(currentPlayerIndex);
    }

    private static void renderPlayerHead2D(GuiGraphics graphics, int x, int y, DeathDataPacket.DeathPlayerData playerData) {
        try {
            ResourceLocation skin = SkinHelper.getPlayerSkinFromData(
                    playerData.playerName,
                    playerData.playerUUID,
                    playerData.skinTextureData,
                    playerData.skinSignature,
                    playerData.hasSkinData
            );

            if (skin != null) {
                graphics.blit(skin, x, y, HEAD_SIZE, HEAD_SIZE, 8, 8, 8, 8, 64, 64);
                graphics.blit(skin, x, y, HEAD_SIZE, HEAD_SIZE, 40, 8, 8, 8, 64, 64);
            } else {
                graphics.fill(x, y, x + HEAD_SIZE, y + HEAD_SIZE, 0xFF8B7355);
            }
        } catch (Exception e) {
            graphics.fill(x, y, x + HEAD_SIZE, y + HEAD_SIZE, 0xFF8B7355);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int angelButtonX = this.width / 2 - (angelButtonWidth / 2);
        int angelButtonY = this.topPos + this.imageHeight - 35;

        this.addRenderableWidget(new AngelButton(angelButtonX, angelButtonY, angelButtonWidth, angelButtonHeight, Component.empty()));

        int centerY = this.topPos + (this.imageHeight / 2);
        int leftButtonX = this.width / 2 - 60 - (leftButtonWidth / 2);
        int leftButtonY = centerY - (leftButtonHeight / 2);

        this.addRenderableWidget(new LeftButton(leftButtonX, leftButtonY, leftButtonWidth, leftButtonHeight, Component.empty()));

        int rightButtonX = this.width / 2 + 60 - (rightButtonWidth / 2);
        int rightButtonY = centerY - (rightButtonHeight / 2);

        this.addRenderableWidget(new RightButton(rightButtonX, rightButtonY, rightButtonWidth, rightButtonHeight, Component.empty()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        if (TEXTURE != null) {
            guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0,
                    this.imageWidth, this.imageHeight,
                    this.textureWidth, this.textureHeight);
        } else {
            guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0x88000000);
        }

        renderPlayerInfo(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderPlayerInfo(GuiGraphics guiGraphics) {
        DeathDataPacket.DeathPlayerData currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) {
            Component noPlayers = Component.literal("No dead players");
            int textWidth = this.font.width(noPlayers);
            int textX = this.width / 2 - (textWidth / 2);
            int textY = this.topPos + (this.imageHeight / 2);
            guiGraphics.drawString(this.font, noPlayers, textX, textY, 0xFFFFFF, false);
            return;
        }

        int centerX = this.width / 2;
        int headCenterY = this.topPos + (this.imageHeight / 2) - 30;
        int nameY = headCenterY - 25;

        int skinX = centerX - (HEAD_SIZE / 2);
        int skinY = headCenterY - (HEAD_SIZE / 2);

        renderPlayerHead2D(guiGraphics, skinX, skinY, currentPlayer);

        Component playerName = Component.literal(currentPlayer.playerName);
        int textWidth = this.font.width(playerName);
        int textX = centerX - (textWidth / 2);

        guiGraphics.pose().pushPose();
        float scaleFactor = 1.5F;
        guiGraphics.pose().translate(centerX, nameY + (scaleFactor * 4), 0);
        guiGraphics.pose().scale(scaleFactor, scaleFactor, 1.0F);
        guiGraphics.pose().translate(-centerX, -(nameY + (scaleFactor * 4)), 0);
        guiGraphics.drawString(this.font, playerName, textX, nameY, 0x751B0C, false);
        guiGraphics.pose().popPose();

        renderNavigationInfo(guiGraphics);
        renderDeathInfo(guiGraphics, currentPlayer);
    }

    private void renderNavigationInfo(GuiGraphics guiGraphics) {
        if (deadPlayers.size() > 1) {
            int centerX = this.width / 2;
            int infoY = this.topPos + this.imageHeight - 50;

            Component navigation = Component.literal((currentPlayerIndex + 1) + "/" + deadPlayers.size());
            int navWidth = this.font.width(navigation);
            int navX = centerX - (navWidth / 2);
            guiGraphics.drawString(this.font, navigation, navX, infoY, 0x888888, false);
        }
    }

    private void renderDeathInfo(GuiGraphics guiGraphics, DeathDataPacket.DeathPlayerData player) {
        int centerX = this.width / 2;
        int infoStartY = this.topPos + (this.imageHeight / 2) + 20;

        String deathTime = formatTime(player.deathTime);
        Component timeComponent = Component.literal("Died: " + deathTime);
        int timeWidth = this.font.width(timeComponent);
        guiGraphics.drawString(this.font, timeComponent, centerX - (timeWidth / 2), infoStartY, 0xCCCCCC, false);

        String dimension = getDimensionName(player.dimension);
        Component dimComponent = Component.literal("In: " + dimension);
        int dimWidth = this.font.width(dimComponent);
        guiGraphics.drawString(this.font, dimComponent, centerX - (dimWidth / 2), infoStartY + 12, 0xCCCCCC, false);
    }

    private String formatTime(String timeString) {
        try {
            return timeString.substring(0, Math.min(timeString.length(), 16)).replace("T", " ");
        } catch (Exception e) {
            return timeString;
        }
    }

    private String getDimensionName(String dimension) {
        if (dimension.contains("nether")) {
            return "Nether";
        } else if (dimension.contains("end")) {
            return "End";
        } else if (dimension.contains("overworld")) {
            return "Overworld";
        }
        return "Unknown";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class AngelButton extends AbstractButton {
        public AngelButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void onPress() {
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation texture = this.isHoveredOrFocused() ? ANGEL_BUTTON_HOVERED : ANGEL_BUTTON;
            if (texture != null) {
                guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            } else {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF44AA44);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private class LeftButton extends AbstractButton {
        public LeftButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void onPress() {
            if (deadPlayers.size() > 1) {
                currentPlayerIndex = (currentPlayerIndex - 1 + deadPlayers.size()) % deadPlayers.size();
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation texture = this.isHoveredOrFocused() ? LEFT_BUTTON_HOVERED : LEFT_BUTTON;
            if (texture != null) {
                guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            } else {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFFAA4444);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private class RightButton extends AbstractButton {
        public RightButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void onPress() {
            if (deadPlayers.size() > 1) {
                currentPlayerIndex = (currentPlayerIndex + 1) % deadPlayers.size();
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation texture = this.isHoveredOrFocused() ? RIGHT_BUTTON_HOVERED : RIGHT_BUTTON;
            if (texture != null) {
                guiGraphics.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            } else {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF4444AA);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}
