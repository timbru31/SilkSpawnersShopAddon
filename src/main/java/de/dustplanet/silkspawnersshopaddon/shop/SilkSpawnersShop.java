package de.dustplanet.silkspawnersshopaddon.shop;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

/**
 * The implementation of a SilkSpawners shop.
 *
 * @author timbru31
 */
@SuppressFBWarnings({ "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "IMC_IMMATURE_CLASS_BAD_SERIALVERSIONUID" })
@SuppressWarnings({ "PMD.CommentSize", "PMD.ShortVariable" })
public class SilkSpawnersShop implements ISilkSpawnersShop, Serializable {
    private static final long serialVersionUID = 1282156635646920413L;
    @Getter
    @Setter
    private double x;
    @Getter
    @Setter
    private double y;
    @Getter
    @Setter
    private double z;
    @Getter
    @Setter
    private String world;
    @Getter
    @Setter
    private double price;
    @Getter
    @Setter
    private SilkspawnersShopMode mode;
    @Getter
    @Setter
    private String mob;
    @Getter
    @Setter
    private UUID id;
    @Getter
    @Setter
    private int amount;

    /**
     * Constructs a new SilkSpawners shop with the given values.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param world the world name the shop is located in
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public SilkSpawnersShop(final double x, final double y, final double z, final String world, final SilkspawnersShopMode mode,
            final String mob, final int amount, final double price, final UUID id) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.id = id;
        this.amount = amount;
    }

    /**
     * Constructs a new SilkSpawners shop with the given values.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param world the world name the shop is located in
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID as a string representation
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public SilkSpawnersShop(final double x, final double y, final double z, final String world, final SilkspawnersShopMode mode,
            final String mob, final int amount, final double price, final String id) {
        this(x, y, z, world, mode, mob, amount, price, UUID.fromString(id));
    }

    /**
     * Constructs a new SilkSpawners shop with the given values. The UUID is randomly generated.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param world the world name the shop is located in
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public SilkSpawnersShop(final double x, final double y, final double z, final String world, final SilkspawnersShopMode mode,
            final String mob, final int amount, final double price) {
        this(x, y, z, world, mode, mob, amount, price, UUID.randomUUID());
    }

    /**
     * Constructs a new SilkSpawners shop with the given values. The location is extracted from the given sign and an UUID generated.
     *
     * @param sign the sign the location should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     */
    public SilkSpawnersShop(final Sign sign, final SilkspawnersShopMode mode, final String mob, final int amount, final double price) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, UUID.randomUUID());
    }

    /**
     * Constructs a new SilkSpawners shop with the given values. The location is extracted from the given sign and an UUID generated.
     *
     * @param sign the sign the location should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID
     */
    public SilkSpawnersShop(final Sign sign, final SilkspawnersShopMode mode, final String mob, final int amount, final double price,
            final UUID id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, id);
    }

    /**
     * Constructs a new SilkSpawners shop with the given values. The location is extracted from the given sign.
     *
     * @param sign the sign the location should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID as a string representation
     */
    public SilkSpawnersShop(final Sign sign, final SilkspawnersShopMode mode, final String mob, final int amount, final double price,
            final String id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, UUID.fromString(id));
    }

    /**
     * Constructs a new SilkSpawners shop with the given values. The UUID is randomly generated.
     *
     * @param location the location where the coordinates should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     */
    public SilkSpawnersShop(final Location location, final SilkspawnersShopMode mode, final String mob, final int amount,
            final double price) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, amount, price, UUID.randomUUID());
    }

    /**
     * Constructs a new SilkSpawners shop with the given values.
     *
     * @param location the location where the coordinates should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID
     */
    @SuppressWarnings("PMD.ShortVariable")
    public SilkSpawnersShop(final Location location, final SilkspawnersShopMode mode, final String mob, final int amount,
            final double price, final UUID id) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, amount, price, id);
    }

    /**
     * Constructs a new SilkSpawners shop with the given values.
     *
     * @param location the location where the coordinates should be extracted from
     * @param mode the operation mode (buy or sell)
     * @param mob the configured mob
     * @param amount the amount of mobs to buy/sell
     * @param price the price
     * @param id the internal shop ID as a string representation
     */
    @SuppressWarnings("PMD.ShortVariable")
    public SilkSpawnersShop(final Location location, final SilkspawnersShopMode mode, final String mob, final int amount,
            final double price, final String id) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, amount, price,
                UUID.fromString(id));
    }

    @Override
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public String toString() {
        return "SilkSpawnersShop [" + this.id + "] at [" + this.getLocation() + "] with mode [" + this.mode + "], mob [" + this.mob
                + "], amount [" + this.amount + "] and price [" + this.price + "]";
    }
}
