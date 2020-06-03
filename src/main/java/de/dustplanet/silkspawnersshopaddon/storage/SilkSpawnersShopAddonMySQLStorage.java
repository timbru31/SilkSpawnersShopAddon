package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The MySQL implementation of the SQL storage class.
 *
 * @author timbru31
 */
public class SilkSpawnersShopAddonMySQLStorage extends AbstractSilkSpawnersShopAddonSQLStorage {

    /**
     * The constructor to open a MySQL database connection via JDBC.
     *
     * @param plugin the plugin
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public SilkSpawnersShopAddonMySQLStorage(final SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading MySQL storage provider");
        final String host = plugin.getConfig().getString("MySQL.host");
        final int port = plugin.getConfig().getInt("MySQL.port");
        final String user = plugin.getConfig().getString("MySQL.user");
        final String pass = plugin.getConfig().getString("MySQL.pass");
        final String database = plugin.getConfig().getString("MySQL.database");
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
            try (PreparedStatement statement = conn.prepareStatement(CREATE_TABLE)) {
                statement.executeUpdate();
            } catch (final SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "There was en error while creating the MySQL table", e);
            }
        } catch (final SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "There was en error while connecting tothe MySQL database", e);
        }
    }
}
