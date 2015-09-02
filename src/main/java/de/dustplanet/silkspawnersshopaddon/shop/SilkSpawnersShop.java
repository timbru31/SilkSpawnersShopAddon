package de.dustplanet.silkspawnersshopaddon.shop;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class SilkSpawnersShop implements ISilkSpawnersShop, Serializable {
    private static final long serialVersionUID = -2341283L;
    private double x;
    private double y;
    private double z;
    private String world;
    private double price;
    private SilkspawnersShopMode mode;
    private String mob;
    private UUID id;

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, double price, UUID id) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.id = id;
    }

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, double price, String id) {
        this(x, y, z, world, mode, mob, price, UUID.fromString(id));
    }

    public SilkSpawnersShop(double x, double y, double z, String world, SilkspawnersShopMode mode, String mob, double price) {
        this(x, y, z, world, mode, mob, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, double price) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode, mob, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, double price, UUID id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode, mob, price, id);
    }

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, double price, String id) {
        this(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ(), sign.getLocation().getWorld().getName(), mode, mob, price, UUID.fromString(id));
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, price, UUID.randomUUID());
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price, UUID id) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, price, id);
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price, String id) {
        this(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), mode, mob, price, UUID.fromString(id));
    }

    @Override
    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public SilkspawnersShopMode getMode() {
        return this.mode;
    }

    @Override
    public void setMode(SilkspawnersShopMode mode) {
        this.mode = mode;
    }

    @Override
    public double getPrice() {
        return this.price;
    }

    @Override
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String getMob() {
        return this.mob;
    }

    @Override
    public void setMob(String mob) {
        this.mob = mob;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public double getZ() {
        return z;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public void setWorld(String world) {
        this.world = world;
    }
}
