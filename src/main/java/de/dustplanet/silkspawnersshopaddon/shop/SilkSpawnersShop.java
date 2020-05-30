package de.dustplanet.silkspawnersshopaddon.shop;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@SuppressFBWarnings({ "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "IMC_IMMATURE_CLASS_BAD_SERIALVERSIONUID" })
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

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, int amount, double price,
            UUID id) {
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

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, int amount, double price,
            String id) {
        this(x, y, z, world, mode, mob, amount, price, UUID.fromString(id));
    }

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, int amount, double price) {
        this(x, y, z, world, mode, mob, amount, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, int amount, double price) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, int amount, double price, UUID id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, id);
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, int amount, double price, String id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode,
                mob, amount, price, UUID.fromString(id));
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, int amount, double price) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, amount, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, int amount, double price, UUID id) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, amount, price, id);
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, int amount, double price, String id) {
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
