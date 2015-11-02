package de.dustplanet.silkspawnersshopaddon.storage;

import java.util.ArrayList;

import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;

public interface ISilkSpawnersShopAddonStorage {

    boolean addShop(SilkSpawnersShop shop);

    boolean removeShop(SilkSpawnersShop shop);

    boolean removeShops(ArrayList<SilkSpawnersShop> shopList);

    boolean updateShop(SilkSpawnersShop shop);

    ArrayList<SilkSpawnersShop> getAllShops();

    boolean isShop(Sign sign);

    SilkSpawnersShop getShop(Sign sign);

    void disable();
}
