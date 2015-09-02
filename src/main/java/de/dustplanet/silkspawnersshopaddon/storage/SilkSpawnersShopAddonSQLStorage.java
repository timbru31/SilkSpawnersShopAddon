package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;

public abstract class SilkSpawnersShopAddonSQLStorage extends SilkSpawnersShopAddonStorageImpl
implements ISilkSpawnersShopAddonStorage {
    protected Connection conn;
    protected static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS SHOPS(SHOPID VARCHAR(36) PRIMARY KEY, MODE VARCHAR(4) NOT NULL, MOB VARCHAR(255) NOT NULL, PRICE NUMERIC(10,3) NOT NULL, X INTEGER NOT NULL, Y INTEGER NOT NULL, Z INTEGER NOT NULL, WORLD VARCHAR(255) NOT NULL)";

    public SilkSpawnersShopAddonSQLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
    }

    @Override
    public boolean addShop(SilkSpawnersShop shop) {
        Location loc = shop.getLocation();
        String query = "INSERT INTO SHOPS VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getId().toString());
            statement.setString(2, shop.getMode().toString());
            statement.setString(3, shop.getMob());
            statement.setDouble(4, shop.getPrice());
            statement.setDouble(5, loc.getX());
            statement.setDouble(6, loc.getY());
            statement.setDouble(7, loc.getZ());
            statement.setString(8, loc.getWorld().getName());
            statement.executeUpdate();
            cachedShops.add(shop);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error adding the shop");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeShop(SilkSpawnersShop shop) {
        String query = "DELETE FROM SHOPS WHERE SHOPID = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getId().toString());
            int result = statement.executeUpdate();
            cachedShops.remove(shop);
            // Only one row should be affected
            return result == 1;
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error removing the shop");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateShop(SilkSpawnersShop shop) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isShop(Sign sign) {
        // Try to find in cache
        for (SilkSpawnersShop shop : cachedShops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return true;
            }
        }

        Location loc = sign.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        String world = loc.getWorld().getName();

        String query = "SELECT * FROM SHOPS WHERE X = ? AND Y = ? AND Z = ? AND WORLD = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDouble(1, x);
            statement.setDouble(2, y);
            statement.setDouble(3, z);
            statement.setString(4, world);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            } catch (SQLException e) {
                plugin.getLogger().severe("There was an error searching for the shop");
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error searching for the shop");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SilkSpawnersShop getShop(Sign sign) {
        // Try to find in cache
        for (SilkSpawnersShop shop : cachedShops) {
            if (shop.getLocation().equals(sign.getLocation())) {
                System.out.println("found in cache");
                return shop;
            }
        }

        Location loc = sign.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        String world = loc.getWorld().getName();

        String query = "SELECT * FROM SHOPS WHERE X = ? AND Y = ? AND Z = ? AND WORLD = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDouble(1, x);
            statement.setDouble(2, y);
            statement.setDouble(3, z);
            statement.setString(4, world);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String shopId = rs.getString("shopID");
                    SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(rs.getString("mode"));
                    String mob = rs.getString("mob");
                    double price = rs.getDouble("price");
                    x = rs.getInt("x");
                    y = rs.getInt("y");
                    z = rs.getInt("z");
                    world = rs.getString("world");
                    loc = new Location(plugin.getServer().getWorld(world), x, y, z);
                    SilkSpawnersShop shop = new SilkSpawnersShop(loc, mode, mob, price, shopId);
                    cachedShops.add(shop);
                    return shop;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("There was an error getting the shop");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error getting the shop");
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an issue closing the SQL connection");
            e.printStackTrace();
        }
        super.disable();
    }
}
