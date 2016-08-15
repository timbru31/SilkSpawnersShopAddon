package de.dustplanet.silkspawnersshopaddon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.HTTPSTokener;
import org.json.HTTPTokenException;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

import de.dustplanet.silkspawnersshopaddon.commands.SilkSpawnersShopCommands;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonBlockListener;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonPlayerListener;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonProtectionListener;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.util.ScalarYamlConfiguration;
import de.dustplanet.silkspawnersshopaddon.util.Updater;
import de.dustplanet.silkspawnersshopaddon.util.Updater.UpdateResult;
import de.dustplanet.util.SilkUtil;
import net.milkbowl.vault.economy.Economy;

public class SilkSpawnersShopAddon extends JavaPlugin {
    private SilkSpawnersShopManager shopManager;
    private DecimalFormat numberFormat;
    private SilkUtil su;
    private ArrayList<Action> allowedActions = new ArrayList<>();
    private FileConfiguration localization;
    private File configFile, localizationFile;
    private final BlockFace[] blockFaces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
    private static final int RESOURCEID = 12028;
    private String userID = "%%__USER__%%";
    private boolean perMobPermissions;
    /**
     * Economy provider with Vault.
     */
    private Economy econ;

    @Override
    public void onDisable() {
        getAllowedActions().clear();
        if (shopManager != null) {
            shopManager.getStorage().disable();
        }
    }

    @Override
    public void onEnable() {
        if (setupEconomy()) {
            // If Vault is enabled, load the economy
            getLogger().info("Loaded Vault successfully");
        } else {
            // Else tell the admin about the missing of Vault
            getLogger().severe("Vault was not found! Disabling now...");
            onDisable();
            return;
        }

        // Config
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            if (configFile.getParentFile().mkdirs()) {
                copy("config.yml", configFile);
            } else {
                getLogger().severe("The config folder could NOT be created, make sure it's writable!");
                getLogger().severe("Disabling now!");
                setEnabled(false);
                return;
            }
        }

        loadConfig();

        // Localization
        localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }

        setLocalization(ScalarYamlConfiguration.loadConfiguration(localizationFile));
        loadLocalization();

        // Load piracy task runner
        final SilkSpawnersShopAddon addon = this;
        final String id = userID;
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                HTTPSTokener httpsTokener = new HTTPSTokener(addon);
                try {
                    httpsTokener.sendHTTPSToken("%%__NONCE__%%");
                    httpsTokener.sendHTTPSToken("%%__USER__%%");
                    httpsTokener.sendHTTPSToken(id);
                } catch (HTTPTokenException e) {
                    addon.disable();
                    return;
                }
            }
        }, 20L * 120);

        // Setup SilkUtil, shop manager and storage provider
        setSilkUtil(SilkUtil.hookIntoSilkSpanwers());
        setShopManager(new SilkSpawnersShopManager(this));

        // Load dynamic mob permissions
        if (isPerMobPermissions()) {
            loadPermissions("buy", "Allows you to use buy shops");
            loadPermissions("sell", "Allows you to use sell shops");
        }

        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        SilkSpawnersShopAddonBlockListener blockListener = new SilkSpawnersShopAddonBlockListener(this);
        SilkSpawnersShopAddonPlayerListener playerListener = new SilkSpawnersShopAddonPlayerListener(this);
        SilkSpawnersShopAddonProtectionListener entityListener = new SilkSpawnersShopAddonProtectionListener(this);
        pluginManager.registerEvents(blockListener, this);
        pluginManager.registerEvents(playerListener, this);
        pluginManager.registerEvents(entityListener, this);

        // Load command
        getCommand("silkspawnersshopaddon").setExecutor(new SilkSpawnersShopCommands(this));

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            Graph graph = metrics.createGraph("storage provider");
            graph.addPlotter(new Metrics.Plotter(getConfig().getString("storageMethod").toUpperCase(Locale.ENGLISH)) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
            metrics.start();
        } catch (IOException e) {
            getLogger().info("Couldn't start Metrics, please report this!");
            e.printStackTrace();
        }

        // Updater
        boolean updaterDisabled = getConfig().getBoolean("disableUpdater", false);
        if (!updaterDisabled) {
            getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    Updater updater = new Updater(getPlugin(), RESOURCEID, false);
                    UpdateResult result = updater.getResult();
                    if (result == UpdateResult.NO_UPDATE) {
                        getLogger().info("You are running the latest version of SilkSpawnersShopAddon!");
                    } else if (result == UpdateResult.UPDATE_AVAILABLE) {
                        getLogger().info(
                                "There is an update available for SilkSpawnersShopAddon. Go grab it from SpigotMC!");
                        getLogger().info("You are running " + getPlugin().getDescription().getVersion() + ", latest is "
                                + updater.getVersion());
                    } else if (result == UpdateResult.SNAPSHOT_DISABLED) {
                        getLogger().info("Update checking is disabled because you are running a dev build.");
                    } else {
                        getLogger().warning("The Updater returned the following value: " + result.name());
                    }
                }
            }, 40L);
        } else {
            getLogger().info("Updater is disabled");
        }
    }

    private void loadPermissions(String permissionPart, String description) {
        HashMap<String, Boolean> childPermissions = new HashMap<>();
        for (String mobAlias : su.eid2DisplayName.values()) {
            mobAlias = mobAlias.toLowerCase().replace(" ", "");
            childPermissions.put("silkspawners.use" + permissionPart + "." + mobAlias, true);
        }
        Permission perm = new Permission("silkspawners.use" + permissionPart + ".*", description, PermissionDefault.TRUE, childPermissions);
        getServer().getPluginManager().addPermission(perm);
    }

    public JavaPlugin getPlugin() {
        return this;
    }

    public void disable() {
        this.setEnabled(false);
    }

    private void loadLocalization() {
        localization.addDefault("buying.inventoryFull", "&6[SilkSpawners] &4Your inventory is full.");
        localization.addDefault("buying.notEnoughMoney", "&6[SilkSpawners] &4You do not have enough money.");
        localization.addDefault("buying.success",
                "&6[SilkSpawners] &2You bought &e%amount% %creature% spawners &2for &e%price%&2.");
        localization.addDefault("checking.error", "&6[SilkSpawners] &4There was an error removing the invalid shops.");
        localization.addDefault("checking.invalid",
                "&6[SilkSpawners] &4Found an invalid shop at &ex %x%&4, &ey %y%&4, &ez %z% &4in world &e%world%&4.");
        localization.addDefault("checking.success",
                "&6[SilkSpawners] &2Removed &e%size% &2invalid shops from the database.");
        localization.addDefault("creating.error", "&6[SilkSpawners] &4There was an error creating the shop.");
        localization.addDefault("creating.invalidAmount", "&6[SilkSpawners] &4The given amount is invalid.");
        localization.addDefault("creating.invalidMob", "&6[SilkSpawners] &4The given mob is invalid.");
        localization.addDefault("creating.invalidMode", "&6[SilkSpawners] &4The given shop mode is invalid.");
        localization.addDefault("creating.invalidPrice",
                "&6[SilkSpawners] &4The given price is invalid. Please use numbers only!");
        localization.addDefault("creating.success", "&6[SilkSpawners] &2You created the shop successfully.");
        localization.addDefault("noPermission.building",
                "&6[SilkSpawners] &4You do not have the permission to create a shop.");
        localization.addDefault("noPermission.buy",
                "&6[SilkSpawners] &4You do not have the permission to buy spawners.");
        localization.addDefault("noPermission.check",
                "&6[SilkSpawners] &4You do not have the permission to check for invalid shops.");
        localization.addDefault("noPermission.destroying",
                "&6[SilkSpawners] &4You do not have the permission to destroy a shop.");
        localization.addDefault("noPermission.edit",
                "&6[SilkSpawners] &4You do not have the permission to edit a shop.");
        localization.addDefault("noPermission.sell",
                "&6[SilkSpawners] &4You do not have the permission to sell spawners.");
        localization.addDefault("removing.error", "&6[SilkSpawners] &4There was an error removing the shop.");
        localization.addDefault("removing.success", "&6[SilkSpawners] &2You removed the shop successfully.");
        localization.addDefault("selling.noSpawnerInHand",
                "&6[SilkSpawners] &4You do not have a spawner in your hand.");
        localization.addDefault("selling.notEnoughSpawners", "&6[SilkSpawners] &4You do not have enough spawners in your hand.");
        localization.addDefault("selling.notTheSameMob",
                "&6[SilkSpawners] &4The spawner in your hand is not a(n) &e%creature% spawner&4.");
        localization.addDefault("selling.success",
                "&6[SilkSpawners] &2You sold &e%amount% %creature% spawners &2for &e%price%&2.");
        localization.addDefault("updating.commandUsage",
                "&6[SilkSpawners] &eUsage for editing a shop: /shop amount|mode|mob|price <newValue>");
        localization.addDefault("updating.noConsole",
                "&6[SilkSpawners] &4The console is not able to edit shops, only remove invalid shops via /shop check");
        localization.addDefault("updating.noShop", "&6[SilkSpawners] &4There is no shop in sight.");
        localization.addDefault("updating.error", "&6[SilkSpawners] &4There was an error updating the shop.");
        localization.addDefault("updating.success", "&6[SilkSpawners] &2The shop was updated successfully!");
        localization.options().copyDefaults(true);
        saveLocalization();
    }

    private void saveLocalization() {
        try {
            localization.save(localizationFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save the localization! Please report this! (I/O)");
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        // Add defaults
        FileConfiguration config = getConfig();
        config.options().header("Valid storage methods are YAML, MONGODB, MYSQL and HSQLDB");
        config.addDefault("disableUpdater", false);
        config.addDefault("shopIdentifier", "&9[SilkSpawners]");
        config.addDefault("numberFormat", "$ 00.##");
        List<String> tempStringAllowedActions = new ArrayList<>();
        tempStringAllowedActions.add(Action.RIGHT_CLICK_BLOCK.toString());
        config.addDefault("allowedActions", tempStringAllowedActions);
        config.addDefault("invincibility.burn", true);
        config.addDefault("invincibility.explode", true);
        config.addDefault("invincibility.ignite", true);
        config.addDefault("forceInventoryUpdate", false);
        config.addDefault("perMobPermissions", false);
        config.addDefault("storageMethod", "YAML");
        config.addDefault("mongoDB.host", "localhost");
        config.addDefault("mongoDB.port", 27017);
        config.addDefault("mongoDB.user", "");
        config.addDefault("mongoDB.pass", "");
        config.addDefault("mongoDB.database", "silkspawners");
        config.addDefault("mongoDB.collection", "shops");
        config.addDefault("MySQL.host", "localhost");
        config.addDefault("MySQL.port", 3306);
        config.addDefault("MySQL.user", "");
        config.addDefault("MySQL.pass", "");
        config.addDefault("MySQL.database", "silkspawners");
        config.addDefault("HSQLDB.user", "SA");
        config.addDefault("HSQLDB.pass", "");
        config.addDefault("HSQLDB.database", "shops.db");
        config.options().copyDefaults(true);
        saveConfig();

        // Load values
        String numberFormatString = config.getString("numberFormat", "$ 00.##");
        setNumberFormat(new DecimalFormat(numberFormatString));
        tempStringAllowedActions = config.getStringList("allowedActions");
        ArrayList<Action> tempAllowedActions = new ArrayList<>();
        for (String allowedAction : tempStringAllowedActions) {
            tempAllowedActions.add(Action.valueOf(allowedAction));
        }
        setAllowedActions(tempAllowedActions);
        setPerMobPermissions(config.getBoolean("perMobPermissions", false));
    }

    /**
     * Hook into Vault.
     *
     * @return whether the hook into Vault was successful
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        setEcon(rsp.getProvider());
        return getEcon() != null;
    }

    // If no config is found, copy the default one(s)!
    public void copy(String yml, File file) {
        try (OutputStream out = new FileOutputStream(file); InputStream in = getResource(yml)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            getLogger().warning("Failed to copy the default config! (I/O)");
            e.printStackTrace();
        }
    }

    public String getFormattedPrice(String price) {
        return getFormattedPrice(Double.parseDouble(price));
    }

    public String getFormattedPrice(double price) {
        DecimalFormat df = getNumberFormat();
        return df.format(price);
    }

    public SilkSpawnersShopManager getShopManager() {
        return shopManager;
    }

    public void setShopManager(SilkSpawnersShopManager shopManager) {
        this.shopManager = shopManager;
    }

    public SilkUtil getSilkUtil() {
        return su;
    }

    public void setSilkUtil(SilkUtil silkUtil) {
        this.su = silkUtil;
    }

    public ArrayList<Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(ArrayList<Action> allowedActions) {
        this.allowedActions = allowedActions;
    }

    public Economy getEcon() {
        return econ;
    }

    public void setEcon(Economy econ) {
        this.econ = econ;
    }

    public FileConfiguration getLocalization() {
        return localization;
    }

    public void setLocalization(FileConfiguration localization) {
        this.localization = localization;
    }

    public BlockFace[] getBlockFaces() {
        return blockFaces.clone();
    }

    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public boolean isPerMobPermissions() {
        return perMobPermissions;
    }

    public void setPerMobPermissions(boolean perMobPermissions) {
        this.perMobPermissions = perMobPermissions;
    }
}
