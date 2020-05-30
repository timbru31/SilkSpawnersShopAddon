package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.exception.InvalidAmountException;
import de.dustplanet.silkspawnersshopaddon.storage.ISilkSpawnersShopAddonStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonMongoStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonMySQLStorage;
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonYAMLStorage;
import de.dustplanet.util.SilkUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class SilkSpawnersShopManager {
    private SilkUtil su;
    private SilkSpawnersShopAddon plugin;
    @Getter
    private final ISilkSpawnersShopAddonStorage storage;

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public SilkSpawnersShopManager(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
        this.su = plugin.getSilkUtil();
        String storageMethod = plugin.getConfig().getString("storageMethod", "YAML").toUpperCase(Locale.ENGLISH);
        switch (storageMethod) {
            case "MONGODB":
            case "MONGO":
                storage = new SilkSpawnersShopAddonMongoStorage(plugin);
                break;
            case "MYSQL":
                storage = new SilkSpawnersShopAddonMySQLStorage(plugin);
                break;
            case "YML":
            case "YAML":
            default:
                storage = new SilkSpawnersShopAddonYAMLStorage(plugin);
                break;
        }

        storage.upgradeDatabase();
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

    public boolean removeShops(List<SilkSpawnersShop> shopList) {
        return storage.removeShops(shopList);
    }

    private boolean addShop(SilkSpawnersShop shop) {
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

    public List<SilkSpawnersShop> getAllShops() {
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
                plugin.getServer().getLogger()
                        .warning("Detected invalid mode, removing shop @" + shop.getLocation().toString().replaceAll("[\r\n]", ""));
                removeShop(shop);
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void handleBuy(Player player, SilkSpawnersShop shop) {
        String mob = shop.getMob();
        String entityID = su.getDisplayNameToMobID().get(mob);
        String mobName = su.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");
        if (!plugin.isPerMobPermissions() && player.hasPermission("silkspawners.use.buy")
                || player.hasPermission("silkspawners.use.buy." + mobName)) {
            double price = shop.getPrice();
            if (!plugin.getEcon().has(player, price)) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("buying.notEnoughMoney", "")));
                return;
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("buying.inventoryFull", "")));
                return;
            }
            plugin.getEcon().withdrawPlayer(player, price);
            mob = su.getCreatureName(entityID);
            int amount = shop.getAmount();
            if (plugin.isEggMode()) {
                player.getInventory().addItem(su.newEggItem(entityID, amount, su.getCreatureEggName(entityID)));
            } else {
                player.getInventory().addItem(su.newSpawnerItem(entityID, su.getCustomSpawnerName(entityID), amount, false));
            }
            if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
                player.updateInventory();
            }
            String priceString = plugin.getFormattedPrice(price);
            if (!plugin.isEggMode()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("buying.successSpawner", "").replace("%creature%", mob)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("buying.successEgg", "").replace("%creature%", mob)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
            }
        } else {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.buy", "")));
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressFBWarnings({ "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    public void handleSell(Player player, SilkSpawnersShop shop, boolean hasItem, ItemStack item) {
        String mob = shop.getMob();
        String entityID = su.getDisplayNameToMobID().get(mob);
        String mobName = su.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");
        if (!plugin.isPerMobPermissions() && player.hasPermission("silkspawners.use.sell")
                || player.hasPermission("silkspawners.use.sell." + mobName)) {
            double price = shop.getPrice();
            if (!hasItem) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.noItemInHand", "")));
                return;
            } else if (!plugin.isEggMode() && item.getType() != Material.SPAWNER) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("selling.noSpawnerInHand", "")));
                return;
            } else if (plugin.isEggMode() && !su.nmsProvider.getSpawnEggMaterials().contains(item.getType())) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.noEggInHand", "")));
                return;
            }
            String entityIDInHand = null;
            if (plugin.isEggMode()) {
                entityIDInHand = su.getStoredEggEntityID(item);
            } else {
                entityIDInHand = su.getStoredSpawnerItemEntityID(item);
            }
            String creatureName = su.getCreatureName(entityID);
            if (!entityIDInHand.equalsIgnoreCase(entityID)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("selling.notTheSameMob", "").replace("%creature%", creatureName)));
                return;
            }
            ItemStack itemInHand = null;
            try {
                itemInHand = player.getInventory().getItemInMainHand();
            } catch (@SuppressWarnings("unused") NoSuchMethodError e) {
                // 1.8 has getItemInHand
                itemInHand = player.getItemInHand();
            }
            if (itemInHand == null) {
                plugin.getLogger().severe("Unable to get item in hand, please report this. Aborting transaction");
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("selling.error", "")));
                return;
            }
            int inHandAmount = itemInHand.getAmount();
            if (inHandAmount < shop.getAmount()) {
                if (!plugin.isEggMode()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.getLocalization().getString("selling.notEnoughSpawners", "")));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.getLocalization().getString("selling.notEnoughEggs", "")));
                }
                return;
            }
            plugin.getEcon().depositPlayer(player, price);
            if (inHandAmount - shop.getAmount() == 0) {
                try {
                    player.getInventory().setItemInMainHand(null);
                } catch (@SuppressWarnings("unused") NoSuchMethodError e) {
                    // 1.8 has setItemInHand
                    player.setItemInHand(null);
                }
            } else {
                itemInHand.setAmount(itemInHand.getAmount() - shop.getAmount());
            }
            if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
                player.updateInventory();
            }
            String priceString = plugin.getFormattedPrice(price);
            if (!plugin.isEggMode()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("selling.successSpawner", "").replace("%creature%", creatureName)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                        plugin.getLocalization().getString("selling.successEgg", "").replace("%creature%", creatureName)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
            }
        } else {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.sell", "")));
        }
    }

    @SuppressFBWarnings({ "CLI_CONSTANT_LIST_INDEX", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    public boolean createOrUpdateShop(String[] lines, Sign sign, Player player) {
        // macOS sends weird \uF700 and \uF701 chars
        boolean existingShop = isShop(sign);
        String secondLine = lines[1].trim().replaceAll("\uF700", "").replaceAll("\uF701", "");
        SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(secondLine);
        if (SilkspawnersShopMode.isValidMode(mode)) {
            String mob = lines[2].toLowerCase(Locale.ENGLISH).trim().replaceAll("\uF700", "").replaceAll("\uF701", "");
            if (su.isKnown(mob)) {
                String priceString = lines[3];
                try {
                    double price = Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
                    int amount = 1;
                    if (secondLine.contains(":")) {
                        try {
                            amount = Integer.parseInt(secondLine.split(":")[1].replaceAll("[^0-9]", ""));
                            if (amount < 1) {
                                throw new InvalidAmountException("Amount must be greater or equal to 1, but got " + amount);
                            }
                        } catch (@SuppressWarnings("unused") IndexOutOfBoundsException | InvalidAmountException | NumberFormatException e) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                    plugin.getLocalization().getString("creating.invalidAmount", "")));
                            removeShop(sign);
                            return false;
                        }
                    }
                    sign.setLine(3, plugin.getFormattedPrice(price));
                    sign.update(true);
                    // Update existing shop or create a new one
                    SilkSpawnersShop shop;
                    if (existingShop) {
                        shop = getShop(sign);
                        shop.setMob(mob);
                        shop.setMode(mode);
                        shop.setPrice(price);
                        if (updateShop(shop)) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                    plugin.getLocalization().getString("updating.success", "")));
                            return true;
                        }
                        player.sendMessage(
                                ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.error", "")));
                        removeShop(shop);
                    }
                    shop = new SilkSpawnersShop(sign, mode, mob, amount, price);
                    if (addShop(shop)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.getLocalization().getString("creating.success", "")));
                        return true;
                    }
                    player.sendMessage(
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.error", "")));
                } catch (@SuppressWarnings("unused") NullPointerException | NumberFormatException e) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.getLocalization().getString("creating.invalidPrice", "")));
                    removeShop(sign);
                }
            } else {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMob", "")));
                removeShop(sign);
            }
        } else {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMode", "")));
            removeShop(sign);
        }
        return false;
    }
}
