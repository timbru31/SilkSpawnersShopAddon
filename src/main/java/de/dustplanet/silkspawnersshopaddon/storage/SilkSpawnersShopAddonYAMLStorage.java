package de.dustplanet.silkspawnersshopaddon.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;

public class SilkSpawnersShopAddonYAMLStorage extends SilkSpawnersShopAddonStorageImpl implements ISilkSpawnersShopAddonStorage {
    protected FileConfiguration shopConfiguration;
    private File shopFile;

    public SilkSpawnersShopAddonYAMLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading yaml storage provider");
        shopConfiguration = new YamlConfiguration();

        shopFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopFile.exists()) {
            try {
                shopFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create YAML shops file!");
                e.printStackTrace();
            }
        }
        // Try to load
        shopConfiguration = YamlConfiguration.loadConfiguration(shopFile);
    }

    private boolean saveToYAML(SilkSpawnersShop shop, boolean save) {
        ArrayList<String> data = new ArrayList<>();
        data.add(shop.getLocation().getWorld().getName());
        data.add(String.valueOf(shop.getLocation().getX()));
        data.add(String.valueOf(shop.getLocation().getY()));
        data.add(String.valueOf(shop.getLocation().getZ()));
        data.add(shop.getMode().toString());
        data.add(shop.getMob());
        data.add(String.valueOf(shop.getPrice()));
        shopConfiguration.set(shop.getId().toString(), data);
        if (save) {
            try {
                shopConfiguration.save(shopFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to add shop tp YAML");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean removeFromYAML(SilkSpawnersShop shop) {
        shopConfiguration.set(shop.getId().toString(), null);
        try {
            shopConfiguration.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to remove shop from YAML");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private void saveToYAML(ArrayList<SilkSpawnersShop> shops) {
        for (SilkSpawnersShop shop : shops) {
            saveToYAML(shop, false);
        }
    }

    private ArrayList<SilkSpawnersShop> loadShops() {
        Set<String> shopList = shopConfiguration.getKeys(false);
        ArrayList<SilkSpawnersShop> shops = new ArrayList<>();
        for (String shopID : shopList) {
            List<String> rawShop = shopConfiguration.getStringList(shopID);
            String world = rawShop.get(0);
            double x= Double.valueOf(rawShop.get(1));
            double y = Double.valueOf(rawShop.get(2));
            double z = Double.valueOf(rawShop.get(3));
            SilkSpawnersShop shop = new SilkSpawnersShop(x, y, z, world, SilkspawnersShopMode.getMode(rawShop.get(4)), rawShop.get(5), Double.valueOf(rawShop.get(6)), UUID.fromString(shopID));
            shops.add(shop);
        }
        return shops;
    }

    @Override
    public boolean addShop(SilkSpawnersShop shop) {
        cachedShops.add(shop);
        return saveToYAML(shop, true);
    }

    @Override
    public boolean removeShop(SilkSpawnersShop shop) {
        cachedShops.remove(shop);
        if (!shopConfiguration.contains(shop.getId().toString())) {
            return false;
        }
        return removeFromYAML(shop);
    }

    @Override
    public boolean updateShop(SilkSpawnersShop shop) {
        int index = cachedShops.indexOf(shop);
        if (index != -1) {
            cachedShops.set(index, shop);
        }
        return saveToYAML(shop, true);
    }

    @Override
    public boolean isShop(Sign sign) {
        // Try to find in cache
        for (SilkSpawnersShop shop : cachedShops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return true;
            }
        }

        // Lookup in yml
        ArrayList<SilkSpawnersShop> shops = loadShops();
        for (SilkSpawnersShop shop : shops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                cachedShops.add(shop);
                return true;
            }
        }
        return false;
    }

    @Override
    public SilkSpawnersShop getShop(Sign sign) {
        // Try to find in cache
        for (SilkSpawnersShop shop : cachedShops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return shop;
            }
        }

        // Lookup in yml
        ArrayList<SilkSpawnersShop> shops = loadShops();
        for (SilkSpawnersShop shop : shops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                cachedShops.add(shop);
                return shop;
            }
        }
        return null;
    }

    @Override
    public void disable() {
        super.disable();
    }

}
