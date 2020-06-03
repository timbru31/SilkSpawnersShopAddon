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
import de.dustplanet.silkspawnersshopaddon.shop.ISilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * The YAML implementation of the storage class.
 *
 * @author timbru31
 */
public class SilkSpawnersShopAddonYamlStorage extends SilkSpawnersShopAddonStorageCleanupTaskTimer
        implements ISilkSpawnersShopAddonStorage {
    @Getter
    private FileConfiguration shopConfiguration;
    @Getter
    private final File shopFile;

    @SuppressFBWarnings({ "SCII_SPOILED_CHILD_INTERFACE_IMPLEMENTOR", "IMC_IMMATURE_CLASS_NO_TOSTRING",
            "PCOA_PARTIALLY_CONSTRUCTED_OBJECT_ACCESS" })
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SilkSpawnersShopAddonYamlStorage(final SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading yaml storage provider");
        shopConfiguration = new YamlConfiguration();

        shopFile = new File(plugin.getDataFolder(), "shops.yml");
        if (!shopFile.exists()) {
            try {
                final boolean success = shopFile.createNewFile();
                if (!success) {
                    throw new IOException();
                }
            } catch (final IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create YAML shops file!", e);
            }
        }
        shopConfiguration = YamlConfiguration.loadConfiguration(getShopFile());
    }

    @SuppressFBWarnings({ "BL_BURYING_LOGIC", "EXS_EXCEPTION_SOFTENING_RETURN_FALSE", "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS" })
    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean saveToYAML(final ISilkSpawnersShop shop, final boolean save) {
        final Location shopLocation = shop.getLocation();
        final World world = shopLocation.getWorld();
        if (world == null) {
            return false;
        }
        final List<String> data = new ArrayList<>();
        final String worldName = world.getName();
        data.add(worldName);
        data.add(String.valueOf(shopLocation.getX()));
        data.add(String.valueOf(shopLocation.getY()));
        data.add(String.valueOf(shopLocation.getZ()));
        data.add(shop.getMode().toString());
        data.add(shop.getMob());
        data.add(String.valueOf(shop.getPrice()));
        data.add(String.valueOf(shop.getAmount()));
        getShopConfiguration().set(shop.getId().toString(), data);
        if (save) {
            try {
                getShopConfiguration().save(getShopFile());
                return true;
            } catch (final IOException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Failed to add shop tp YAML", e);
            }
            return false;
        }
        return true;
    }

    private boolean removeFromYAML(final ISilkSpawnersShop shop) {
        getShopConfiguration().set(shop.getId().toString(), null);
        return saveYAML();
    }

    private boolean saveYAML() {
        try {
            getShopConfiguration().save(getShopFile());
            return true;
        } catch (final IOException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to save the YAML", e);
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "PMD.ShortVariable", "checkstyle:MagicNumber", "PMD.DataflowAnomalyAnalysis" })
    public List<SilkSpawnersShop> getAllShops() {
        final Set<String> shopSet = getShopConfiguration().getKeys(false);
        final List<SilkSpawnersShop> shops = new ArrayList<>(shopSet.size());
        for (final String shopID : shopSet) {
            final List<String> rawShop = getShopConfiguration().getStringList(shopID);
            final String world = rawShop.get(0);
            final double x = Double.parseDouble(rawShop.get(1));
            final double y = Double.parseDouble(rawShop.get(2));
            final double z = Double.parseDouble(rawShop.get(3));
            int amount;
            try {
                amount = Integer.parseInt(rawShop.get(7));
            } catch (@SuppressWarnings("unused") final IndexOutOfBoundsException e) {
                amount = 1;
            }
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
            final SilkSpawnersShop shop = new SilkSpawnersShop(x, y, z, world, SilkspawnersShopMode.getMode(rawShop.get(4)), rawShop.get(5),
                    amount, Double.parseDouble(rawShop.get(6)), UUID.fromString(shopID));
            shops.add(shop);
        }
        return shops;
    }

    @Override
    public boolean addShop(final SilkSpawnersShop shop) {
        getCachedShops().add(shop);
        return saveToYAML(shop, true);
    }

    @Override
    public boolean removeShop(final SilkSpawnersShop shop) {
        getCachedShops().remove(shop);
        if (!getShopConfiguration().contains(shop.getId().toString())) {
            return false;
        }
        return removeFromYAML(shop);
    }

    @Override
    public boolean removeShops(final List<SilkSpawnersShop> shopList) {
        for (final SilkSpawnersShop shop : shopList) {
            getCachedShops().remove(shop);
            getShopConfiguration().set(shop.getId().toString(), null);
        }
        return saveYAML();
    }

    @Override
    public boolean updateShop(final SilkSpawnersShop shop) {
        final int index = getCachedShops().indexOf(shop);
        if (index != -1) {
            getCachedShops().set(index, shop);
        }
        return saveToYAML(shop, true);
    }

    @Override
    public boolean isShop(final Sign sign) {
        // Try to find in cache
        for (final SilkSpawnersShop shop : getCachedShops()) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return true;
            }
        }

        // Lookup in yml
        final List<SilkSpawnersShop> shops = getAllShops();
        for (final SilkSpawnersShop shop : shops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                getCachedShops().add(shop);
                return true;
            }
        }
        return false;
    }

    @Override
    public SilkSpawnersShop getShop(final Sign sign) {
        // Try to find in cache
        for (final SilkSpawnersShop shop : getCachedShops()) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return shop;
            }
        }

        // Lookup in yml
        final List<SilkSpawnersShop> shops = getAllShops();
        for (final SilkSpawnersShop shop : shops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                getCachedShops().add(shop);
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
