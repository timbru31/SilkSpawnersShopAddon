package de.dustplanet.silkspawnersshopaddon.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SilkSpawnersShopAddonYAMLStorage extends SilkSpawnersShopAddonStorageImpl implements ISilkSpawnersShopAddonStorage {
    protected FileConfiguration shopConfiguration;
    private File shopFile;

    @SuppressFBWarnings({ "SCII_SPOILED_CHILD_INTERFACE_IMPLEMENTOR", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    public SilkSpawnersShopAddonYAMLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading yaml storage provider");
        shopConfiguration = new YamlConfiguration();

        shopFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopFile.exists()) {
            try {
                boolean success = shopFile.createNewFile();
                if (!success) {
                    throw new IOException();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create YAML shops file!", e);
            }
        }
        shopConfiguration = YamlConfiguration.loadConfiguration(shopFile);
    }

    @SuppressFBWarnings({ "BL_BURYING_LOGIC", "EXS_EXCEPTION_SOFTENING_RETURN_FALSE", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS" })
    private boolean saveToYAML(SilkSpawnersShop shop, boolean save) {
        List<String> data = new ArrayList<>();
        Location shopLocation = shop.getLocation();
        World world = shopLocation.getWorld();
        if (world == null) {
            return false;
        }
        String worldName = world.getName();
        data.add(worldName);
        data.add(String.valueOf(shopLocation.getX()));
        data.add(String.valueOf(shopLocation.getY()));
        data.add(String.valueOf(shopLocation.getZ()));
        data.add(shop.getMode().toString());
        data.add(shop.getMob());
        data.add(String.valueOf(shop.getPrice()));
        data.add(String.valueOf(shop.getAmount()));
        shopConfiguration.set(shop.getId().toString(), data);
        if (save) {
            try {
                shopConfiguration.save(shopFile);
                return true;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to add shop tp YAML", e);
            }
            return false;
        }
        return true;
    }

    private boolean removeFromYAML(SilkSpawnersShop shop) {
        shopConfiguration.set(shop.getId().toString(), null);
        return saveYAML();
    }

    private boolean saveYAML() {
        try {
            shopConfiguration.save(shopFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save the YAML", e);
        }
        return false;
    }

    @Override
    public List<SilkSpawnersShop> getAllShops() {
        Set<String> shopSet = shopConfiguration.getKeys(false);
        List<SilkSpawnersShop> shops = new ArrayList<>(shopSet.size());
        for (String shopID : shopSet) {
            List<String> rawShop = shopConfiguration.getStringList(shopID);
            String world = rawShop.get(0);
            double x = Double.parseDouble(rawShop.get(1));
            double y = Double.parseDouble(rawShop.get(2));
            double z = Double.parseDouble(rawShop.get(3));
            int amount;
            try {
                amount = Integer.parseInt(rawShop.get(7));
            } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
                amount = 1;
            }
            SilkSpawnersShop shop = new SilkSpawnersShop(x, y, z, world, SilkspawnersShopMode.getMode(rawShop.get(4)), rawShop.get(5),
                    amount, Double.parseDouble(rawShop.get(6)), UUID.fromString(shopID));
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
    public boolean removeShops(List<SilkSpawnersShop> shopList) {
        for (SilkSpawnersShop shop : shopList) {
            cachedShops.remove(shop);
            shopConfiguration.set(shop.getId().toString(), null);
        }
        return saveYAML();
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
        List<SilkSpawnersShop> shops = getAllShops();
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
        List<SilkSpawnersShop> shops = getAllShops();
        for (SilkSpawnersShop shop : shops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                cachedShops.add(shop);
                return shop;
            }
        }
        return null;
    }

    @Override
    public boolean upgradeDatabase() {
        // not necessary with YAML
        return false;
    }
}
