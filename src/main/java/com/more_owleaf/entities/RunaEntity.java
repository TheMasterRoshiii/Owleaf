package com.more_owleaf.entities;

import com.more_owleaf.config.RunaTradesConfig;
import com.more_owleaf.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class RunaEntity extends Entity implements GeoEntity, Merchant {
    private static final EntityDataAccessor<Integer> DATA_COLOR_VARIANT = SynchedEntityData.defineId(RunaEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();
    private boolean tradesLoaded = false;

    public static final int VARIANT_YELLOW = 0;
    public static final int VARIANT_LIGHT_BLUE = 1;
    public static final int VARIANT_MAGENTA = 2;
    public static final int VARIANT_PURPLE = 3;
    public static final int VARIANT_RED = 4;
    public static final int VARIANT_GREEN = 5;

    public RunaEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR_VARIANT, VARIANT_YELLOW);
    }

    public int getColorVariant() {
        return this.entityData.get(DATA_COLOR_VARIANT);
    }

    public void setColorVariant(int variant) {
        this.entityData.set(DATA_COLOR_VARIANT, variant);
    }

    private void loadTrades() {
        if (!this.level().isClientSide && (!tradesLoaded || this.offers.isEmpty())) {
            if (!RunaTradesConfig.isConfigLoaded()) {
                RunaTradesConfig.loadConfig();
            }

            this.offers = RunaTradesConfig.getTradesForRuna(this.getUUID());

            if (this.offers.isEmpty()) {
                setupDefaultTrades();
                RunaTradesConfig.setTradesForRuna(this.getUUID(), this.offers);
            }

            tradesLoaded = true;
            System.out.println("Trades loaded for runa " + this.getUUID() + ": " + this.offers.size() + " offers");
            for (MerchantOffer offer : this.offers) {
                System.out.println("Trade: " + offer.getBaseCostA().getItem() + " x" + offer.getBaseCostA().getCount() +
                        " -> " + offer.getResult().getItem() + " x" + offer.getResult().getCount());
            }
        }
    }

    public void reloadTrades() {
        tradesLoaded = false;
        this.offers.clear();
        loadTrades();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            loadTrades();

            if (this.offers.isEmpty()) {
                player.sendSystemMessage(Component.literal("§cEsta runa no tiene trades configurados"));
                return InteractionResult.FAIL;
            }

            this.setTradingPlayer(player);

            try {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, playerEntity) -> {
                            net.minecraft.world.inventory.MerchantMenu menu = new net.minecraft.world.inventory.MerchantMenu(containerId, playerInventory, this);
                            return menu;
                        },
                        this.getDisplayName()
                ));

                System.out.println("Opening merchant menu for player: " + player.getName().getString());
                System.out.println("Available trades: " + this.offers.size());
                System.out.println("Trading player: " + (this.getTradingPlayer() != null ? this.getTradingPlayer().getName().getString() : "null"));

            } catch (Exception e) {
                System.err.println("Error opening merchant menu: " + e.getMessage());
                e.printStackTrace();
                this.setTradingPlayer(null);
                return InteractionResult.FAIL;
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
        if (player != null) {
            System.out.println("Trading player set: " + player.getName().getString());
        } else {
            System.out.println("Trading player cleared");
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        loadTrades();
        System.out.println("getOffers() called, returning " + this.offers.size() + " offers");
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        if (offers != null) {
            this.offers = offers;
            System.out.println("Offers overridden with " + offers.size() + " trades");
        }
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        System.out.println("Trade executed: " + offer.getResult().getItem().toString());
        if (this.getTradingPlayer() != null) {
            this.level().playSound(null, this.blockPosition(), this.getNotifyTradeSound(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xp) {}

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    public String getVariantName() {
        switch (getColorVariant()) {
            case VARIANT_YELLOW: return "amarilla";
            case VARIANT_LIGHT_BLUE: return "azul_claro";
            case VARIANT_MAGENTA: return "magenta";
            case VARIANT_PURPLE: return "morada";
            case VARIANT_RED: return "roja";
            case VARIANT_GREEN: return "verde";
            default: return "amarilla";
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Runa " + getVariantName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("ColorVariant")) {
            setColorVariant(compound.getInt("ColorVariant"));
        }
        tradesLoaded = false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("ColorVariant", getColorVariant());
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<RunaEntity> controller = new AnimationController<>(this, "idle_controller", 0, this::idlePredicate);
        controllers.add(controller);
    }

    private PlayState idlePredicate(AnimationState<RunaEntity> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        EntityType<?> type = this.getType();
        if (type == EntityInit.RUNA_AMARILLA.get()) setColorVariant(VARIANT_YELLOW);
        else if (type == EntityInit.RUNA_AZUL_CLARO.get()) setColorVariant(VARIANT_LIGHT_BLUE);
        else if (type == EntityInit.RUNA_MAGENTA.get()) setColorVariant(VARIANT_MAGENTA);
        else if (type == EntityInit.RUNA_MORADA.get()) setColorVariant(VARIANT_PURPLE);
        else if (type == EntityInit.RUNA_ROJA.get()) setColorVariant(VARIANT_RED);
        else if (type == EntityInit.RUNA_VERDE.get()) setColorVariant(VARIANT_GREEN);

        tradesLoaded = false;
        this.offers.clear();
    }

    private void setupDefaultTrades() {
        this.offers.clear();

        switch (getColorVariant()) {
            case VARIANT_YELLOW:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.EMERALD, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.GOLD_INGOT, 3),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_LIGHT_BLUE:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.DIAMOND, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD, 2),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_MAGENTA:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.REDSTONE, 10),
                        ItemStack.EMPTY,
                        new ItemStack(Items.DIAMOND, 1),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_PURPLE:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.ENDER_PEARL, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EXPERIENCE_BOTTLE, 5),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_RED:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.IRON_INGOT, 3),
                        ItemStack.EMPTY,
                        new ItemStack(Items.REDSTONE, 10),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_GREEN:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.WHEAT, 20),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD, 1),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            default:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.IRON_INGOT, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.COAL, 5),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
        }
    }
}