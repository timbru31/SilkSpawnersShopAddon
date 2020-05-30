package de.dustplanet.silkspawnersshopaddon.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.exception.InvalidAmountException;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "PSC_PRESIZE_COLLECTIONS" })
public class SilkSpawnersShopCommands implements CommandExecutor {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;
    private SignHelper signHelper = new SignHelper();

    public SilkSpawnersShopAddon getPlugin() {
        return plugin;
    }

    public SilkSpawnersShopManager getShopManager() {
        return shopManager;
    }

    public SilkSpawnersShopCommands(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @Override
    @SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            "CLI_CONSTANT_LIST_INDEX" }, justification = "Default values are provided to prevent a NPE")
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && "check".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("silkspawners.updateshops")) {
                updateShops(sender);
            } else {
                sender.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.check", "")));
            }
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("silkspawners.editshop")) {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.getLocalization().getString("updating.commandUsage", "")));
                    return true;
                }
                Block block = player.getTargetBlock((Set<Material>) null, 6);
                if (signHelper.getAllSignMaterials().contains(block.getType())) {
                    Sign sign = (Sign) block.getState();
                    if (shopManager.isShop(sign)) {
                        boolean change = true;
                        SilkSpawnersShop shop = shopManager.getShop(sign);
                        String argument = args[1];
                        switch (args[0].toUpperCase(Locale.ENGLISH)) {
                            case "MODE":
                                SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(argument);
                                if (mode == null) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                            plugin.getLocalization().getString("creating.invalidMode", "")));
                                    change = false;
                                } else {
                                    shop.setMode(mode);
                                }
                                break;
                            case "MOB":
                                boolean knownMob = plugin.getSilkUtil().isKnown(argument);
                                if (!knownMob) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                            plugin.getLocalization().getString("creating.invalidMob", "")));
                                    change = false;
                                } else {
                                    shop.setMob(argument);
                                }
                                break;
                            case "PRICE":
                                try {
                                    double price = Double.parseDouble(argument.replaceAll("[^0-9.]", ""));
                                    shop.setPrice(price);
                                } catch (@SuppressWarnings("unused") NumberFormatException e) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                            plugin.getLocalization().getString("creating.invalidPrice", "")));
                                    change = false;
                                }
                                break;
                            case "AMOUNT":
                                try {
                                    int amount = Integer.parseInt(argument.replaceAll("[^0-9.]", ""));
                                    if (amount < 1) {
                                        throw new InvalidAmountException("Amount must be greater or equal to 1, but got " + amount);
                                    }
                                    shop.setAmount(amount);
                                } catch (@SuppressWarnings("unused") NumberFormatException | InvalidAmountException e) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                            plugin.getLocalization().getString("creating.invalidAmount", "")));
                                    change = false;
                                }
                                break;
                            default:
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                        plugin.getLocalization().getString("updating.commandUsage", "")));
                                change = false;
                                break;
                        }
                        if (change) {
                            if (shopManager.updateShop(shop)) {
                                sign.setLine(0, ChatColor.translateAlternateColorCodes('\u0026',
                                        plugin.getConfig().getString("shopIdentifier", "")));
                                if (shop.getAmount() > 1) {
                                    sign.setLine(1, shop.getMode().toString() + ":" + shop.getAmount());
                                } else {
                                    sign.setLine(1, shop.getMode().toString());
                                }
                                sign.setLine(2, shop.getMob());
                                sign.setLine(3, plugin.getFormattedPrice(shop.getPrice()));
                                sign.update(true);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                        plugin.getLocalization().getString("updating.success", "")));
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                        plugin.getLocalization().getString("updating.error", "")));
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                                plugin.getLocalization().getString("updating.noShop", "")));
                    }
                } else {
                    player.sendMessage(
                            ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.noShop", "")));
                }
            } else {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.edit", "")));
            }
        } else {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.noConsole", "")));
        }
        return true;
    }

    private void updateShops(final CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<SilkSpawnersShop> shopList = getShopManager().getAllShops();
            List<SilkSpawnersShop> invalidShops = new ArrayList<>();
            String invalidMessage = ChatColor.translateAlternateColorCodes('\u0026',
                    getPlugin().getLocalization().getString("checking.invalid", ""));
            for (SilkSpawnersShop shop : shopList) {
                Location shopLoc = shop.getLocation();
                if (signHelper.getAllSignMaterials().contains(shopLoc.getBlock().getType())) {
                    continue;
                }
                sender.sendMessage(
                        invalidMessage.replace("%world%", shopLoc.getWorld().getName()).replace("%x%", Double.toString(shopLoc.getX()))
                                .replace("%y%", Double.toString(shopLoc.getY())).replace("%z%", Double.toString(shopLoc.getZ())));
                invalidShops.add(shop);
            }
            if (shopList.size() > 0 && !getShopManager().removeShops(invalidShops)) {
                sender.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', getPlugin().getLocalization().getString("checking.error", "")));
            }
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes('\u0026', getPlugin().getLocalization().getString("checking.success", ""))
                            .replace("%size%", Integer.toString(invalidShops.size())));
        });
    }
}
