package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;

public class SilkSpawnersShopAddonProtectionListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

    public SilkSpawnersShopAddonProtectionListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                if (plugin.getConfig().getBoolean("invincibility.ignite", true)) {
                    event.setCancelled(true);
                } else {
                    shopManager.removeShop(sign);
                }
            }
        }
    }


    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                if (plugin.getConfig().getBoolean("invincibility.burn", true)) {
                    event.setCancelled(true);
                } else {
                    shopManager.removeShop(sign);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                if (shopManager.isShop(sign)) {
                    if (plugin.getConfig().getBoolean("invincibility.explode", true)) {
                        event.setCancelled(true);
                    } else {
                        shopManager.removeShop(sign);
                    }
                }
            }
        }
    }
}
