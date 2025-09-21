package com.more_owleaf.entities;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.config.DeathRegistry;
import com.more_owleaf.network.DeathDataPacket;
import com.more_owleaf.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class FogataEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FogataEntity(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public InteractionResult interactAt(Player player, net.minecraft.world.phys.Vec3 vec, InteractionHand hand) {
        return this.mobInteract(player, hand);
    }


    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (More_Owleaf.DEATH_REGISTRY != null && !More_Owleaf.DEATH_REGISTRY.getDeathRecords().isEmpty()) {
                List<DeathDataPacket.DeathPlayerData> deadPlayers = new ArrayList<>();
                for (DeathRegistry.DeathRecord record : More_Owleaf.DEATH_REGISTRY.getDeathRecords()) {
                    deadPlayers.add(new DeathDataPacket.DeathPlayerData(
                            record.playerName,
                            record.playerUUID,
                            record.deathTime,
                            record.dimension
                    ));
                }

                NetworkHandler.INSTANCE.sendTo(
                        new DeathDataPacket(deadPlayers, true),
                        serverPlayer.connection.connection,
                        net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT
                );
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}