package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;

public class SilkSpawnersShopAddonEntityListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

    public SilkSpawnersShopAddonEntityListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                if (shopManager.isShop(sign)) {
                    shopManager.removeShop(sign);
                }
            }
        }
        // for (Sign s : signs) {
        // event.blockList().remove(s.getBlock());
        // org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign)
        // s.getData();
        // event.blockList().remove(s.getBlock().getRelative(materialSign.getAttachedFace()));
        // }
    }
}
