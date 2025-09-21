package com.more_owleaf.entities.more;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public class RunaMerchantMenu extends MerchantMenu {

    public RunaMerchantMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory);
    }

    public RunaMerchantMenu(int containerId, Inventory playerInventory, Merchant merchant) {
        super(containerId, playerInventory, merchant);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}