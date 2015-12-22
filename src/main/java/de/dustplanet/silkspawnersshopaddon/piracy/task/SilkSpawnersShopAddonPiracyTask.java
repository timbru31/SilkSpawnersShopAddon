package de.dustplanet.silkspawnersshopaddon.piracy.task;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.piracy.checker.BlackListedException;
import de.dustplanet.silkspawnersshopaddon.piracy.checker.SilkSpawnersShopAddonPiracyChecker;

public class SilkSpawnersShopAddonPiracyTask {
    private SilkSpawnersShopAddon plugin;

    public SilkSpawnersShopAddonPiracyTask(SilkSpawnersShopAddon plugin) {
        this.plugin = plugin;
    }

    public void checkPiracy() throws BlackListedException {
        SilkSpawnersShopAddonPiracyChecker piracyChecker = new SilkSpawnersShopAddonPiracyChecker(plugin);
        piracyChecker.sendPost();
    }
}
