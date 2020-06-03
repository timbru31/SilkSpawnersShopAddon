package de.dustplanet.silkspawnersshopaddon.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The abstract implementation of the SQL storage class.
 *
 * @author timbru31
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public abstract class AbstractSilkSpawnersShopAddonSQLStorage extends SilkSpawnersShopAddonStorageCleanupTaskTimer
        implements ISilkSpawnersShopAddonStorage {
    @SuppressWarnings("checkstyle:LineLength")
    protected static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS SHOPS(SHOPID VARCHAR(36) PRIMARY KEY, MODE VARCHAR(4) NOT NULL, MOB VARCHAR(255) NOT NULL, AMOUNT INTEGER DEFAULT 1 NOT NULL, PRICE NUMERIC(10,3) NOT NULL, X INTEGER NOT NULL, Y INTEGER NOT NULL, Z INTEGER NOT NULL, WORLD VARCHAR(255) NOT NULL)";
    @SuppressWarnings("checkstyle:VisibilityModifier")
    protected Connection conn;

    @SuppressFBWarnings({ "CD_CIRCULAR_DEPENDENCY", "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY" })
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public AbstractSilkSpawnersShopAddonSQLStorage(final SilkSpawnersShopAddon plugin) {
        super(plugin);
    }

    @Override
    @SuppressFBWarnings({ "EXS_EXCEPTION_SOFTENING_RETURN_FALSE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" })
    @SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "checkstyle:MagicNumber", "PMD.DataflowAnomalyAnalysis" })
    public boolean addShop(final SilkSpawnersShop shop) {
        final Location loc = shop.getLocation();
        final World world = loc.getWorld();
        if (world == null) {
            return false;
        }
        final String worldName = world.getName();
        final String query = "INSERT INTO SHOPS VALUES(?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getId().toString());
            statement.setString(2, shop.getMode().toString());
            statement.setString(3, shop.getMob());
            statement.setInt(4, shop.getAmount());
            statement.setDouble(5, shop.getPrice());
            statement.setDouble(6, loc.getX());
            statement.setDouble(7, loc.getY());
            statement.setDouble(8, loc.getZ());
            statement.setString(9, worldName);
            statement.executeUpdate();
            getCachedShops().add(shop);
            return true;
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error adding the shop", e);
            return false;
        }
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "PMD.AvoidDuplicateLiterals" })
    public boolean removeShop(final SilkSpawnersShop shop) {
        final String query = "DELETE FROM SHOPS WHERE SHOPID = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getId().toString());
            final int result = statement.executeUpdate();
            getCachedShops().remove(shop);
            // Only one row should be affected
            return result == 1;
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error removing the shop", e);
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "checkstyle:LineLength" })
    @SuppressFBWarnings(value = { "DLS_DEAD_LOCAL_STORE", "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" }, justification = "False positive and builder is only used in for loop to calculate size of shops to remove")
    public boolean removeShops(final List<SilkSpawnersShop> shopList) {
        final StringBuilder builder = new StringBuilder();
        IntStream.range(0, shopList.size()).forEach(shopIndex -> builder.append("?,"));
        final String query = "DELETE FROM SHOPS WHERE SHOPID IN (" + builder.deleteCharAt(builder.length() - 1).toString() + ")";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            int index = 1;
            for (final SilkSpawnersShop shop : shopList) {
                statement.setObject(index++, shop.getId().toString());
            }
            final int result = statement.executeUpdate();
            getCachedShops().removeAll(shopList);
            // Only one row should be affected
            return result == shopList.size();
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error removing the shops", e);
        }
        return false;
    }

    @Override
    @SuppressFBWarnings({ "EXS_EXCEPTION_SOFTENING_RETURN_FALSE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" })
    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "checkstyle:MagicNumber" })
    public boolean updateShop(final SilkSpawnersShop shop) {
        final String query = "UPDATE SHOPS SET MODE = ?, MOB = ?, PRICE = ?, AMOUNT = ? WHERE SHOPID = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shop.getMode().toString());
            statement.setString(2, shop.getMob());
            statement.setDouble(3, shop.getPrice());
            statement.setInt(4, shop.getAmount());
            statement.setString(5, shop.getId().toString());
            statement.executeUpdate();
            final int index = getCachedShops().indexOf(shop);
            if (index != -1) {
                getCachedShops().set(index, shop);
            }
            return true;
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error updating the shop", e);
            return false;
        }
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @SuppressWarnings({ "PMD.DataflowAnomalyAnalysis", "checkstyle:MagicNumber", "PMD.ShortVariable", "checkstyle:ReturnCount" })
    public boolean isShop(final Sign sign) {
        // Try to find in cache
        for (final SilkSpawnersShop shop : getCachedShops()) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return true;
            }
        }

        final Location loc = sign.getLocation();
        final World world = loc.getWorld();
        if (world == null) {
            return false;
        }
        final double x = loc.getX();
        final double y = loc.getY();
        final double z = loc.getZ();
        final String worldName = world.getName();

        final String query = "SELECT * FROM SHOPS WHERE X = ? AND Y = ? AND Z = ? AND WORLD = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDouble(1, x);
            statement.setDouble(2, y);
            statement.setDouble(3, z);
            statement.setString(4, worldName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            } catch (final SQLException e) {
                getPlugin().getLogger().log(Level.SEVERE, "There was an error searching for the shop", e);
            }
            return false;
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error searching for the shop", e);
        }
        return false;
    }

    @Override
    @Nullable
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @SuppressWarnings({ "PMD.ShortVariable", "checkstyle:ReturnCount", "PMD.AvoidBranchingStatementAsLastInLoop",
            "PMD.DataflowAnomalyAnalysis", "checkstyle:MagicNumber" })
    public SilkSpawnersShop getShop(final Sign sign) {
        // Try to find in cache
        for (final SilkSpawnersShop shop : getCachedShops()) {
            if (shop.getLocation().equals(sign.getLocation())) {
                return shop;
            }
        }

        final Location loc = sign.getLocation();
        final World world = loc.getWorld();
        if (world == null) {
            return null;
        }

        final double x = loc.getX();
        final double y = loc.getY();
        final double z = loc.getZ();
        final String worldName = world.getName();

        final String query = "SELECT * FROM SHOPS WHERE X = ? AND Y = ? AND Z = ? AND WORLD = ?";
        SilkSpawnersShop shop = null;
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setDouble(1, x);
            statement.setDouble(2, y);
            statement.setDouble(3, z);
            statement.setString(4, worldName);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    shop = getShopFromResultSet(rs);
                    getCachedShops().add(shop);
                    break;
                }
            } catch (final SQLException e) {
                getPlugin().getLogger().log(Level.SEVERE, "There was an error getting the shop", e);
            }
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error getting the shop", e);
            return shop;
        }
        return shop;
    }

    @Override
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<SilkSpawnersShop> getAllShops() {
        final List<SilkSpawnersShop> shopList = new ArrayList<>();
        final String query = "SELECT * FROM SHOPS";
        try (PreparedStatement statement = conn.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                final SilkSpawnersShop shop = getShopFromResultSet(resultSet);
                shopList.add(shop);
            }
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an error getting the shop list", e);
            return null;
        }
        return shopList;
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "There was an issue closing the SQL connection", e);
        }
        super.disable();
    }

    @SuppressWarnings({ "PMD.ShortVariable", "static-method" })
    private SilkSpawnersShop getShopFromResultSet(final ResultSet resultSet) throws SQLException {
        final String shopId = resultSet.getString("shopID");
        final SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(resultSet.getString("mode"));
        final String mob = resultSet.getString("mob");
        final double price = resultSet.getDouble("price");
        final double x = resultSet.getInt("x");
        final double y = resultSet.getInt("y");
        final double z = resultSet.getInt("z");
        final String world = resultSet.getString("world");
        final int amount = resultSet.getInt("amount");
        return new SilkSpawnersShop(x, y, z, world, mode, mob, amount, price, shopId);
    }

    @Override
    @SuppressFBWarnings({ "EXS_EXCEPTION_SOFTENING_RETURN_FALSE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE" })
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public boolean upgradeDatabase() {
        final String query = "ALTER TABLE SHOPS ADD AMOUNT INTEGER DEFAULT 1 AFTER MOB";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Unable to upgrade the SQL database!", e);
            return false;
        }
    }
}
