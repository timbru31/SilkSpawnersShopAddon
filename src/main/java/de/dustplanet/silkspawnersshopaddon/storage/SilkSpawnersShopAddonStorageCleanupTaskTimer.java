package de.dustplanet.silkspawnersshopaddon.storage;

import java.util.ArrayList;
import java.util.List;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

/**
 * Cleanup task timer of the cached shops.
 *
 * @author timbru31
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class SilkSpawnersShopAddonStorageCleanupTaskTimer {
    private static final long ONE_MINUTE_IN_TICKS = 20L * 60;
    @Getter
    private final List<SilkSpawnersShop> cachedShops = new ArrayList<>();
    @Getter
    private final SilkSpawnersShopAddon plugin;
    private final int taskId;

    @SuppressWarnings({ "checkstyle:MissingJavadocMethod", "checkstyle:SeparatorWrap" })
    public SilkSpawnersShopAddonStorageCleanupTaskTimer(final SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
        taskId = plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> cachedShops.clear(), ONE_MINUTE_IN_TICKS, ONE_MINUTE_IN_TICKS).getTaskId();
    }

    /**
     * Cleans all cached shops and cancels the cleanup timer.
     */
    public void disable() {
        cachedShops.clear();
        plugin.getServer().getScheduler().cancelTask(taskId);
    }
}
