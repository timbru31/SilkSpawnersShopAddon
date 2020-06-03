package de.dustplanet.silkspawnersshopaddon.storage;

import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;

/**
 * Common interface of the different storage implementations.
 *
 * @author timbru31
 */
public interface ISilkSpawnersShopAddonStorage {

    /**
     * Called when a new shop should be added.
     *
     * @param shop the shop to save
     * @return the operation result
     */
    boolean addShop(SilkSpawnersShop shop);

    /**
     * Called when a shop should be removed.
     *
     * @param shop the shop to remove
     * @return the operation result
     */
    boolean removeShop(SilkSpawnersShop shop);

    /**
     * Called when a new shop should be added.
     *
     * @param shopList the list of shops to remove
     * @return the operation result
     */
    boolean removeShops(List<SilkSpawnersShop> shopList);

    /**
     * Called when a shop should be updated.
     *
     * @param shop the shop to updated
     * @return the operation result
     */
    boolean updateShop(SilkSpawnersShop shop);

    /**
     * Method to return all known shops.
     *
     * @return the shop list
     */
    @Nullable
    List<SilkSpawnersShop> getAllShops();

    /**
     * Checks whether a given sign is a valid and recognized shop.
     *
     * @param sign the sign to check
     * @return the operation result
     */
    boolean isShop(Sign sign);

    /**
     * Attempts to return a shop from a sign.
     *
     * @param sign the sign to check
     * @return the shop, null otherwise
     */
    @Nullable
    SilkSpawnersShop getShop(Sign sign);

    /**
     * Called when the plugin disabled itself. Methods should cleanup any caches and gracefully close any database connections or save
     * files.
     */
    void disable();

    /**
     * Called when a database migration should be performed.
     *
     * @return the upgrade result
     */
    boolean upgradeDatabase();
}
