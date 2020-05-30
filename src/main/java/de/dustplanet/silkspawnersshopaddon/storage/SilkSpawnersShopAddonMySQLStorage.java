package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SilkSpawnersShopAddonMySQLStorage extends SilkSpawnersShopAddonSQLStorage {

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    public SilkSpawnersShopAddonMySQLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading MySQL storage provider");
        String host = plugin.getConfig().getString("MySQL.host");
        int port = plugin.getConfig().getInt("MySQL.port");
        String user = plugin.getConfig().getString("MySQL.user");
        String pass = plugin.getConfig().getString("MySQL.pass");
        String db = plugin.getConfig().getString("MySQL.database");
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            try (PreparedStatement statement = conn.prepareStatement(CREATE_TABLE)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "There was en error while creating the MySQL table", e);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "There was en error while connecting tothe MySQL database", e);
        }
    }
}
