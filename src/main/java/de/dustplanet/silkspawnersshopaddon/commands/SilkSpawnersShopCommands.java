package de.dustplanet.silkspawnersshopaddon.commands;

import java.util.Set;

import org.bukkit.ChatColor;
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

    public SilkSpawnersShopCommands(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
                        switch (args[0].toUpperCase()) {
                        case "MODE":
                            SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(argument);
                            if (mode == null) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMode")));
                            } else {
                                shop.setMode(mode);
                            }
                            break;
                        case "MOB":
                            boolean knownMob = plugin.getSilkUtil().isKnown(argument);
                            if (!knownMob) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("creating.invalidMob")));
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
                            }
                            break;
                        default:
                            player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("updating.commandUsage")));
                            change = false;
                            break;
                        }
                        if (change) {
                            if (shopManager.updateShop(shop)) {
                                sign.setLine(1, shop.getMode().toString());
                                sign.setLine(2, shop.getMob());
                                sign.setLine(3, plugin.getCurrencySign() + shop.getPrice());
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
