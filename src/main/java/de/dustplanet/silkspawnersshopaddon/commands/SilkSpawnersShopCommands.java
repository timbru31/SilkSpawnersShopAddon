package de.dustplanet.silkspawnersshopaddon.commands;

import java.util.ArrayList;
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
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;

public class SilkSpawnersShopCommands implements CommandExecutor {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

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
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("check")) {
            if (sender.hasPermission("silkspawners.updateshops")) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<SilkSpawnersShop> shopList = getShopManager().getAllShops();
                        ArrayList<SilkSpawnersShop> invalidShops = new ArrayList<>();
                        String invalidMessage = ChatColor.translateAlternateColorCodes('\u0026', getPlugin().getLocalization().getString("checking.invalid"));
                        for (SilkSpawnersShop shop : shopList) {
                            Location shopLoc = shop.getLocation();
                            if (shopLoc.getBlock().getType() == Material.WALL_SIGN || shopLoc.getBlock().getType() == Material.SIGN_POST) {
                                continue;
                            }
                            sender.sendMessage(invalidMessage.replace("%world%", shopLoc.getWorld().getName())
                                    .replace("%x%", Double.toString(shopLoc.getX()))
                                    .replace("%y%", Double.toString(shopLoc.getY()))
                                    .replace("%z%", Double.toString(shopLoc.getZ())));
                            invalidShops.add(shop);
                        }
                        if (shopList.size() > 0 && !getShopManager().removeShops(invalidShops)) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', getPlugin().getLocalization().getString("checking.error")));
                        }
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', getPlugin().getLocalization().getString("checking.success")).replace("%size%", Integer.toString(invalidShops.size())));
                    }
                });
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.check")));
            }
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("silkspawners.editshop")) {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.commandUsage")));
                    return true;
                }
                Block block = player.getTargetBlock((Set<Material>) null, 6);
                if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                    Sign sign = (Sign) block.getState();
                    if (shopManager.isShop(sign)) {
                        boolean change = true;
                        SilkSpawnersShop shop = shopManager.getShop(sign);
                        String argument = args[1];
                        switch (args[0].toUpperCase(Locale.ENGLISH)) {
                        case "MODE":
                            SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(argument);
                            if (mode == null) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMode")));
                                change = false;
                            } else {
                                shop.setMode(mode);
                            }
                            break;
                        case "MOB":
                            boolean knownMob = plugin.getSilkUtil().isKnown(argument);
                            if (!knownMob) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMob")));
                                change = false;
                            } else {
                                shop.setMob(argument);
                            }
                            break;
                        case "PRICE":
                            try {
                                double price = Double.parseDouble(argument.replaceAll("[^0-9.]", ""));
                                shop.setPrice(price);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidPrice")));
                                change = false;
                            }
                            break;
                        case "AMOUNT":
                            try {
                                int amount = Integer.parseInt(argument.replaceAll("[^0-9.]", ""));
                                shop.setAmount(amount);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidAmount")));
                                change = false;
                            }
                            break;
                        default:
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.commandUsage")));
                            change = false;
                            break;
                        }
                        if (change) {
                            if (shopManager.updateShop(shop)) {
                                sign.setLine(0, ChatColor.translateAlternateColorCodes('\u0026', plugin.getConfig().getString("shopIdentifier")));
                                sign.setLine(1, shop.getMode().toString() + ":" + shop.getAmount());
                                sign.setLine(2, shop.getMob());
                                sign.setLine(3, plugin.getFormattedPrice(shop.getPrice()));
                                sign.update(true);
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.success")));
                            } else {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.error")));
                            }
                        }
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.noShop")));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.noShop")));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.edit")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.noConsole")));
        }
        return true;
    }
}
