package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.UUID;

import org.bukkit.Location;

/**
 * The interface of a SilkSpawners shop.
 *
 * @author timbru31
 */
public interface ISilkSpawnersShop {

    /**
     * Returns the location of the shop as a Bukkit location.
     *
     * @return the location
     */
    Location getLocation();

    /**
     * Gets the shop's operation mode.
     *
     * @return the operation mode
     */
    SilkspawnersShopMode getMode();

    /**
     * Sets the shop's operation mode.
     *
     * @param mode the new operation mode
     */
    void setMode(SilkspawnersShopMode mode);

    /**
     * Gets a shop's price.
     *
     * @return the shop's price
     */
    double getPrice();

    /**
     * Sets a new shop price.
     *
     * @param price the new price to charge
     */
    void setPrice(double price);

    /**
     * Gets the mob of the shop.
     *
     * @return the mob the shop holds
     */
    String getMob();

    /**
     * Sets the mob the shop hold.
     *
     * @param mob the new mob to sell/buy
     */
    void setMob(String mob);

    /**
     * Gets the internal ID of the shop.
     *
     * @return the internal ID
     */
    UUID getId();

    /**
     * Sets the internal ID of a shop.
     *
     * @param id the new internal ID
     */
    @SuppressWarnings({ "PMD.ShortVariable", "PMD.AvoidDuplicateLiterals" })
    void setId(UUID id);

    /**
     * Gets the X coordinate.
     *
     * @return the X coordinate
     */
    double getX();

    /**
     * Sets the X coordinate.
     *
     * @param x the X coordinate
     */
    @SuppressWarnings("PMD.ShortVariable")
    void setX(double x);

    /**
     * Gets the Y coordinate.
     *
     * @return the y coordinate
     */
    double getY();

    /**
     * Sets the Y coordinate.
     *
     * @param y the Y coordinate
     */
    @SuppressWarnings("PMD.ShortVariable")
    void setY(double y);

    /**
     * Gets the Z coordinate.
     *
     * @return the Z coordinate
     */
    double getZ();

    /**
     * Sets the Z coordinate.
     *
     * @param z the Z coordinate
     */
    @SuppressWarnings("PMD.ShortVariable")
    void setZ(double z);

    /**
     * Gets the world name of the shop.
     *
     * @return name of the world the shop is located in
     */
    String getWorld();

    /**
     * Sets the world where the shop is located in.
     *
     * @param world the new world name
     */
    void setWorld(String world);

    /**
     * Gets the amount.
     *
     * @return the amount
     */
    int getAmount();

    /**
     * Sets the amount.
     *
     * @param amount the new amount
     */
    void setAmount(int amount);
}
