package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.storage.ISilkSpawnersShopAddonStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonHSQLDBStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonMongoStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonMySQLStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonYAMLStorage;
import de.dustplanet.util.SilkUtil;

public class SilkSpawnersShopManager {
    private SilkUtil su;
    private SilkSpawnersShopAddon plugin;
    private ISilkSpawnersShopAddonStorage storage;

    public SilkSpawnersShopManager(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
        this.su = plugin.getSilkUtil();
        String storageMethod = plugin.getConfig().getString("storageMethod").toUpperCase();
        switch (storageMethod) {
        case "YML":
        case "YAML":
            setStorage(new SilkSpawnersShopAddonYAMLStorage(plugin));
            break;
        case "MONGODB":
        case "MONGO":
            setStorage(new SilkSpawnersShopAddonMongoStorage(plugin));
            break;
        case "HSQLDB":
            setStorage(new SilkSpawnersShopAddonHSQLDBStorage(plugin));
            break;
        case "MYSQL":
            setStorage(new SilkSpawnersShopAddonMySQLStorage(plugin));
            break;
        default:
            setStorage(new SilkSpawnersShopAddonYAMLStorage(plugin));
            break;
        }
    }

    public ISilkSpawnersShopAddonStorage getStorage() {
        return storage;
    }

    public void setStorage(ISilkSpawnersShopAddonStorage storage) {
        this.storage = storage;
    }

    public boolean removeShop(SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.removeShop(shop);
    }

    public boolean removeShop(Sign sign) {
        if (sign == null || !isShop(sign)) {
            return false;
        }
        return removeShop(getShop(sign));
    }

    public boolean removeShops(ArrayList<SilkSpawnersShop> shopList) {
        return storage.removeShops(shopList);
    }

    public boolean addShop(SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.addShop(shop);
    }

    public boolean updateShop(SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.updateShop(shop);
    }

    public boolean isShop(Sign sign) {
        if (sign == null) {
            return false;
        }
        return storage.isShop(sign);
    }

    public SilkSpawnersShop getShop(Sign sign) {
        return storage.getShop(sign);
    }

    public ArrayList<SilkSpawnersShop> getAllShops() {
        return storage.getAllShops();
    }

    public void handleShopInteraction(Player player, Sign sign, boolean hasItem, ItemStack item) {
        SilkSpawnersShop shop = getShop(sign);
        SilkspawnersShopMode mode = shop.getMode();
        switch (mode) {
        case BUY:
            handleBuy(player, shop);
            break;
        case SELL:
            handleSell(player, shop, hasItem, item);
            break;
        default:
            plugin.getServer().getLogger().warning("Detected invalid mode, removing shop @" + shop.getLocation().toString());
            removeShop(shop);
            break;
        }
    }

    public void handleBuy(Player player, SilkSpawnersShop shop) {
        if (player.hasPermission("silkspawners.use.buy")) {
            String mob = shop.getMob();
            double price = shop.getPrice();
            short entityID = su.name2Eid.get(mob);
            if (!plugin.getEcon().has(player, price)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("buying.notEnoughMoney")));
                return;
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("buying.inventoryFull")));
                return;
            }
            plugin.getEcon().withdrawPlayer(player, price);
            mob = su.getCreatureName(entityID);
            player.getInventory().addItem(su.newSpawnerItem(entityID, su.getCustomSpawnerName(su.eid2MobID.get(entityID)), 1, false));
            if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
                player.updateInventory();
            }
            String priceString = plugin.getCurrencySign() + Double.toString(price);
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("buying.success").replace("%creature%", mob).replace("%price%", priceString)));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.buy")));
        }
    }

    public void handleSell(Player player, SilkSpawnersShop shop, boolean hasItem, ItemStack item) {
        if (player.hasPermission("silkspawners.use.sell")) {
            String mob = shop.getMob();
            double price = shop.getPrice();
            short entityID = su.name2Eid.get(mob);
            if (!hasItem || !(item.getType() == Material.MOB_SPAWNER)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.noSpawnerInHand")));
                return;
            }
            short entityIDInHand = su.getStoredSpawnerItemEntityID(item);
            String creatureName = su.getCreatureName(entityID);
            if (entityIDInHand != entityID) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.notTheSameMob").replace("%creature%", creatureName)));
                return;
            }
            plugin.getEcon().depositPlayer(player, price);
            ItemStack itemInHand = player.getItemInHand();
            if (itemInHand.getAmount() == 1) {
                player.setItemInHand(null);
            } else {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            }
            if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
                player.updateInventory();
            }
            String priceString = plugin.getCurrencySign() + Double.toString(price);
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.success").replace("%creature%", creatureName).replace("%price%", priceString)));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.sell")));
        }
    }

    public boolean createOrUpdateShop(String[] lines, Sign sign, Player player) {
        // Mac sends weird \uF700 and \uF701 chars
        boolean existingShop = isShop(sign);
        SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(lines[1].trim().replaceAll("\uF700", "").replaceAll("\uF701", ""));
        if (SilkspawnersShopMode.isValidMode(mode)) {
            String mob = lines[2].toLowerCase().trim().replaceAll("\uF700", "").replaceAll("\uF701", "");
            if (su.isKnown(mob)) {
                String priceString = lines[3];
                try {
                    double price = Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
                    // Update existing shop or create a new one
                    SilkSpawnersShop shop;
                    if (existingShop) {
                        shop = getShop(sign);
                        shop.setMob(mob);
                        shop.setMode(mode);
                        shop.setPrice(price);
                        if (updateShop(shop)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.success")));
                            return true;
                        }
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.error")));
                        removeShop(shop);
                    }
                    shop = new SilkSpawnersShop(sign, mode, mob, price);
                    if (addShop(shop)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.success")));
                        return true;
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.error")));
                } catch (NullPointerException | NumberFormatException e) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidPrice")));
                    removeShop(sign);
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMob")));
                removeShop(sign);
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMode")));
            removeShop(sign);
        }
        return false;
    }
}
