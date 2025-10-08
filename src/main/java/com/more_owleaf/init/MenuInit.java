package com.more_owleaf.init;

import com.more_owleaf.More_Owleaf;
import com.more_owleaf.entities.more.RunaMerchantMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuInit {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, More_Owleaf.MODID);

    public static final RegistryObject<MenuType<RunaMerchantMenu>> RUNA_MERCHANT = MENUS.register(
            "runa_merchant",
            () -> IForgeMenuType.create((windowId, inv, data) -> {
                return new RunaMerchantMenu(windowId, inv);
            })

    );
}