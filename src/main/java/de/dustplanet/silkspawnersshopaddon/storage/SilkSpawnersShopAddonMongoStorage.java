package de.dustplanet.silkspawnersshopaddon.storage;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.ISilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;

/**
 * The MongoDB implementation of the storage class.
 *
 * @author timbru31
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class SilkSpawnersShopAddonMongoStorage extends SilkSpawnersShopAddonStorageCleanupTaskTimer
        implements ISilkSpawnersShopAddonStorage {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    @SuppressFBWarnings({ "IMC_IMMATURE_CLASS_NO_TOSTRING", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE" })
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public SilkSpawnersShopAddonMongoStorage(final SilkSpawnersShopAddon plugin) {
        super(plugin);
        getPlugin().getLogger().info("Loading mongo storage provider");
        final String host = getPlugin().getConfig().getString("mongoDB.host");
        final int port = getPlugin().getConfig().getInt("mongoDB.port");
        final String user = getPlugin().getConfig().getString("mongoDB.user");
        final String pass = getPlugin().getConfig().getString("mongoDB.pass");
        final String databaseName = getPlugin().getConfig().getString("mongoDB.database");
        final String coll = getPlugin().getConfig().getString("mongoDB.collection");
        String userPass = "";
        if (user != null && pass != null && !user.isEmpty() && !pass.isEmpty()) {
            userPass = user + ":" + pass + "@";
        }
        final MongoClientSettings.Builder mongoClientSettings = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                .writeConcern(WriteConcern.ACKNOWLEDGED);
        if (user != null && pass != null) {
            final MongoCredential credential = MongoCredential.createCredential(user, databaseName, pass.toCharArray());
            mongoClientSettings.credential(credential);
        }
        mongoClient = MongoClients.create(mongoClientSettings.build());
        final MongoDatabase database = mongoClient.getDatabase(databaseName);
        collection = database.getCollection(coll);
    }

    @SuppressWarnings({ "static-method", "PMD.AvoidDuplicateLiterals", "checkstyle:SeparatorWrap" })
    @Nullable
    private Document createDocumentFromShop(final ISilkSpawnersShop shop) {
        final Location shopLoc = shop.getLocation();
        final World world = shopLoc.getWorld();
        if (world == null) {
            return null;
        }
        final String worldName = world.getName();
        final Document loc = new Document("world", worldName);
        loc.append("x", shopLoc.getX()).append("y", shopLoc.getY()).append("z", shopLoc.getZ());
        return new Document("shopId", shop.getId().toString())
                .append("mode", shop.getMode().toString())
                .append("mob", shop.getMob())
                .append("amount", shop.getAmount())
                .append("price", shop.getPrice())
                .append("location", loc);
    }

    @SuppressWarnings({ "PMD.ShortVariable", "static-method" })
    private SilkSpawnersShop getShopFromDocument(final Document doc) {
        final Document location = (Document) doc.get("location");
        final String world = location.getString("world");
        final double x = location.getDouble("x");
        final double y = location.getDouble("y");
        final double z = location.getDouble("z");
        final String shopId = doc.getString("shopId");
        final String mob = doc.getString("mob");
        final double price = doc.getDouble("price");
        final SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(doc.getString("mode"));
        final int amount = doc.getInteger("amount", 1);
        return new SilkSpawnersShop(x, y, z, world, mode, mob, amount, price, UUID.fromString(shopId));
    }

    @Override
    public boolean addShop(final SilkSpawnersShop shop) {
        final Document doc = createDocumentFromShop(shop);
        if (doc == null) {
            return false;
        }
        try {
            collection.insertOne(doc);
            getCachedShops().add(shop);
            return true;
        } catch (MongoWriteException | MongoException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to add shop to MongoDB", e);
        }
        return false;
    }

    @Override
    public boolean removeShop(final SilkSpawnersShop shop) {
        try {
            collection.deleteOne(Filters.eq("shopId", shop.getId().toString()));
            getCachedShops().remove(shop);
            return true;
        } catch (MongoWriteException | MongoException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to remove shop from MongoDB", e);
        }
        return false;
    }

    @Override
    public boolean removeShops(final List<SilkSpawnersShop> shopList) {
        try {
            final List<String> shopIdList = new ArrayList<>(shopList.size());
            getCachedShops().removeAll(shopList);
            for (final SilkSpawnersShop shop : shopList) {
                shopIdList.add(shop.getId().toString());
            }
            collection.deleteMany(Filters.in("shopId", shopIdList));
            return true;
        } catch (MongoWriteException | MongoException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Failed to remove shops from MongoDB", e);
        }
        return false;
    }

    @Override
    public boolean updateShop(final SilkSpawnersShop shop) {
        final Document doc = createDocumentFromShop(shop);
        collection.replaceOne(Filters.eq("shopId", shop.getId().toString()), doc);
        final int index = getCachedShops().indexOf(shop);
        if (index != -1) {
            getCachedShops().set(index, shop);
        }
        return true;
    }

    @Override
    @SuppressWarnings({ "PMD.ShortVariable", "checkstyle:SeparatorWrap" })
    public boolean isShop(final Sign sign) {
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
        final Document doc = collection
                .find(Filters.and(Filters.eq("location.world", worldName), Filters.eq("location.x", x), Filters.eq("location.y", y), Filters.eq("location.z", z)))
                .first();
        return doc != null;
    }

    @Override
    @Nullable
    @SuppressWarnings({ "PMD.ShortVariable", "checkstyle:ReturnCount", "checkstyle:SeparatorWrap" })
    public SilkSpawnersShop getShop(final Sign sign) {
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

        final Document doc = collection
                .find(Filters.and(Filters.eq("location.world", worldName), Filters.eq("location.x", x), Filters.eq("location.y", y), Filters.eq("location.z", z)))
                .first();
        if (doc == null) {
            return null;
        }
        final SilkSpawnersShop shop = getShopFromDocument(doc);
        getCachedShops().add(shop);
        return shop;
    }

    @Override
    public List<SilkSpawnersShop> getAllShops() {
        final List<SilkSpawnersShop> shopList = new ArrayList<>();
        final FindIterable<Document> shopDocuments = collection.find();
        for (final Document doc : shopDocuments) {
            final SilkSpawnersShop shop = getShopFromDocument(doc);
            shopList.add(shop);
        }
        return shopList;
    }

    @Override
    public void disable() {
        mongoClient.close();
        super.disable();
    }

    @Override
    public boolean upgradeDatabase() {
        collection.updateMany(new Document(), new Document("$set", new Document("amount", 1)));
        return true;
    }
}
