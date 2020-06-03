package de.dustplanet.silkspawnersshopaddon.shop;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

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
import de.dustplanet.silkspawnersshopaddon.storage.SilkSpawnersShopAddonYamlStorage;
import de.dustplanet.util.SilkUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * The ShopManager which has the knowledge of the different storage classes. It also handles the buy and sell operations and the creation
 * and update of shops.
 *
 * @author timbru31
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "PMD.TooManyMethods", "PMD.GodClass" })
public class SilkSpawnersShopManager {
    private final SilkUtil silkUtil;
    private final SilkSpawnersShopAddon plugin;
    @Getter
    private final ISilkSpawnersShopAddonStorage storage;

    /**
     * ShopManager that instantiates the correct storage class.
     *
     * @param plugin the plugin
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public SilkSpawnersShopManager(final SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
        this.silkUtil = plugin.getSilkUtil();
        final String storageMethod = plugin.getConfig().getString("storageMethod", "YAML").toUpperCase(Locale.ENGLISH);
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
                storage = new SilkSpawnersShopAddonYamlStorage(plugin);
                break;
        }

        storage.upgradeDatabase();
    }

    /**
     * Removes a shop from the storage class.
     *
     * @param shop the shop to remove
     * @return the result, true if the operation was a success, false otherwise
     */
    public boolean removeShop(final SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.removeShop(shop);
    }

    /**
     * Removes a shop from a sign from the storage class.
     *
     * @param sign the sign as the shop to remove
     * @return the result, true if the operation was a success, false otherwise
     */
    public boolean removeShop(final Sign sign) {
        if (sign == null || !isShop(sign)) {
            return false;
        }
        return removeShop(getShop(sign));
    }

    /**
     * Removes a list of shops from the storage class.
     *
     * @param shopList a list of shops to remove
     * @return the result, true if the operation was a success, false otherwise
     */
    public boolean removeShops(final List<SilkSpawnersShop> shopList) {
        return storage.removeShops(shopList);
    }

    private boolean addShop(final SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.addShop(shop);
    }

    /**
     * Updates and saves a given shop.
     *
     * @param shop the shop to update and save
     * @return the operation result
     */
    public boolean updateShop(final SilkSpawnersShop shop) {
        if (shop == null) {
            return false;
        }
        return storage.updateShop(shop);
    }

    /**
     * Checks whether a given sign is a shop.
     *
     * @param shop the sign to check
     * @return the operation result
     */
    public boolean isShop(final Sign sign) {
        if (sign == null) {
            return false;
        }
        return storage.isShop(sign);
    }

    /**
     * Attempts to return a shop from a sign.
     *
     * @param sign the sign to check
     * @return the shop, null otherwise
     */
    @Nullable
    public SilkSpawnersShop getShop(final Sign sign) {
        return storage.getShop(sign);
    }

    /**
     * Method to return all known shops.
     *
     * @return the shop list
     */
    @Nullable
    public List<SilkSpawnersShop> getAllShops() {
        return storage.getAllShops();
    }

    /**
     * Handles the abstract interaction of a player with a given shop.
     *
     * @param player the player that interacts with the shop
     * @param shop the shop that's interacted with
     */
    @SuppressWarnings("checkstyle:SeparatorWrap")
    public void handleShopInteraction(final Player player, final Sign sign, final boolean hasItem, final ItemStack item) {
        final SilkSpawnersShop shop = getShop(sign);
        final SilkspawnersShopMode mode = shop.getMode();
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

    /**
     * Handles the buy intent of a player with a given shop.
     *
     * @param player the player that interacts with the shop
     * @param shop the shop that's interacted with
     */
    @SuppressWarnings({ "deprecation", "PMD.AvoidDuplicateLiterals", "checkstyle:SeparatorWrap" })
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void handleBuy(final Player player, final SilkSpawnersShop shop) {
        String mob = shop.getMob();
        final String entityID = silkUtil.getDisplayNameToMobID().get(mob);
        final String mobName = silkUtil.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");
        if (!plugin.isPerMobPermissions() && player.hasPermission("silkspawners.use.buy")
                || player.hasPermission("silkspawners.use.buy." + mobName)) {
            final double price = shop.getPrice();
            if (!plugin.getEcon().has(player, price)) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("buying.notEnoughMoney", "")));
                return;
            }
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("buying.inventoryFull", "")));
                return;
            }
            plugin.getEcon().withdrawPlayer(player, price);
            mob = silkUtil.getCreatureName(entityID);
            final int amount = shop.getAmount();
            if (plugin.isEggMode()) {
                player.getInventory().addItem(silkUtil.newEggItem(entityID, amount, silkUtil.getCreatureEggName(entityID)));
            } else {
                player.getInventory().addItem(silkUtil.newSpawnerItem(entityID, silkUtil.getCustomSpawnerName(entityID), amount, false));
            }
            if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
                player.updateInventory();
            }
            final String priceString = plugin.getFormattedPrice(price);
            if (plugin.isEggMode()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getLocalization().getString("buying.successEgg", "").replace("%creature%", mob)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getLocalization().getString("buying.successSpawner", "").replace("%creature%", mob)
                                .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));

            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.buy", "")));
        }
    }

    /**
     * Handles the sell intent of a player with a given shop.
     *
     * @param player the player that interacts with the shop
     * @param shop the shop that's interacted with
     */
    @SuppressWarnings({ "deprecation", "PMD.AvoidDuplicateLiterals", "PMD.DataflowAnomalyAnalysis", "checkstyle:ReturnCount",
            "PMD.CyclomaticComplexity" })
    @SuppressFBWarnings({ "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    public void handleSell(final Player player, final SilkSpawnersShop shop, final boolean hasItem, final ItemStack item) {
        final String mob = shop.getMob();
        final String entityID = silkUtil.getDisplayNameToMobID().get(mob);
        final String mobName = silkUtil.getCreatureName(entityID).toLowerCase(Locale.ENGLISH).replace(" ", "");

        if (plugin.isPerMobPermissions() && !player.hasPermission("silkspawners.use.sell." + mobName)
                || !player.hasPermission("silkspawners.use.sell")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.sell", "")));
            return;
        }

        if (checkSellAbortingCriteria(player, hasItem, item)) {
            return;
        }

        final String entityIDInHand = getEntityIDInHand(item);
        final String creatureName = silkUtil.getCreatureName(entityID);
        if (!entityIDInHand.equalsIgnoreCase(entityID)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getLocalization().getString("selling.notTheSameMob", "").replace("%creature%", creatureName)));
            return;
        }
        final ItemStack itemInHand = getItemInHand(player);
        if (itemInHand == null) {
            plugin.getLogger().severe("Unable to get item in hand, please report this. Aborting transaction");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.error", "")));
            return;
        }
        final int inHandAmount = itemInHand.getAmount();
        if (inHandAmount < shop.getAmount()) {
            if (plugin.isEggMode()) {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.notEnoughEggs", "")));
            } else {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.notEnoughSpawners", "")));
            }
            return;
        }
        final double price = shop.getPrice();
        plugin.getEcon().depositPlayer(player, price);
        setItemInHand(player, shop, itemInHand, inHandAmount);
        if (plugin.getConfig().getBoolean("forceInventoryUpdate", false)) {
            player.updateInventory();
        }
        sendConfirmationMessage(player, shop, creatureName, price);

    }

    @SuppressWarnings("checkstyle:SeparatorWrap")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void sendConfirmationMessage(final Player player, final SilkSpawnersShop shop, final String creatureName, final double price) {
        final String priceString = plugin.getFormattedPrice(price);
        if (plugin.isEggMode()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getLocalization().getString("selling.successEgg", "").replace("%creature%", creatureName)
                            .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getLocalization().getString("selling.successSpawner", "").replace("%creature%", creatureName)
                            .replace("%price%", priceString).replace("%amount%", Integer.toString(shop.getAmount()))));
        }
    }

    @SuppressWarnings({ "static-method", "deprecation", "PMD.AvoidDuplicateLiterals" })
    private void setItemInHand(final Player player, final ISilkSpawnersShop shop, final ItemStack itemInHand, final int inHandAmount) {
        if (inHandAmount - shop.getAmount() == 0) {
            try {
                player.getInventory().setItemInMainHand(null);
            } catch (@SuppressWarnings("unused") final NoSuchMethodError e) {
                // 1.8 has setItemInHand
                player.setItemInHand(null);
            }
        } else {
            itemInHand.setAmount(itemInHand.getAmount() - shop.getAmount());
        }
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private String getEntityIDInHand(final ItemStack item) {
        String entityIDInHand = null;
        if (plugin.isEggMode()) {
            entityIDInHand = silkUtil.getStoredEggEntityID(item);
        } else {
            entityIDInHand = silkUtil.getStoredSpawnerItemEntityID(item);
        }
        return entityIDInHand;
    }

    @SuppressWarnings({ "static-method", "deprecation", "PMD.DataflowAnomalyAnalysis" })
    private ItemStack getItemInHand(final Player player) {
        ItemStack itemInHand = null;
        try {
            itemInHand = player.getInventory().getItemInMainHand();
        } catch (@SuppressWarnings("unused") final NoSuchMethodError e) {
            // 1.8 has getItemInHand
            itemInHand = player.getItemInHand();
        }
        return itemInHand;
    }

    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "PMD.ConfusingTernary" })
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private boolean checkSellAbortingCriteria(final Player player, final boolean hasItem, final ItemStack item) {
        boolean abort = false;
        if (!hasItem) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.noItemInHand", "")));
            abort = true;
        } else if (!plugin.isEggMode() && item.getType() != Material.SPAWNER) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.noSpawnerInHand", "")));
            abort = true;
        } else if (plugin.isEggMode() && !silkUtil.nmsProvider.getSpawnEggMaterials().contains(item.getType())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("selling.noEggInHand", "")));
            abort = true;
        }
        return abort;
    }

    /**
     * Handles the update or create intent of a player with a given sign.
     *
     * @param lines the lines of the sign to create a shop from
     * @param sign the sign that's interacted with
     * @param player the player that interacts with the sign
     */
    @SuppressFBWarnings({ "CLI_CONSTANT_LIST_INDEX", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    @SuppressWarnings({ "checkstyle:AvoidEscapedUnicodeCharacters", "checkstyle:ReturnCount", "PMD.DataflowAnomalyAnalysis",
            "PMD.AvoidCatchingGenericException", "PMD.AvoidLiteralsInIfCondition", "PMD.AvoidCatchingNPE" })
    public boolean createOrUpdateShop(final String[] lines, final Sign sign, final Player player) {
        // macOS sends weird \uF700 and \uF701 chars
        final String secondLine = lines[1].trim().replaceAll("\uF700", "").replaceAll("\uF701", "");
        final SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(secondLine);
        if (!SilkspawnersShopMode.isValidMode(mode)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidMode", "")));
            removeShop(sign);
            return false;
        }
        final String mob = lines[2].toLowerCase(Locale.ENGLISH).trim().replaceAll("\uF700", "").replaceAll("\uF701", "");
        if (!silkUtil.isKnown(mob)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidMob", "")));
            removeShop(sign);
            return false;
        }

        final int lastLine = 3;
        final String priceString = lines[lastLine];
        try {
            int amount = 1;
            if (secondLine.contains(":")) {
                try {
                    amount = Integer.parseInt(secondLine.split(":")[1].replaceAll("[^0-9]", ""));
                    if (amount < 1) {
                        throw new InvalidAmountException("Amount must be greater or equal to 1, but got " + amount);
                    }
                } catch (@SuppressWarnings("unused") IndexOutOfBoundsException | InvalidAmountException | NumberFormatException e) {
                    player.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidAmount", "")));
                    removeShop(sign);
                    return false;
                }
            }
            final double price = Double.parseDouble(priceString.replaceAll("[^0-9.]", ""));
            sign.setLine(lastLine, plugin.getFormattedPrice(price));
            sign.update(true);
            if (updateOrCreateShop(player, sign, mode, mob, price, amount)) {
                return true;
            }
        } catch (@SuppressWarnings("unused") NullPointerException | NumberFormatException e) {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.invalidPrice", "")));
            removeShop(sign);
        }

        return false;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private boolean updateOrCreateShop(final Player player, final Sign sign, final SilkspawnersShopMode mode, final String mob,
            final double price, final int amount) {
        final boolean existingShop = isShop(sign);

        SilkSpawnersShop shop;
        if (existingShop) {
            shop = getShop(sign);
            shop.setMob(mob);
            shop.setMode(mode);
            shop.setPrice(price);
            if (updateShop(shop)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.success", "")));
                return true;
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("updating.error", "")));
            removeShop(shop);
        }
        shop = new SilkSpawnersShop(sign, mode, mob, amount, price);
        if (addShop(shop)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.success", "")));
            return true;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("creating.error", "")));
        return false;
    }
}
