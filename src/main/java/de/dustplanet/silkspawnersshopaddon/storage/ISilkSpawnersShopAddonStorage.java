package de.dustplanet.silkspawnersshopaddon.storage;

import java.util.List;

import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;

public interface ISilkSpawnersShopAddonStorage {

    boolean addShop(SilkSpawnersShop shop);

    boolean removeShop(SilkSpawnersShop shop);

    boolean removeShops(List<SilkSpawnersShop> shopList);

    boolean updateShop(SilkSpawnersShop shop);

    List<SilkSpawnersShop> getAllShops();

    boolean isShop(Sign sign);

    SilkSpawnersShop getShop(Sign sign);

    void disable();

    boolean upgradeDatabase();
}
