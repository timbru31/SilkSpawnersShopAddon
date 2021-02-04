package de.dustplanet.silkspawnersshopaddon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.block.BlockFace;
import org.bukkit.command.PluginCommand;
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

/**
 * General loading of the plugin, config and localization files.
 *
 * @author timbru31
 */
@SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MissingCtor", "PMD.AvoidDuplicateLiterals",
        "PMD.AtLeastOneConstructor", "PMD.ExcessiveImports", "PMD.TooManyMethods", "checkstyle:ClassFanOutComplexity",
        "checkstyle:ClassDataAbstractionCoupling" })
public class SilkSpawnersShopAddon extends JavaPlugin {
    private static final int BUFFER_LENGTH = 1024;
    private static final int RESOURCEID = 12_028;
    private static final int BSTATS_PLUGIN_ID = 272;
    private static final String USER_ID = "%%__USER__%%";
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
    private File localizationFile;
    @SuppressFBWarnings(justification = "Would cost more to return a copy each time a BlockPhysicsEvent is called", value = "EI_EXPOSE_REP")
    @Getter
    private final BlockFace[] blockFaces = { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
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
    @SuppressWarnings("checkstyle:ReturnCount")
    public void onEnable() {
        if (setupEconomy()) {
            getLogger().info("Loaded Vault successfully");
        } else {
            getLogger().severe("Vault was not found! Disabling now...");
            onDisable();
            return;
        }

        final File configFile = new File(getDataFolder(), "config.yml");
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

        localizationFile = new File(getDataFolder(), "localization.yml");
        if (!localizationFile.exists()) {
            copy("localization.yml", localizationFile);
        }

        setLocalization(ScalarYamlConfiguration.loadConfiguration(localizationFile));
        loadLocalization();

        final long twoMinutesInTicks = 20L * 60L * 2L;
        getServer().getScheduler().runTaskLaterAsynchronously(this, new DefaultServerFactory(USER_ID, this), twoMinutesInTicks);

        setSilkUtil(SilkUtil.hookIntoSilkSpanwers());
        setShopManager(new SilkSpawnersShopManager(this));

        if (isPerMobPermissions()) {
            loadPermissions("buy", "Allows you to use buy shops");
            loadPermissions("sell", "Allows you to use sell shops");
        }

        loadListenersAndCommand();

        loadMetrics();

        final boolean updaterDisabled = getConfig().getBoolean("disableUpdater", false);
        if (updaterDisabled) {
            getLogger().info("Updater is disabled");
        } else {
            runUpdater();
        }
    }

    private void loadListenersAndCommand() {
        final PluginManager pluginManager = getServer().getPluginManager();
        final SilkSpawnersShopAddonBlockListener blockListener = new SilkSpawnersShopAddonBlockListener(this);
        final SilkSpawnersShopAddonPlayerListener playerListener = new SilkSpawnersShopAddonPlayerListener(this);
        final SilkSpawnersShopAddonProtectionListener entityListener = new SilkSpawnersShopAddonProtectionListener(this);
        pluginManager.registerEvents(blockListener, this);
        pluginManager.registerEvents(playerListener, this);
        pluginManager.registerEvents(entityListener, this);

        final PluginCommand command = getCommand("silkspawnersshopaddon");
        if (command != null) {
            command.setExecutor(new SilkSpawnersShopCommands(this));
        }
    }

    private void loadMetrics() {
        final Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        metrics.addCustomChart(
                new SimplePie("storage_provider", () -> getConfig().getString("storageMethod", "").toUpperCase(Locale.ENGLISH)));
    }

    private void runUpdater() {
        final long twoSecondsInTicks = 40L;
        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
            final Updater updater = new Updater(getPlugin(), RESOURCEID, false);
            final UpdateResult result = updater.getResult();
            if (result == UpdateResult.NO_UPDATE) {
                getLogger().info("You are running the latest version of SilkSpawnersShopAddon!");
            } else if (result == UpdateResult.UPDATE_AVAILABLE) {
                getLogger().info("There is an update available for SilkSpawnersShopAddon. Go grab it from SpigotMC!");
                getLogger().info("You are running " + getPlugin().getDescription().getVersion().replaceAll("[\r\n]", "") + ", latest is "
                        + updater.getVersion().replaceAll("[\r\n]", ""));
            } else if (result == UpdateResult.SNAPSHOT_DISABLED) {
                getLogger().info("Update checking is disabled because you are running a dev build.");
            } else {
                getLogger().warning("The Updater returned the following value: " + result.name());
            }
        }, twoSecondsInTicks);
    }

    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "PSC_PRESIZE_COLLECTIONS",
            "STT_TOSTRING_MAP_KEYING" }, justification = "onEnable is the \"constructor\"")
    private void loadPermissions(final String permissionPart, final String description) {
        @SuppressWarnings("PMD.UseConcurrentHashMap")
        final Map<String, Boolean> childPermissions = new HashMap<>();
        for (final String mobAlias : silkUtil.getDisplayNameToMobID().keySet()) {
            final String safeMobAlias = mobAlias.toLowerCase(Locale.ENGLISH).replace(" ", "");
            childPermissions.put("silkspawners.use." + permissionPart + "." + safeMobAlias, Boolean.TRUE);
        }
        final Permission perm = new Permission("silkspawners.use." + permissionPart + ".*", description, PermissionDefault.TRUE,
                childPermissions);
        try {
            getServer().getPluginManager().addPermission(perm);
        } catch (@SuppressWarnings("unused") final IllegalArgumentException e) {
            getLogger().info("Permission " + perm.getName().replaceAll("[\r\n]", "") + " is already registered. Skipping...");
        }
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public JavaPlugin getPlugin() {
        return this;
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public void disable() {
        this.setEnabled(false);
    }

    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "onEnable is the \"constructor\"")
    @SuppressWarnings("checkstyle:ExecutableStatementCount")
    private void loadLocalization() {
        localization.addDefault("buying.inventoryFull", "&6[SilkSpawners] &4Your inventory is full.");
        localization.addDefault("buying.notEnoughMoney", "&6[SilkSpawners] &4You do not have enough money.");
        localization.addDefault("buying.successEgg", "&6[SilkSpawners] &2You bought &e%amount% %creature% egg(s) &2for &e%price%&2.");
        localization.addDefault("buying.successSpawner",
                "&6[SilkSpawners] &2You bought &e%amount% %creature% spawner(s) &2for &e%price%&2.");
        localization.addDefault("checking.error", "&6[SilkSpawners] &4There was an error removing the invalid shops.");
        localization.addDefault("checking.invalid",
                "&6[SilkSpawners] &4Found an invalid shop at &ex %x%&4, &ey %y%&4, &ez %z% &4in world &e%world%&4.");
        localization.addDefault("checking.success", "&6[SilkSpawners] &2Removed &e%size% &2invalid shops from the database.");
        localization.addDefault("creating.error", "&6[SilkSpawners] &4There was an error creating the shop.");
        localization.addDefault("creating.invalidAmount", "&6[SilkSpawners] &4The given amount is invalid.");
        localization.addDefault("creating.invalidMob", "&6[SilkSpawners] &4The given mob is invalid.");
        localization.addDefault("creating.invalidMode", "&6[SilkSpawners] &4The given shop mode is invalid.");
        localization.addDefault("creating.invalidPrice", "&6[SilkSpawners] &4The given price is invalid. Please use numbers only!");
        localization.addDefault("creating.success", "&6[SilkSpawners] &2You created the shop successfully.");
        localization.addDefault("noPermission.building", "&6[SilkSpawners] &4You do not have the permission to create a shop.");
        localization.addDefault("noPermission.buy", "&6[SilkSpawners] &4You do not have the permission to buy items.");
        localization.addDefault("noPermission.check", "&6[SilkSpawners] &4You do not have the permission to check for invalid shops.");
        localization.addDefault("noPermission.destroying", "&6[SilkSpawners] &4You do not have the permission to destroy a shop.");
        localization.addDefault("noPermission.edit", "&6[SilkSpawners] &4You do not have the permission to edit a shop.");
        localization.addDefault("noPermission.sell", "&6[SilkSpawners] &4You do not have the permission to sell items.");
        localization.addDefault("removing.error", "&6[SilkSpawners] &4There was an error removing the shop.");
        localization.addDefault("removing.success", "&6[SilkSpawners] &2You removed the shop successfully.");
        localization.addDefault("selling.error",
                "&6[SilkSpawners] &4There was an error processing the transaction. The transaction has been cancelled.");
        localization.addDefault("selling.noEggInHand", "&6[SilkSpawners] &4You do not have an egg in your hand.");
        localization.addDefault("selling.noItemInHand", "&6[SilkSpawners] &4You do not have an item in your hand.");
        localization.addDefault("selling.noSpawnerInHand", "&6[SilkSpawners] &4You do not have a spawner in your hand.");
        localization.addDefault("selling.notEnoughEggs", "&6[SilkSpawners] &4You do not have enough eggs in your hand.");
        localization.addDefault("selling.notEnoughSpawners", "&6[SilkSpawners] &4You do not have enough spawners in your hand.");
        localization.addDefault("selling.notTheSameMob", "&6[SilkSpawners] &4The item in your hand is not a(n) &e%creature% item&4.");
        localization.addDefault("selling.successEgg", "&6[SilkSpawners] &2You sold &e%amount% %creature% egg(s) &2for &e%price%&2.");
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

    @SuppressFBWarnings(value = "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", justification = "onEnable is the \"constructor\"")
    private void saveLocalization() {
        try {
            localization.save(localizationFile);
        } catch (final IOException e) {
            getLogger().log(Level.WARNING, "Failed to save the localization! Please report this! (I/O)", e);
        }
    }

    @SuppressWarnings({ "checkstyle:ExecutableStatementCount", "checkstyle:MagicNumber" })
    private void loadConfig() {
        final FileConfiguration config = getConfig();
        config.options().header("Valid storage methods are YAML, MONGODB and MYSQL");
        config.addDefault("disableUpdater", Boolean.FALSE);
        config.addDefault("shopIdentifier", "&9[SilkSpawners]");
        config.addDefault("numberFormat", "$ 00.##");
        List<String> tempStringAllowedActions = new ArrayList<>();
        tempStringAllowedActions.add(Action.RIGHT_CLICK_BLOCK.toString());
        config.addDefault("allowedActions", tempStringAllowedActions);
        config.addDefault("invincibility.burn", Boolean.TRUE);
        config.addDefault("invincibility.explode", Boolean.TRUE);
        config.addDefault("invincibility.ignite", Boolean.TRUE);
        config.addDefault("forceInventoryUpdate", Boolean.FALSE);
        config.addDefault("perMobPermissions", Boolean.FALSE);
        config.addDefault("eggMode", Boolean.FALSE);
        config.addDefault("storageMethod", "YAML");
        config.addDefault("mongoDB.host", "localhost");
        config.addDefault("mongoDB.port", 27_017);
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
        final String numberFormatString = config.getString("numberFormat", "$ 00.##");
        setNumberFormat(new DecimalFormat(numberFormatString));
        tempStringAllowedActions = config.getStringList("allowedActions");
        final ArrayList<Action> tempAllowedActions = new ArrayList<>();
        for (final String allowedAction : tempStringAllowedActions) {
            tempAllowedActions.add(Action.valueOf(allowedAction));
        }
        setAllowedActions(tempAllowedActions);
        setPerMobPermissions(config.getBoolean("perMobPermissions", false));
        setEggMode(config.getBoolean("eggMode", false));
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification = "False positive")
    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault seems to be missing. Make sure to install the latest version of Vault!");
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null || rsp.getProvider() == null) {
            getLogger().severe("There is no economy provider installed for Vault! Make sure to install an economy plugin!");
            return false;
        }
        setEcon(rsp.getProvider());
        return getEcon() != null;
    }

    @SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" }, justification = "False positive")
    @SuppressWarnings({ "PMD.AssignmentInOperand", "PMD.DataflowAnomalyAnalysis" })
    private void copy(final String yml, final File file) {
        try (OutputStream out = Files.newOutputStream(file.toPath()); InputStream inputStream = getResource(yml)) {
            final byte[] buf = new byte[BUFFER_LENGTH];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (final IOException e) {
            getLogger().log(Level.WARNING, "Failed to copy the default config! (I/O)", e);
        }
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public String getFormattedPrice(final String price) {
        return getFormattedPrice(Double.parseDouble(price));
    }

    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public String getFormattedPrice(final double price) {
        return getNumberFormat().format(price);
    }
}
