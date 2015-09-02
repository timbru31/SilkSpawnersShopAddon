package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;

public class SilkSpawnersShopAddonMySQLStorage extends SilkSpawnersShopAddonSQLStorage {
    public SilkSpawnersShopAddonMySQLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
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
                plugin.getLogger().severe("There was en error while creating the MySQL table");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("There was en error while connecting tothe MySQL database");
            e.printStackTrace();
        }
        plugin.getLogger().info("Loading MySQL storage provider");
    }
}
