package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;

public class SilkSpawnersShopAddonBlockListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

    public SilkSpawnersShopAddonBlockListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        if (brokenBlock.getType() == Material.SIGN_POST || brokenBlock.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) brokenBlock.getState();
            Player player = event.getPlayer();
            if (shopManager.isShop(sign)) {
                if (player.hasPermission("silkspawners.destroyshop")) {
                    if (!shopManager.removeShop(shopManager.getShop(sign))) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("removing.error")));
                        event.setCancelled(true);
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("removing.success")));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.destroying")));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (lines[0].equalsIgnoreCase("[SilkSpawners]")) {
            Player player = event.getPlayer();
            if (player.hasPermission("silkspawners.createshop")) {
                Sign sign = (Sign) event.getBlock().getState();
                if (shopManager.createShop(lines, sign, player)) {
                    event.setLine(0, ChatColor.BLUE + "[SilkSpawners]");
                    event.setLine(3, plugin.getCurrencySign() + lines[3]);
                } else {
                    event.setLine(0, ChatColor.RED + "[SilkSpawners]");
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.building")));
                event.setLine(0, ChatColor.RED + "[SilkSpawners]");
            }
        }
    }
}
