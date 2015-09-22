package de.dustplanet.silkspawnersshopaddon.storage;

import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;


public class SilkSpawnersShopAddonHSQLDBStorage extends SilkSpawnersShopAddonSQLStorage {
    public SilkSpawnersShopAddonHSQLDBStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading HSQLDB storage provider");
        String user = plugin.getConfig().getString("HSQLDB.user");
        String pass = plugin.getConfig().getString("HSQLDB.pass");
        String db = plugin.getConfig().getString("HSQLDB.database");
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:file:" + plugin.getDataFolder() + File.separator + db, user,
                    pass);
            try (PreparedStatement statement = conn.prepareStatement(CREATE_TABLE)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("There was en error while creating the HSQLDB table");
                e.printStackTrace();
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("There was en error while connecting the HSQLDB database");
            e.printStackTrace();
        }
    }
}
