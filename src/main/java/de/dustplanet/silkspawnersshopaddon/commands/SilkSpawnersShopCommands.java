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
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;

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
            if (player.hasPermission("")) {
                if (args.length != 2) {
                    // message usage
                    return true;
                }
                Block block = player.getTargetBlock((Set<Material>) null, 6);
                if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                    Sign sign = (Sign) block.getState();
                    if (shopManager.isShop(sign)) {

                    } else {
                        // no shop
                    }
                } else {
                    // no block in sight
                }
            } else {
                // message no perms
            }
        } else {
            // message no console
        }
        return true;
    }
}
