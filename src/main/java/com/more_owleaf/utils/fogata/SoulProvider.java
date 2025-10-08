package com.more_owleaf.utils.fogata;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulProvider implements ICapabilitySerializable<CompoundTag> {

    private final ISoul soul;
    private final LazyOptional<ISoul> optional;

    public SoulProvider(int initialSouls) {
        this.soul = new Soul(initialSouls);
        this.optional = LazyOptional.of(() -> this.soul);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return SoulUtil.SOUL_CAPABILITY.orEmpty(cap, this.optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("souls", this.soul.getSouls());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.soul.setSouls(nbt.getInt("souls"));
    }
}