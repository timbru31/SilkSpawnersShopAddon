package de.dustplanet.silkspawnersshopaddon.listeners;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.util.SignHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * SilkSpawnerShopAddon protection listener that handles sign destruction attempt.s
 *
 * @author timbru31
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class SilkSpawnersShopAddonProtectionListener implements Listener {
    private final SilkSpawnersShopAddon plugin;
    private final SilkSpawnersShopManager shopManager;
    private final SignHelper signHelper = new SignHelper();

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidDuplicateLiterals" })
    public SilkSpawnersShopAddonProtectionListener(final SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    @EventHandler
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onBlockIgnite(final BlockIgniteEvent event) {
        final Block block = event.getBlock();
        if (!checkBlockFaces(block, event, "ignite", signHelper.getAllSigns())) {
            for (final BlockFace face : plugin.getBlockFaces()) {
                final Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "ignite", signHelper.getWallSigns())) {
                    break;
                }
            }
            final Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "ignite", signHelper.getStandingSigns());
        }
    }

    @EventHandler
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onBlockBurn(final BlockBurnEvent event) {
        final Block block = event.getBlock();
        if (!checkBlockFaces(block, event, "burn", signHelper.getAllSigns())) {
            for (final BlockFace face : plugin.getBlockFaces()) {
                final Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "burn", signHelper.getWallSigns())) {
                    break;
                }
            }
            final Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "burn", signHelper.getStandingSigns());
        }
    }

    @EventHandler
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void onEntityExplode(final EntityExplodeEvent event) {
        for (final Block block : event.blockList()) {
            if (!signHelper.getAllSigns().contains(block.getType())) {
                continue;
            }
            final Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                if (plugin.getConfig().getBoolean("invincibility.explode", true)) {
                    event.setCancelled(true);
                } else {
                    shopManager.removeShop(sign);
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "PMD.AvoidCatchingGenericException", "PMD.AvoidCatchingNPE" })
    public void onBlockPhysics(final BlockPhysicsEvent event) {
        final Block block = event.getBlock();
        if (!signHelper.getAllSigns().contains(block.getType())) {
            return;
        }
        final Sign sign = (Sign) block.getState();
        if (!shopManager.isShop(sign)) {
            return;
        }
        try {
            @SuppressWarnings({ "deprecation", "checkstyle:LineLength" })
            @SuppressFBWarnings(justification = "Correct way to do get the block attached to a sign in Bukkit", value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
            final Block attachedBlock = block.getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
            if (attachedBlock.getType() == Material.AIR) {
                shopManager.removeShop(sign);
                plugin.getLogger().info("Removed a shop due to physics event");
            }
        } catch (@SuppressWarnings("unused") final ClassCastException | NullPointerException e) {
            final WallSign signData = (WallSign) block.getState().getBlockData();
            final BlockFace attached = signData.getFacing().getOppositeFace();
            final Block attachedBlock = block.getRelative(attached);
            if (attachedBlock.getType() == Material.AIR) {
                shopManager.removeShop(sign);
                plugin.getLogger().info("Removed a shop due to physics event");
            }
        }

    }

    private boolean checkBlockFaces(final Block block, final BlockEvent event, final String mode,
            final Collection<Material> signMaterials) {
        if (!signMaterials.stream().anyMatch(block.getType()::equals)) {
            return false;
        }

        final Sign sign = (Sign) block.getState();
        if (!shopManager.isShop(sign)) {
            return false;
        }

        if (plugin.getConfig().getBoolean("invincibility." + mode, true)) {
            ((Cancellable) event).setCancelled(true);
        } else {
            shopManager.removeShop(sign);
        }

        return true;
    }
}
