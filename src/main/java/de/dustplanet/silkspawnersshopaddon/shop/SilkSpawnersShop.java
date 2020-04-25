package de.dustplanet.silkspawnersshopaddon.shop;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import lombok.Getter;
import lombok.Setter;

public class SilkSpawnersShop implements ISilkSpawnersShop, Serializable {
    private static final long serialVersionUID = 4375338308025596438L;
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
}
