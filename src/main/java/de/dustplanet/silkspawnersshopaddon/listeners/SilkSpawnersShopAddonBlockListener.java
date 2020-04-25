package de.dustplanet.silkspawnersshopaddon.listeners;

import java.util.Collection;

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
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;

public class SilkSpawnersShopAddonBlockListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;
    private SignHelper signHelper = new SignHelper();

    public SilkSpawnersShopAddonBlockListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    private boolean checkBlockFaces(Block block, BlockBreakEvent event, Collection<Material> signMaterials) {
        if (signMaterials.stream().anyMatch(block.getType()::equals)) {
            Sign sign = (Sign) block.getState();
            Player player = event.getPlayer();
            if (shopManager.isShop(sign)) {
                if (player.hasPermission("silkspawners.destroyshop")) {
                    if (!shopManager.removeShop(shopManager.getShop(sign))) {
                        player.sendMessage(
                                ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("removing.error")));
                        event.setCancelled(true);
                    } else {
                        player.sendMessage(
                                ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("removing.success")));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('\u0026',
                            plugin.getLocalization().getString("noPermission.destroying")));
                    event.setCancelled(true);
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        if (!checkBlockFaces(brokenBlock, event, signHelper.getAllSignMaterials())
                && !signHelper.getAllSignMaterials().contains(brokenBlock.getType())) {
            for (BlockFace face : plugin.getBlockFaces()) {
                Block attachedBlock = brokenBlock.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, signHelper.getSignMaterials())) {
                    break;
                }
            }
            Block attachedBlock = brokenBlock.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, signHelper.getSignMaterials());
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // macOS sends weird \uF700 and \uF701 chars
        String[] lines = event.getLines();
        String shopIdentifier = ChatColor.translateAlternateColorCodes('\u0026', plugin.getConfig().getString("shopIdentifier"));
        if (ChatColor.stripColor(lines[0].trim().replaceAll("\uF700", "").replaceAll("\uF701", ""))
                .equalsIgnoreCase(ChatColor.stripColor(shopIdentifier))) {
            Player player = event.getPlayer();
            if (player.hasPermission("silkspawners.createshop")) {
                Sign sign = (Sign) event.getBlock().getState();
                if (shopManager.createOrUpdateShop(lines, sign, player)) {
                    event.setLine(0, shopIdentifier);
                    event.setLine(3, plugin.getFormattedPrice(lines[3].replaceAll("[^0-9.]", "")));
                } else {
                    event.setLine(0, ChatColor.RED + ChatColor.stripColor(shopIdentifier));
                }
            } else {
                player.sendMessage(
                        ChatColor.translateAlternateColorCodes('\u0026', plugin.getLocalization().getString("noPermission.building")));
                event.setLine(0, ChatColor.RED + ChatColor.stripColor(shopIdentifier));
            }
        }
    }
}
