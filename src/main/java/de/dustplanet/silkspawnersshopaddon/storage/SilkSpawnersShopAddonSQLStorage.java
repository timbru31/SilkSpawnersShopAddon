package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class SilkSpawnersShopAddonSQLStorage extends SilkSpawnersShopAddonStorageImpl implements ISilkSpawnersShopAddonStorage {
    protected Connection conn;
    protected static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS SHOPS(SHOPID VARCHAR(36) PRIMARY KEY, MODE VARCHAR(4) NOT NULL, MOB VARCHAR(255) NOT NULL, AMOUNT INTEGER DEFAULT 1 NOT NULL, PRICE NUMERIC(10,3) NOT NULL, X INTEGER NOT NULL, Y INTEGER NOT NULL, Z INTEGER NOT NULL, WORLD VARCHAR(255) NOT NULL)";

    public SilkSpawnersShopAddonSQLStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
    }

    @Override
    public boolean addShop(SilkSpawnersShop shop) {
        Location loc = shop.getLocation();
        String query = "INSERT INTO SHOPS VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getId().toString());
            statement.setString(2, shop.getMode().toString());
            statement.setString(3, shop.getMob());
            statement.setInt(4, shop.getAmount());
            statement.setDouble(5, shop.getPrice());
            statement.setDouble(6, loc.getX());
            statement.setDouble(7, loc.getY());
            statement.setDouble(8, loc.getZ());
            statement.setString(9, loc.getWorld().getName());
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
    public boolean removeShops(ArrayList<SilkSpawnersShop> shopList) {
        StringBuilder builder = new StringBuilder();
        for (@SuppressWarnings("unused")
        SilkSpawnersShop element : shopList) {
            builder.append("?,");
        }
        @SuppressFBWarnings(justification = "builder is only used in for loop to calculate size of shops to remove", value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
        String query = "DELETE FROM SHOPS WHERE SHOPID IN (" + builder.deleteCharAt(builder.length() - 1).toString() + ")";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            int index = 1;
            for (SilkSpawnersShop shop : shopList) {
                statement.setObject(index++, shop.getId().toString());
            }
            int result = statement.executeUpdate();
            cachedShops.removeAll(shopList);
            // Only one row should be affected
            return result == shopList.size();
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error removing the shops");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateShop(SilkSpawnersShop shop) {
        String query = "UPDATE SHOPS SET MODE = ?, MOB = ?, PRICE = ?, AMOUNT = ? WHERE SHOPID = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getMode().toString());
            statement.setString(2, shop.getMob());
            statement.setDouble(3, shop.getPrice());
            statement.setInt(4, shop.getAmount());
            statement.setString(5, shop.getId().toString());
            statement.executeUpdate();
            int index = cachedShops.indexOf(shop);
            if (index != -1) {
                cachedShops.set(index, shop);
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error updating the shop");
            e.printStackTrace();
            return false;
        }
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
                return shop;
            }
        }

        Location loc = sign.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        String world = loc.getWorld().getName();

        String query = "SELECT * FROM SHOPS WHERE X = ? AND Y = ? AND Z = ? AND WORLD = ?";
        SilkSpawnersShop shop = null;
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDouble(1, x);
            statement.setDouble(2, y);
            statement.setDouble(3, z);
            statement.setString(4, world);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    shop = getShopFromResultSet(rs);
                    cachedShops.add(shop);
                    break;
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("There was an error getting the shop");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error getting the shop");
            e.printStackTrace();
            return shop;
        }
        return shop;
    }

    @Override
    public ArrayList<SilkSpawnersShop> getAllShops() {
        ArrayList<SilkSpawnersShop> shopList = new ArrayList<>();
        String query = "SELECT * FROM SHOPS";
        try (PreparedStatement statement = conn.prepareStatement(query); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                SilkSpawnersShop shop = getShopFromResultSet(rs);
                shopList.add(shop);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("There was an error getting the shop list");
            e.printStackTrace();
            return null;
        }
        return shopList;
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

    private SilkSpawnersShop getShopFromResultSet(ResultSet rs) throws SQLException {
        String shopId = rs.getString("shopID");
        SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(rs.getString("mode"));
        String mob = rs.getString("mob");
        double price = rs.getDouble("price");
        double x = rs.getInt("x");
        double y = rs.getInt("y");
        double z = rs.getInt("z");
        String world = rs.getString("world");
        int amount = rs.getInt("amount");
        return new SilkSpawnersShop(x, y, z, world, mode, mob, amount, price, shopId);
    }

    @Override
    public boolean upgradeDatabase() {
        String query = "ALTER TABLE SHOPS ADD AMOUNT INTEGER DEFAULT 1 AFTER MOB";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.executeUpdate();
            return true;
        } catch (@SuppressWarnings("unused") SQLException e) {
            return false;
        }
    }
}
