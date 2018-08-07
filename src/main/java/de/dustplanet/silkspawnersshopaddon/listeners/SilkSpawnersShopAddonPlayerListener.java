package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;

public class SilkSpawnersShopAddonPlayerListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

    public SilkSpawnersShopAddonPlayerListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN) {
            if (!plugin.getAllowedActions().contains(event.getAction())) {
                return;
            }
            Sign sign = (Sign) event.getClickedBlock().getState();
            if (shopManager.isShop(sign)) {
                if (plugin.isEggMode()) {
                    event.setCancelled(true);
                }
                shopManager.handleShopInteraction(event.getPlayer(), sign, event.hasItem(), event.getItem());
            }
        }
    }
}
