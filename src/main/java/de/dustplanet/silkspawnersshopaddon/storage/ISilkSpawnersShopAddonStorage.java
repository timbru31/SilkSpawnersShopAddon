package de.dustplanet.silkspawnersshopaddon.storage;

import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;

public interface ISilkSpawnersShopAddonStorage {

    boolean addShop(SilkSpawnersShop shop);

    boolean removeShop(SilkSpawnersShop shop);

    boolean updateShop(SilkSpawnersShop shop);

    boolean isShop(Sign sign);

    SilkSpawnersShop getShop(Sign sign);

    void disable();
}
