package de.dustplanet.silkspawnersshopaddon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonBlockListener;
import de.dustplanet.silkspawnersshopaddon.listeners.SilkSpawnersShopAddonPlayerListener;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShopManager;
import de.dustplanet.util.SilkUtil;
import net.milkbowl.vault.economy.Economy;

public class SilkSpawnersShopAddon extends JavaPlugin {
    private SilkSpawnersShopManager shopManager;
    private String currencySign = "$";
    private SilkUtil su;
    private ArrayList<Action> allowedActions = new ArrayList<>();
    private FileConfiguration localization;
    private File configFile, localizationFile;
    /**
     * Economy provider with Vault.
     */
    private Economy econ;

    @Override
    public void onDisable() {
        getAllowedActions().clear();
        shopManager.getStorage().disable();
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

        setLocalization(YamlConfiguration.loadConfiguration(localizationFile));
        loadLocalization();

        // Setup SilkUtil, shop manager and storage provider
        setSilkUtil(SilkUtil.hookIntoSilkSpanwers());
        setShopManager(new SilkSpawnersShopManager(this));

        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        SilkSpawnersShopAddonBlockListener blockListener = new SilkSpawnersShopAddonBlockListener(this);
        SilkSpawnersShopAddonPlayerListener playerListener = new SilkSpawnersShopAddonPlayerListener(this);
        pluginManager.registerEvents(blockListener, this);
        pluginManager.registerEvents(playerListener, this);

        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().info("Couldn't start Metrics, please report this!");
            e.printStackTrace();
        }
    }

    private void loadLocalization() {
        localization.addDefault("buying.inventoryFull", "&6[SilkSpawners] &4Your inventory is full.");
        localization.addDefault("buying.notEnoughMoney", "&6[SilkSpawners] &4You do not have enough money.");
        localization.addDefault("buying.success", "&6[SilkSpawners] &2You bought a(n) &e%creature% spawner &2for &e%price%&2.");
        localization.addDefault("creating.error", "&6[SilkSpawners] &4There was an error creating the shop.");
        localization.addDefault("creating.invalidMob", "&6[SilkSpawners] &4The given mob is invald.");
        localization.addDefault("creating.invalidMode", "&6[SilkSpawners] &4The given shop mode is invalid.");
        localization.addDefault("creating.invalidPrice", "&6[SilkSpawners] &4The given price is invalid.");
        localization.addDefault("creating.success", "&6[SilkSpawners] &2You created the shop successfully.");
        localization.addDefault("noPermission.building", "&6[SilkSpawners] &4You do not have the permission to create a shop.");
        localization.addDefault("noPermission.destroying", "&6[SilkSpawners] &4You do not have the permission to destroy a shop.");
        localization.addDefault("removing.error", "&6[SilkSpawners] &4There was an error removing the shop.");
        localization.addDefault("removing.success", "&6[SilkSpawners] &2You removed the shop successfully.");
        localization.addDefault("selling.noSpawnerInHand", "&6[SilkSpawners] &4You do not have a spawner in your hand.");
        localization.addDefault("selling.notTheSameMob", "&6[SilkSpawners] &4The spawner in your hand is not a(n) &e%creature% spawner&4.");
        localization.addDefault("selling.success", "&6[SilkSpawners] &2You sold a(n) &e%creature% spawner &2for &e%price%&2.");
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
        config.addDefault("currencySign", "$");
        List<String> tempStringAllowedActions = new ArrayList<>();
        tempStringAllowedActions.add(Action.RIGHT_CLICK_BLOCK.toString());
        config.addDefault("allowedActions", tempStringAllowedActions);
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
        setCurrencySign(config.getString("currencySign"));
        tempStringAllowedActions = config.getStringList("allowedActions");
        ArrayList<Action> tempAllowedActions = new ArrayList<>();
        for (String allowedAction : tempStringAllowedActions) {
            tempAllowedActions.add(Action.valueOf(allowedAction));
        }
        setAllowedActions(tempAllowedActions);
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
        try (OutputStream out = new FileOutputStream(file);
                InputStream in = getResource(yml)) {
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

    public SilkSpawnersShopManager getShopManager() {
        return shopManager;
    }

    public void setShopManager(SilkSpawnersShopManager shopManager) {
        this.shopManager = shopManager;
    }

    public String getCurrencySign() {
        return currencySign;
    }

    public void setCurrencySign(String currencySign) {
        this.currencySign = currencySign;
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
}
