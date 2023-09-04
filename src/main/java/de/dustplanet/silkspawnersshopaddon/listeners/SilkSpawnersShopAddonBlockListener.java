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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * SilkSpawnerShopAddon block listener that handles sign changes and destructions.
 *
 * @author timbru31
 */
@SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY" })
public class SilkSpawnersShopAddonBlockListener implements Listener {
    private final SilkSpawnersShopAddon plugin;
    private final SilkSpawnersShopManager shopManager;
    private final SignHelper signHelper = new SignHelper();

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SilkSpawnersShopAddonBlockListener(final SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block brokenBlock = event.getBlock();
        if (!checkBlockFaces(brokenBlock, event, signHelper.getAllSigns()) && !signHelper.getAllSigns().contains(brokenBlock.getType())) {
            for (final BlockFace face : plugin.getBlockFaces()) {
                final Block attachedBlock = brokenBlock.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, signHelper.getStandingSigns())) {
                    break;
                }
            }
            final Block attachedBlock = brokenBlock.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, signHelper.getStandingSigns());
        }
    }

    @EventHandler
    @SuppressFBWarnings({ "CLI_CONSTANT_LIST_INDEX", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:SeparatorWrap", "checkstyle:AvoidEscapedUnicodeCharacters" })
    public void onSignChange(final SignChangeEvent event) {
        final String[] lines = event.getLines();
        final String shopIdentifier = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("shopIdentifier", ""));
        final String cleanShopIdentifier = ChatColor.stripColor(shopIdentifier);
        final String shopIdentifierLine = ChatColor.translateAlternateColorCodes('&',
                // macOS sends weird \uF700 and \uF701 chars
                lines[0].trim().replaceAll("\uF700", "").replaceAll("\uF701", ""));
        final String cleanShopIdentifierLine = ChatColor.stripColor(shopIdentifierLine);
        if (!cleanShopIdentifierLine.equalsIgnoreCase(cleanShopIdentifier)) {
            return;
        }
        final Player player = event.getPlayer();
        if (player.hasPermission("silkspawners.createshop")) {
            final Sign sign = (Sign) event.getBlock().getState();
            if (shopManager.createOrUpdateShop(lines, sign, player)) {
                event.setLine(0, shopIdentifier);
                final int lasteLine = 3;
                event.setLine(lasteLine, plugin.getFormattedPrice(lines[lasteLine].replaceAll("[^0-9.]", "")));
            } else {
                event.setLine(0, ChatColor.RED + ChatColor.stripColor(shopIdentifier));
            }
        } else {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.building", "")));
            event.setLine(0, ChatColor.RED + ChatColor.stripColor(shopIdentifier));
        }

    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private boolean checkBlockFaces(final Block block, final BlockBreakEvent event, final Collection<Material> signMaterials) {
        if (!signMaterials.stream().anyMatch(block.getType()::equals)) {
            return false;
        }

        final Sign sign = (Sign) block.getState();
        if (!shopManager.isShop(sign)) {
            return false;
        }

        final Player player = event.getPlayer();
        if (player.hasPermission("silkspawners.destroyshop")) {
            if (shopManager.removeShop(shopManager.getShop(sign))) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("removing.success", "")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("removing.error", "")));
                event.setCancelled(true);
            }
        } else {
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', plugin.getLocalization().getString("noPermission.destroying", "")));
            event.setCancelled(true);
        }
        return true;
    }
}
