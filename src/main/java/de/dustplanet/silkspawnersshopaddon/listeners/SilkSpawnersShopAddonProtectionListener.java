package de.dustplanet.silkspawnersshopaddon.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SilkSpawnersShopAddonProtectionListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;

    public SilkSpawnersShopAddonProtectionListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    private boolean checkBlockFaces(Block block, BlockEvent event, String mode, Material[] signMaterials) {
        boolean match = false;
        for (Material m : signMaterials) {
            if (block.getType() == m) {
                match = true;
                break;
            }
        }
        if (match) {
            Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                if (plugin.getConfig().getBoolean("invincibility." + mode, true)) {
                    ((Cancellable) event).setCancelled(true);
                } else {
                    shopManager.removeShop(sign);
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        if (!checkBlockFaces(block, event, "ignite", new Material[] { Material.WALL_SIGN, Material.SIGN_POST })) {
            for (BlockFace face : plugin.getBlockFaces()) {
                Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "ignite", new Material[] { Material.WALL_SIGN })) {
                    break;
                }
            }
            Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "ignite", new Material[] { Material.SIGN_POST });
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (!checkBlockFaces(block, event, "burn", new Material[] { Material.WALL_SIGN, Material.SIGN_POST })) {
            for (BlockFace face : plugin.getBlockFaces()) {
                Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "burn", new Material[] { Material.WALL_SIGN })) {
                    break;
                }
            }
            Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "burn", new Material[] { Material.SIGN_POST });
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

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
            Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                @SuppressFBWarnings(justification = "Correct way to do get the block attached to a sign in Bukkit", value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
                Block attachedBlock = block.getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
                if (attachedBlock.getType() == Material.AIR) {
                    shopManager.removeShop(sign);
                    plugin.getLogger().info("Removed a shop due to physics event");
                }
            }
        }
    }
}
