package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

    private boolean checkBlockFaces(Block block, BlockBreakEvent event, Material[] signMaterials) {
        boolean match = false;
        for (Material m : signMaterials) {
            if (block.getType() == m) {
                match = true;
                break;
            }
        }
        if (match) {
            Sign sign = (Sign) block.getState();
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
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check block itself
        Block brokenBlock = event.getBlock();
        if (!checkBlockFaces(brokenBlock, event, new Material[] {Material.WALL_SIGN, Material.SIGN_POST})) {
            // Check attached blocks if the block broken block wasn't a sign
            if (brokenBlock.getType() != Material.WALL_SIGN && brokenBlock.getType() != Material.SIGN_POST) {
                for (BlockFace face : plugin.getBlockFaces()) {
                    Block attachedBlock = brokenBlock.getRelative(face);
                    if (checkBlockFaces(attachedBlock, event, new Material[] {Material.WALL_SIGN})) {
                        break;
                    }
                }
                // Check block up (sign post)
                Block attachedBlock = brokenBlock.getRelative(BlockFace.UP);
                checkBlockFaces(attachedBlock, event, new Material[] {Material.SIGN_POST});
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // Mac sends weird \uF700 and \uF701 chars
        String[] lines = event.getLines();
        if (ChatColor.stripColor(lines[0].trim().replaceAll("\uF700", "").replaceAll("\uF701", "")).equalsIgnoreCase("[SilkSpawners]")) {
            Player player = event.getPlayer();
            if (player.hasPermission("silkspawners.createshop")) {
                Sign sign = (Sign) event.getBlock().getState();
                if (shopManager.createOrUpdateShop(lines, sign, player)) {
                    event.setLine(0, ChatColor.BLUE + "[SilkSpawners]");
                    // Strip everything else than numbers
                    event.setLine(3, plugin.getFormattedPrice(lines[3].replaceAll("[^0-9.]", "")));
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
