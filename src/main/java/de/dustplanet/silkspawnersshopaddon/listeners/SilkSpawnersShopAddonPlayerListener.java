package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The player listener class to handle shop interactions of a player.
 *
 * @author timbru31
 */
public class SilkSpawnersShopAddonPlayerListener implements Listener {
    private final SilkSpawnersShopAddon plugin;
    private final SilkSpawnersShopManager shopManager;
    private final SignHelper signHelper = new SignHelper();

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SilkSpawnersShopAddonPlayerListener(final SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:ReturnCount" })
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "False positive")
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        if (!signHelper.getAllSigns().contains(event.getClickedBlock().getType())) {
            return;
        }
        if (!plugin.getAllowedActions().contains(event.getAction())) {
            return;
        }
        final Sign sign = (Sign) event.getClickedBlock().getState();
        if (shopManager.isShop(sign)) {
            if (plugin.isEggMode()) {
                event.setCancelled(true);
            }
            shopManager.handleShopInteraction(event.getPlayer(), sign, event.hasItem(), event.getItem());
        }

    }
}
