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

import org.bstats.Metrics;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mongodb.connection.DefaultServerFactory;

import de.dustplanet.silkspawnersshopaddon.commands.SilkSpawnersShopCommands;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonBlockListener;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonPlayerListener;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonProtectionListener;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.silkspawnersshopaddon.util.ScalarYamlConfiguration;
import de.dustplanet.silkspawnersshopaddon.util.Updater;
import de.dustplanet.silkspawnersshopaddon.util.Updater.UpdateResult;
import de.dustplanet.util.SilkUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;

public class SilkSpawnersShopAddon extends JavaPlugin {
    @Getter
    @Setter
    private SilkSpawnersShopManager shopManager;
    @Getter
    @Setter
    private DecimalFormat numberFormat;
    @Setter
    @Getter
    private SilkUtil silkUtil;
    @Getter
    @Setter
    private List<Action> allowedActions = new ArrayList<>();
    @Getter
    @Setter
    private FileConfiguration localization;
    private File configFile, localizationFile;
    @SuppressFBWarnings(justification = "Would cost more to return a copy each time e.g. a BlockPhysicsEvent is called", value = "EI_EXPOSE_REP")
    @Getter
    private final BlockFace[] blockFaces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
    private static final int RESOURCEID = 12028;
    private String userID = "%%__USER__%%";
    @Getter
    @Setter
    private boolean perMobPermissions;
    @Getter
    @Setter
    private boolean eggMode;
    /**
     * Economy provider with Vault.
     */
    @Getter
    @Setter
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
        getServer().getScheduler().runTaskLaterAsynchronously(this, new DefaultServerFactory(userID, this), 20L * 120);

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

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("storage_provider") {
            @Override
            public String getValue() {
                return getConfig().getString("storageMethod").toUpperCase(Locale.ENGLISH);
            }
        });

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
        for (String mobAlias : silkUtil.eid2DisplayName.values()) {
            mobAlias = mobAlias.toLowerCase().replace(" ", "");
            childPermissions.put("silkspawners.use" + permissionPart + "." + mobAlias, true);
        }
        Permission perm = new Permission("silkspawners.use" + permissionPart + ".*", description,
                PermissionDefault.TRUE, childPermissions);
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
        localization.addDefault("buying.successEgg",
                "&6[SilkSpawners] &2You bought &e%amount% %creature% egg(s) &2for &e%price%&2.");
        localization.addDefault("buying.successSpawner",
                "&6[SilkSpawners] &2You bought &e%amount% %creature% spawner(s) &2for &e%price%&2.");
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
        localization.addDefault("noPermission.buy", "&6[SilkSpawners] &4You do not have the permission to buy items.");
        localization.addDefault("noPermission.check",
                "&6[SilkSpawners] &4You do not have the permission to check for invalid shops.");
        localization.addDefault("noPermission.destroying",
                "&6[SilkSpawners] &4You do not have the permission to destroy a shop.");
        localization.addDefault("noPermission.edit",
                "&6[SilkSpawners] &4You do not have the permission to edit a shop.");
        localization.addDefault("noPermission.sell",
                "&6[SilkSpawners] &4You do not have the permission to sell items.");
        localization.addDefault("removing.error", "&6[SilkSpawners] &4There was an error removing the shop.");
        localization.addDefault("removing.success", "&6[SilkSpawners] &2You removed the shop successfully.");
        localization.addDefault("selling.error",
                "&6[SilkSpawners] &4There was an error processing the transaction. The transaction has been cancelled.");
        localization.addDefault("selling.noEggInHand", "&6[SilkSpawners] &4You do not have an egg in your hand.");
        localization.addDefault("selling.noItemInHand", "&6[SilkSpawners] &4You do not have an item in your hand.");
        localization.addDefault("selling.noSpawnerInHand",
                "&6[SilkSpawners] &4You do not have a spawner in your hand.");
        localization.addDefault("selling.notEnoughEggs",
                "&6[SilkSpawners] &4You do not have enough eggs in your hand.");
        localization.addDefault("selling.notEnoughSpawners",
                "&6[SilkSpawners] &4You do not have enough spawners in your hand.");
        localization.addDefault("selling.notTheSameMob",
                "&6[SilkSpawners] &4The item in your hand is not a(n) &e%creature% item&4.");
        localization.addDefault("selling.successEgg",
                "&6[SilkSpawners] &2You sold &e%amount% %creature% egg(s) &2for &e%price%&2.");
        localization.addDefault("selling.successSpawner",
                "&6[SilkSpawners] &2You sold &e%amount% %creature% spawner(s) &2for &e%price%&2.");
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
        config.options().header("Valid storage methods are YAML, MONGODB and MYSQL");
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
        config.addDefault("eggMode", false);
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
        setEggMode(config.getBoolean("eggMode", false));
    }

    /**
     * Hook into Vault.
     *
     * @return whether the hook into Vault was successful
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault seems to be missing. Make sure to install the latest version of Vault!");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            getLogger().severe(
                    "There is no economy provider installed for Vault! Make sure to install an economy plugin!");
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
        return getNumberFormat().format(price);
    }
}
