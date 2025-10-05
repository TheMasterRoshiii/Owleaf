package com.more_owleaf.entities;

import com.more_owleaf.config.CasinoConfig;
import com.more_owleaf.events.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CasinoEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_SPINNING = SynchedEntityData.defineId(CasinoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> CURRENT_SPIN_TYPE = SynchedEntityData.defineId(CasinoEntity.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int spinCooldown = 0;
    private long spinStartTime = 0;
    private static final int SPIN_DURATION = 15 * 20;

    private final Map<UUID, PendingPrize> pendingPrizes = new HashMap<>();

    public static final List<String> SPIN_WINNING = new ArrayList<>();
    public static final List<String> SPIN_SEMI_WINNING = new ArrayList<>();
    public static final List<String> SPIN_LOSING = new ArrayList<>();

    private static final String[] WINNING_ANIMATIONS = {"animacion_ganadora_pluma",
            "animacion_ganadora_eon",
            "animacion_ganadora_brocoli",
            "animacion_ganadora_dedita",
            "animacion_ganadora_nutria",
            "animacion_ganadora_corazon",
            "animacion_ganadora_oro",
            "animacion_ganadora_pocion"};

    private static final String SEMI_WINNING_ANIMATION_BASE = "animacion_semi_ganadora";
    private static final int SEMI_WINNING_ANIMATION_COUNT = 56;

    private static final String LOSE_ANIMATION_BASE = "animacion_aleatoria";
    private static final int LOSE_ANIMATION_COUNT = 60;

    static {
        for (String winningAnim : WINNING_ANIMATIONS) {
            SPIN_WINNING.add(winningAnim);
        }

        for (int i = 1; i <= SEMI_WINNING_ANIMATION_COUNT; i++) {
            SPIN_SEMI_WINNING.add(SEMI_WINNING_ANIMATION_BASE + "_" + i);
        }

        for (int i = 1; i <= LOSE_ANIMATION_COUNT; i++) {
            SPIN_LOSING.add(LOSE_ANIMATION_BASE + "_" + i);
        }
    }

    private static class PendingPrize {
        public final ItemStack prize;
        public final long deliveryTime;
        public final String animationType;

        public PendingPrize(ItemStack prize, long deliveryTime, String animationType) {
            this.prize = prize.copy();
            this.deliveryTime = deliveryTime;
            this.animationType = animationType;
        }
    }

    public CasinoEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SPINNING, false);
        this.entityData.define(CURRENT_SPIN_TYPE, SPIN_LOSING.get(0));
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

    public boolean isSpinning() {
        return this.entityData.get(DATA_SPINNING);
    }

    public void setSpinning(boolean spinning) {
        this.entityData.set(DATA_SPINNING, spinning);
    }

    public String getCurrentSpinType() {
        return this.entityData.get(CURRENT_SPIN_TYPE);
    }

    public void setCurrentSpinType(String spinType) {
        this.entityData.set(CURRENT_SPIN_TYPE, spinType);
    }

    private String determineSpinType() {
        Random random = new Random();
        int chance = random.nextInt(100);

        int winningChance = CasinoConfig.WINNING_CHANCE;
        int semiWinningChance = CasinoConfig.SEMI_WINNING_CHANCE;

        String result;
        if (chance < winningChance) {
            int index = random.nextInt(SPIN_WINNING.size());
            result = SPIN_WINNING.get(index);
        } else if (chance < winningChance + semiWinningChance) {
            int index = random.nextInt(SPIN_SEMI_WINNING.size());
            result = SPIN_SEMI_WINNING.get(index);
        } else {
            int index = random.nextInt(SPIN_LOSING.size());
            result = SPIN_LOSING.get(index);
        }
        return result;
    }

    private ItemStack getPrizeForAnimation(String animationName) {
        return CasinoConfig.getPrizeForAnimation(animationName);
    }

    private void playCasinoSound() {
        try {
            SoundEvent casinoSound = SoundEvents.CASINO_SONG.get();
            if (casinoSound != null) {
                if (this.level().isClientSide) {
                    this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                            casinoSound, SoundSource.RECORDS, 1.0f, 1.0f, false);
                } else {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            casinoSound, SoundSource.RECORDS, 1.0f, 1.0f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void schedulePrizeDelivery(Player player, ItemStack prize, String animationType) {
        long currentTime = this.level().getGameTime();
        long deliveryTime = currentTime + CasinoConfig.PRIZE_COOLDOWN_TICKS;

        pendingPrizes.put(player.getUUID(), new PendingPrize(prize, deliveryTime, animationType));
    }

    private void processPendingPrizes() {
        if (pendingPrizes.isEmpty()) return;

        long currentTime = this.level().getGameTime();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, PendingPrize> entry : pendingPrizes.entrySet()) {
            UUID playerUUID = entry.getKey();
            PendingPrize pendingPrize = entry.getValue();

            if (currentTime >= pendingPrize.deliveryTime) {
                Player player = this.level().getPlayerByUUID(playerUUID);

                if (player != null && player.distanceToSqr(this) <= 256) {
                    ItemStack prizeCopy = pendingPrize.prize.copy();

                    if (!prizeCopy.isEmpty()) {
                        Component winMessage = CasinoConfig.formatWinMessage(prizeCopy);
                        player.sendSystemMessage(winMessage);
                    }

                    if (!player.getInventory().add(prizeCopy)) {
                        player.drop(prizeCopy, false);
                    }

                } else {
                    System.out.println("Premio cancelado para jugador " + playerUUID +
                            " (no está cerca o desconectado)");
                }

                toRemove.add(playerUUID);
            }
        }

        for (UUID uuid : toRemove) {
            pendingPrizes.remove(uuid);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("SpinCooldown")) {
            spinCooldown = compound.getInt("SpinCooldown");
        }
        if (compound.contains("IsSpinning")) {
            setSpinning(compound.getBoolean("IsSpinning"));
        }
        if (compound.contains("SpinType")) {
            setCurrentSpinType(compound.getString("SpinType"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("SpinCooldown", spinCooldown);
        compound.putBoolean("IsSpinning", isSpinning());
        compound.putString("SpinType", getCurrentSpinType());
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (!CasinoConfig.CASINO_ENABLED) {
                player.sendSystemMessage(Component.literal("El casino está deshabilitado temporalmente")
                        .withStyle(style -> style.withColor(0xFF5555)));
                return InteractionResult.FAIL;
            }

            boolean isValidItem = !heldItem.isEmpty() &&
                    heldItem.getItem() != Items.AIR &&
                    heldItem.getItem() == CasinoConfig.INTERACTION_ITEM;

            if (isValidItem && !isSpinning() && spinCooldown <= 0) {

                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                String spinType = determineSpinType();
                setCurrentSpinType(spinType);

                ItemStack prize = getPrizeForAnimation(spinType);
                if (!prize.isEmpty()) {
                    schedulePrizeDelivery(player, prize, spinType);
                }

                this.playCasinoSound();

                setSpinning(true);
                spinStartTime = this.level().getGameTime();
                spinCooldown = 100;

                return InteractionResult.SUCCESS;
            }

            if (heldItem.isEmpty() || heldItem.getItem() == Items.AIR) {
                return InteractionResult.FAIL;
            }

            if (heldItem.getItem() != CasinoConfig.INTERACTION_ITEM) {
                return InteractionResult.FAIL;
            }

            if (isSpinning()) {
                return InteractionResult.FAIL;
            }

            if (spinCooldown > 0) {
                return InteractionResult.FAIL;
            }
        }

        if (this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (!heldItem.isEmpty() &&
                    heldItem.getItem() != Items.AIR &&
                    !isSpinning() &&
                    spinCooldown <= 0) {

                this.triggerAnim("spin_controller", getCurrentSpinType());
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (spinCooldown > 0) {
            spinCooldown--;
        }

        if (isSpinning() && this.level().getGameTime() - spinStartTime >= SPIN_DURATION) {
            setSpinning(false);
        }

        if (!this.level().isClientSide) {
            processPendingPrizes();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide && key.equals(CURRENT_SPIN_TYPE)) {
            if (isSpinning()) {
                this.triggerAnim("spin_controller", getCurrentSpinType());
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<CasinoEntity> controller = new AnimationController<>(this, "spin_controller", 0, this::spinPredicate);

        for (String winningAnim : SPIN_WINNING) {
            controller.triggerableAnim(winningAnim, RawAnimation.begin().thenPlay(winningAnim));
        }

        for (String semiWinningAnim : SPIN_SEMI_WINNING) {
            controller.triggerableAnim(semiWinningAnim, RawAnimation.begin().thenPlay(semiWinningAnim));
        }

        for (String losingAnim : SPIN_LOSING) {
            controller.triggerableAnim(losingAnim, RawAnimation.begin().thenPlay(losingAnim));
        }

        controllers.add(controller);
    }

    private PlayState spinPredicate(AnimationState<CasinoEntity> event) {
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}