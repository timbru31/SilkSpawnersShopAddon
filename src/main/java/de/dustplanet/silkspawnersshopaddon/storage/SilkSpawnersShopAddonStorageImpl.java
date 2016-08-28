package de.dustplanet.silkspawnersshopaddon.storage;

import java.util.ArrayList;

import org.bukkit.scheduler.BukkitTask;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;

public class SilkSpawnersShopAddonStorageImpl {
    protected ArrayList<SilkSpawnersShop> cachedShops = new ArrayList<>();
    protected SilkSpawnersShopAddon plugin;
    private int taskId;
    private SilkSpawnersCleanupRunner cleanTask;

    public SilkSpawnersShopAddonStorageImpl(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
        cleanTask = new SilkSpawnersCleanupRunner();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, cleanTask, 1200L, 1200L);
        taskId = task.getTaskId();
    }

    private class SilkSpawnersCleanupRunner implements Runnable {
        public SilkSpawnersCleanupRunner() {}

        @Override
        public void run() {
            cachedShops.clear();
        }
    }

    public void disable() {
        cachedShops.clear();
        plugin.getServer().getScheduler().cancelTask(taskId);
    }
}
