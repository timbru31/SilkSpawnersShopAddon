package de.dustplanet.silkspawnersshopaddon.commands;

import java.util.Set;

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
                    // message usage
                    System.out.println("usage");
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
                                System.out.println("invalid mode");
                                // message invalid mode
                            } else {
                                shop.setMode(mode);
                                System.out.println("new mode: " + mode.name());
                                // message success
                            }
                            break;
                        case "MOB":
                            boolean knownMob = plugin.getSilkUtil().isKnown(argument);
                            if (!knownMob) {
                                System.out.println("invalid mob");
                                // message invalid mob
                            } else {
                                System.out.println("new mob: " + argument);
                                shop.setMob(argument);
                                // message success
                            }
                            break;
                        case "PRICE":
                            try {
                                double price = Double.parseDouble(argument.replaceAll("[^0-9.]", ""));
                                shop.setPrice(price);
                                System.out.println("new price "+ price);
                                // message success
                            } catch(NumberFormatException e) {
                                // message invalid price
                                System.out.println("invalid price");
                            }
                            break;
                        default:
                            // message the usage, unkown option
                            System.out.println("unknown option");
                            change = false;
                            break;
                        }
                        if (change) {
                            if (shopManager.updateShop(shop)) {
                                // success update
                                sign.setLine(1, shop.getMode().toString());
                                sign.setLine(2, shop.getMob());
                                sign.setLine(3, plugin.getCurrencySign() + shop.getPrice());
                                sign.update(true);
                            } else {
                                // error update
                            }
                        }
                    } else {
                        System.out.println("not a shop");
                        // no shop
                    }
                } else {
                    System.out.println("no block in sight");
                    // no block in sight
                }
            } else {
                System.out.println("no perms");
                // message no perms
            }
        } else {
            System.out.println("no console pls");
            // message no console
        }
        return true;
    }
}
