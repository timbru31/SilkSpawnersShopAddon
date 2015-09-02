package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.UUID;

import org.bukkit.Location;

public interface ISilkSpawnersShop {

    Location getLocation();

    SilkspawnersShopMode getMode();

    void setMode(SilkspawnersShopMode mode);

    double getPrice();

    void setPrice(double price);

    String getMob();

    void setMob(String mob);

    UUID getId();

    void setId(UUID id);

    double getX();

    void setX(double x);

    double getY();

    void setY(double y);

    double getZ();

    void setZ(double z);

    String getWorld();

    void setWorld(String world);
}
