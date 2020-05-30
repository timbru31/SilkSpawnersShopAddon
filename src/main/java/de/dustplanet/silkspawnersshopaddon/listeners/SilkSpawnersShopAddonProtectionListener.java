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

public class SilkSpawnersShopAddonProtectionListener implements Listener {
    private SilkSpawnersShopAddon plugin;
    private SilkSpawnersShopManager shopManager;
    private SignHelper signHelper = new SignHelper();

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "IMC_IMMATURE_CLASS_NO_TOSTRING" })
    public SilkSpawnersShopAddonProtectionListener(SilkSpawnersShopAddon instance) {
        plugin = instance;
        shopManager = plugin.getShopManager();
    }

    private boolean checkBlockFaces(Block block, BlockEvent event, String mode, Collection<Material> signMaterials) {
        if (signMaterials.stream().anyMatch(block.getType()::equals)) {
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
        if (!checkBlockFaces(block, event, "ignite", signHelper.getAllSignMaterials())) {
            for (BlockFace face : plugin.getBlockFaces()) {
                Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "ignite", signHelper.getWallSignMaterials())) {
                    break;
                }
            }
            Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "ignite", signHelper.getSignMaterials());
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (!checkBlockFaces(block, event, "burn", signHelper.getAllSignMaterials())) {
            for (BlockFace face : plugin.getBlockFaces()) {
                Block attachedBlock = block.getRelative(face);
                if (checkBlockFaces(attachedBlock, event, "burn", signHelper.getWallSignMaterials())) {
                    break;
                }
            }
            Block attachedBlock = block.getRelative(BlockFace.UP);
            checkBlockFaces(attachedBlock, event, "burn", signHelper.getSignMaterials());
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (signHelper.getAllSignMaterials().contains(block.getType())) {
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
        if (signHelper.getAllSignMaterials().contains(block.getType())) {
            Sign sign = (Sign) block.getState();
            if (shopManager.isShop(sign)) {
                try {
                    @SuppressWarnings("deprecation")
                    @SuppressFBWarnings(justification = "Correct way to do get the block attached to a sign in Bukkit", value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE")
                    Block attachedBlock = block.getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
                    if (attachedBlock.getType() == Material.AIR) {
                        shopManager.removeShop(sign);
                        plugin.getLogger().info("Removed a shop due to physics event");
                    }
                } catch (@SuppressWarnings("unused") ClassCastException e) {
                    WallSign signData = (WallSign) block.getState().getBlockData();
                    BlockFace attached = signData.getFacing().getOppositeFace();
                    Block attachedBlock = block.getRelative(attached);
                    if (attachedBlock.getType() == Material.AIR) {
                        shopManager.removeShop(sign);
                        plugin.getLogger().info("Removed a shop due to physics event");
                    }
                }
            }
        }
    }
}
