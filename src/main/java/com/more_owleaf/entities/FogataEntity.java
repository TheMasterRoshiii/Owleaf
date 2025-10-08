package com.more_owleaf.entities;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.config.DeathRegistry;
import com.more_owleaf.network.fogata.DeathDataPacket;
import com.more_owleaf.network.NetworkHandler;
import com.more_owleaf.utils.fogata.SoulUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class FogataEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FogataEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return this.interactAt(player, Vec3.ZERO, hand);
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack heldItem = player.getItemInHand(hand);

            boolean hasCuchara = false;
            boolean hasTenedor = false;

            if (More_Owleaf.FOGATA_CONFIG != null && More_Owleaf.FOGATA_CONFIG.getItemCuchara() != null) {
                hasCuchara = heldItem.getItem() == More_Owleaf.FOGATA_CONFIG.getItemCuchara();
            }

            if (More_Owleaf.FOGATA_CONFIG != null && More_Owleaf.FOGATA_CONFIG.getItemTenedor() != null) {
                hasTenedor = heldItem.getItem() == More_Owleaf.FOGATA_CONFIG.getItemTenedor();
            }

            if (!hasCuchara && !hasTenedor) {
                return InteractionResult.FAIL;
            }

            if (hasCuchara) {
                boolean hasAlmas = checkPlayerAlmas(player);
                if (!hasAlmas) {
                    return InteractionResult.FAIL;
                }
            }

            if (More_Owleaf.DEATH_REGISTRY != null && !More_Owleaf.DEATH_REGISTRY.getDeathRecords().isEmpty()) {
                List<DeathDataPacket.DeathPlayerData> deadPlayers = new ArrayList<>();
                for (DeathRegistry.DeathRecord record : More_Owleaf.DEATH_REGISTRY.getDeathRecords()) {
                    deadPlayers.add(new DeathDataPacket.DeathPlayerData(
                            record.playerName,
                            record.playerUUID,
                            record.deathTime,
                            record.dimension,
                            record.skinTextureData,
                            record.skinSignature,
                            record.hasSkinData
                    ));
                }

                NetworkHandler.INSTANCE.sendTo(
                        new DeathDataPacket(deadPlayers, true),
                        serverPlayer.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                return InteractionResult.SUCCESS;
            } else {
            }
        }
        return InteractionResult.PASS;
    }

    private boolean checkPlayerAlmas(Player player) {
        return player.getCapability(SoulUtil.SOUL_CAPABILITY)
                .map(soul -> soul.getSouls() >= More_Owleaf.FOGATA_CONFIG.COMMON.almasRequeridas.get())
                .orElse(false);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}