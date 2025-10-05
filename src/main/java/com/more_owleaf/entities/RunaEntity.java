package com.more_owleaf.entities;

import com.more_owleaf.config.RunaTradesConfig;
import com.more_owleaf.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
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
import java.util.UUID;

public class RunaEntity extends Entity implements GeoEntity, Merchant {
    private static final EntityDataAccessor<Integer> DATA_COLOR_VARIANT = SynchedEntityData.defineId(RunaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_RENDERER = SynchedEntityData.defineId(RunaEntity.class, EntityDataSerializers.ITEM_STACK);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Player tradingPlayer;
    private MerchantOffers offers;
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
        this.offers = new MerchantOffers();
        this.entityData.set(DATA_ITEM_RENDERER, ItemStack.EMPTY);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR_VARIANT, VARIANT_YELLOW);
        this.entityData.define(DATA_ITEM_RENDERER, ItemStack.EMPTY);
    }

    public int getColorVariant() {
        return this.entityData.get(DATA_COLOR_VARIANT);
    }

    public void setColorVariant(int variant) {
        this.entityData.set(DATA_COLOR_VARIANT, variant);
    }

    public ItemStack getItemRenderer() {
        return this.entityData.get(DATA_ITEM_RENDERER);
    }

    public void setItemRenderer(ItemStack itemStack) {
        this.entityData.set(DATA_ITEM_RENDERER, itemStack);
    }

    private void loadTrades() {
        if (!this.level().isClientSide) {
            RunaTradesConfig.ensureConfigExists();

            UUID runaId = this.getUUID();
            RunaTradesConfig.RunaConfig config = RunaTradesConfig.getConfigForRuna(runaId);

            this.offers.clear();
            for (MerchantOffer offer : config.getOffers()) {
                MerchantOffer newOffer = new MerchantOffer(
                        offer.getBaseCostA().copy(),
                        offer.getCostB().copy(),
                        offer.getResult().copy(),
                        0,
                        offer.getMaxUses(),
                        offer.getXp(),
                        offer.getPriceMultiplier()
                );
                this.offers.add(newOffer);
            }

            if (!config.getItemRenderer().isEmpty()) {
                this.setItemRenderer(config.getItemRenderer().copy());
            }

            if (this.offers.isEmpty()) {
                setupDefaultTrades();
                RunaTradesConfig.setTradesForRuna(runaId, this.offers);
            }

            tradesLoaded = true;
        }
    }

    public void reloadTrades() {
        tradesLoaded = false;
        this.offers.clear();
        RunaTradesConfig.forceReload();
        loadTrades();

        if (this.getTradingPlayer() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu) {
                serverPlayer.connection.send(new ClientboundMerchantOffersPacket(
                        serverPlayer.containerMenu.containerId,
                        this.offers,
                        0,
                        this.getVillagerXp(),
                        this.showProgressBar(),
                        this.canRestock()
                ));
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            loadTrades();

            if (this.offers.isEmpty()) {
                setupDefaultTrades();
            }

            this.setTradingPlayer(player);

            try {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, playerEntity) -> {
                            return new net.minecraft.world.inventory.MerchantMenu(containerId, playerInventory, this);
                        },
                        this.getDisplayName()
                ));

                this.level().getServer().execute(() -> {
                    if (serverPlayer.containerMenu instanceof net.minecraft.world.inventory.MerchantMenu) {
                        serverPlayer.connection.send(new ClientboundMerchantOffersPacket(
                                serverPlayer.containerMenu.containerId,
                                this.offers,
                                0,
                                this.getVillagerXp(),
                                this.showProgressBar(),
                                this.canRestock()
                        ));
                    }
                });

            } catch (Exception e) {
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
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (!this.level().isClientSide && (!tradesLoaded || this.offers.isEmpty())) {
            loadTrades();
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        if (offers != null) {
            this.offers = offers;
            tradesLoaded = true;
        }
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
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
    public boolean canRestock() {
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

        if (compound.contains("ItemRenderer")) {
            CompoundTag itemTag = compound.getCompound("ItemRenderer");
            ItemStack itemStack = ItemStack.of(itemTag);
            this.setItemRenderer(itemStack);
        }

        if (compound.contains("Offers")) {
            this.offers = new MerchantOffers(compound.getCompound("Offers"));
            tradesLoaded = true;
        } else {
            tradesLoaded = false;
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("ColorVariant", getColorVariant());

        if (!getItemRenderer().isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            getItemRenderer().save(itemTag);
            compound.put("ItemRenderer", itemTag);
        }

        compound.put("Offers", this.offers.createTag());
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

        RunaTradesConfig.registerRuna(this.getUUID());

        if (!this.level().isClientSide) {
            tradesLoaded = false;
            this.offers.clear();
            loadTrades();
        }
    }

    private void setupDefaultTrades() {
        this.offers.clear();

        this.offers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                ItemStack.EMPTY,
                new ItemStack(Items.DIAMOND, 1),
                Integer.MAX_VALUE, 0, 0.0f
        ));

        this.offers.add(new MerchantOffer(
                new ItemStack(Items.IRON_INGOT, 3),
                ItemStack.EMPTY,
                new ItemStack(Items.EMERALD, 1),
                Integer.MAX_VALUE, 0, 0.0f
        ));

        this.offers.add(new MerchantOffer(
                new ItemStack(Items.COAL, 8),
                new ItemStack(Items.STICK, 4),
                new ItemStack(Items.TORCH, 16),
                Integer.MAX_VALUE, 0, 0.0f
        ));

        switch (getColorVariant()) {
            case VARIANT_YELLOW:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.GOLD_INGOT, 2),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD, 3),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_LIGHT_BLUE:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.DIAMOND, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD, 4),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_MAGENTA:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.REDSTONE, 8),
                        ItemStack.EMPTY,
                        new ItemStack(Items.DIAMOND, 1),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_PURPLE:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.ENDER_PEARL, 1),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EXPERIENCE_BOTTLE, 3),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_RED:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.IRON_INGOT, 4),
                        ItemStack.EMPTY,
                        new ItemStack(Items.REDSTONE, 8),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
            case VARIANT_GREEN:
                this.offers.add(new MerchantOffer(
                        new ItemStack(Items.WHEAT, 16),
                        ItemStack.EMPTY,
                        new ItemStack(Items.EMERALD, 1),
                        Integer.MAX_VALUE, 0, 0.0f
                ));
                break;
        }
    }
}