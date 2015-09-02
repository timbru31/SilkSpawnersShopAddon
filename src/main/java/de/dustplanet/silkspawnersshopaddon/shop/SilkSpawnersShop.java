package de.dustplanet.silkspawnersshopaddon.shop;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class SilkSpawnersShop implements ISilkSpawnersShop, Serializable {
    private static final long serialVersionUID = -2341283L;
    private Location location;
    private double price;
    private SilkspawnersShopMode mode;
    private String mob;
    private UUID id = UUID.randomUUID();

    public SilkSpawnersShop(Sign sign, SilkspawnersShopMode mode, String mob, double price) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.location = sign.getLocation();
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.location = location;
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price, UUID id) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.location = location;
        this.id = id;
    }

    public SilkSpawnersShop(Location location, SilkspawnersShopMode mode, String mob, double price, String id) {
        this.mode = mode;
        this.price = price;
        this.mob = mob;
        this.location = location;
        this.id = UUID.fromString(id);
    }

    @Override
    public Location getLocation() {
        return this.location;
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
}
