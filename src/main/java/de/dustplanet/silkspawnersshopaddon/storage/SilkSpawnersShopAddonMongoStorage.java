package de.dustplanet.silkspawnersshopaddon.storage;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.dustplanet.silkspawnersshopaddon.SilkSpawnersShopAddon;
import de.dustplanet.silkspawnersshopaddon.shop.SilkSpawnersShop;
import de.dustplanet.silkspawnersshopaddon.shop.SilkspawnersShopMode;

public class SilkSpawnersShopAddonMongoStorage extends SilkSpawnersShopAddonStorageImpl implements ISilkSpawnersShopAddonStorage {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public SilkSpawnersShopAddonMongoStorage(SilkSpawnersShopAddon plugin) {
        super(plugin);
        plugin.getLogger().info("Loading mongo storage provider");
        String host = plugin.getConfig().getString("mongoDB.host");
        int port = plugin.getConfig().getInt("mongoDB.port");
        String user = plugin.getConfig().getString("mongoDB.user");
        String pass = plugin.getConfig().getString("mongoDB.pass");
        String db = plugin.getConfig().getString("mongoDB.database");
        String coll = plugin.getConfig().getString("mongoDB.collection");
        String userPass = "";
        if (user != null && pass != null && !user.isEmpty() && !pass.isEmpty()) {
            userPass = user + ":" + pass + "@";
        }
        Builder mongoClientOptions = MongoClientOptions.builder().writeConcern(WriteConcern.ACKNOWLEDGED);
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + userPass + host + ":" + port + "/" + db, mongoClientOptions);
        mongoClient = new MongoClient(connectionString);
        database = mongoClient.getDatabase(db);
        collection = database.getCollection(coll);
    }

    private Document createDocumentFromShop(SilkSpawnersShop shop) {
        Location shopLoc = shop.getLocation();
        Document loc = new Document("world", shopLoc.getWorld().getName());
        loc.append("x", shopLoc.getX()).append("y", shopLoc.getY()).append("z", shopLoc.getZ());
        Document doc = new Document("shopId", shop.getId().toString()).append("mode", shop.getMode().toString())
                .append("mob", shop.getMob()).append("amount", shop.getAmount()).append("price", shop.getPrice()).append("location", loc);
        return doc;
    }

    private SilkSpawnersShop getShopFromDocument(Document doc) {
        Document location = (Document) doc.get("location");
        String world = location.getString("world");
        double x = location.getDouble("x");
        double y = location.getDouble("y");
        double z = location.getDouble("z");
        String shopId = doc.getString("shopId");
        String mob = doc.getString("mob");
        double price = doc.getDouble("price");
        SilkspawnersShopMode mode = SilkspawnersShopMode.getMode(doc.getString("mode"));
        int amount = doc.getInteger("amount", 1);
        return new SilkSpawnersShop(x, y, z, world, mode, mob, amount, price, UUID.fromString(shopId));
    }

    @Override
    public boolean addShop(SilkSpawnersShop shop) {
        Document doc = createDocumentFromShop(shop);
        try {
            collection.insertOne(doc);
            cachedShops.add(shop);
            return true;
        } catch (MongoWriteException | MongoWriteConcernException e) {
            plugin.getLogger().severe("Failed to add shop to MongoDB");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeShop(SilkSpawnersShop shop) {
        try {
            collection.deleteOne(eq("shopId", shop.getId().toString()));
            cachedShops.remove(shop);
            return true;
        } catch (MongoWriteException | MongoWriteConcernException e) {
            plugin.getLogger().severe("Failed to remove shop from MongoDB");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeShops(List<SilkSpawnersShop> shopList) {
        try {
            List<String> shopIdList = new ArrayList<>();
            cachedShops.removeAll(shopList);
            for (SilkSpawnersShop shop : shopList) {
                shopIdList.add(shop.getId().toString());
            }
            collection.deleteMany(in("shopId", shopIdList));
            return true;
        } catch (MongoWriteException | MongoWriteConcernException e) {
            plugin.getLogger().severe("Failed to remove shops from MongoDB");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateShop(SilkSpawnersShop shop) {
        Document doc = createDocumentFromShop(shop);
        collection.replaceOne(eq("shopId", shop.getId().toString()), doc);
        int index = cachedShops.indexOf(shop);
        if (index != -1) {
            cachedShops.set(index, shop);
        }
        return true;
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
        Document doc = collection.find(and(eq("location.world", world), eq("location.x", x), eq("location.y", y), eq("location.z", z)))
                .first();
        return doc != null;
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
        Document doc = collection.find(and(eq("location.world", world), eq("location.x", x), eq("location.y", y), eq("location.z", z)))
                .first();
        if (doc == null) {
            return null;
        }
        SilkSpawnersShop shop = getShopFromDocument(doc);
        cachedShops.add(shop);
        return shop;
    }

    @Override
    public List<SilkSpawnersShop> getAllShops() {
        List<SilkSpawnersShop> shopList = new ArrayList<>();
        FindIterable<Document> shopDocuments = collection.find();
        for (Document doc : shopDocuments) {
            SilkSpawnersShop shop = getShopFromDocument(doc);
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
