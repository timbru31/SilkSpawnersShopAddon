package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SilkSpawnersShopAddonPlayerListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;
    private SignHelper signHelper = new SignHelper();

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    public SilkSpawnersShopAddonPlayerListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "False positive")
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        if (signHelper.getAllSignMaterials().contains(event.getClickedBlock().getType())) {
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
