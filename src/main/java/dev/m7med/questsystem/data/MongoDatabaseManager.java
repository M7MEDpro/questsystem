package dev.m7med.questsystem.data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the MongoDB connection and executes asynchronous database operations.
 */
public class MongoDatabaseManager {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> playerCollection;

    /**
     * Connects to the MongoDB server using the provided URI and database name.
     *
     * @param uri    The MongoDB connection string URI.
     * @param dbName The name of the database.
     */
    public void connect(String uri, String dbName) {
        try {
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName);
            playerCollection = database.getCollection("player_quests");
            Questsystem.getInstance().getLogger().info("Successfully connected to MongoDB.");
        } catch (Exception e) {
            Questsystem.getInstance().getLogger().severe("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    /**
     * Disconnects from the MongoDB server, closing all connections.
     */
    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            Questsystem.getInstance().getLogger().info("Disconnected from MongoDB.");
        }
    }

    /**
     * Asynchronously loads player quest data from the database.
     * If no existing data is found, a new empty PlayerQuestData instance is
     * returned.
     *
     * @param uuid The UUID of the player.
     * @return A CompletableFuture containing the player's quest data.
     */
    public CompletableFuture<PlayerQuestData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document doc = playerCollection.find(Filters.eq("_id", uuid.toString())).first();
            if (doc == null) {
                return new PlayerQuestData(uuid);
            }
            return PlayerQuestData.fromDocument(doc);
        });
    }

    /**
     * Asynchronously saves player quest data to the database using an upsert
     * operation.
     *
     * @param data The PlayerQuestData instance to save.
     * @return A CompletableFuture that completes when the save operation is
     *         finished.
     */
    public CompletableFuture<Void> savePlayerData(PlayerQuestData data) {
        return CompletableFuture.runAsync(() -> {
            Document doc = data.toDocument();
            playerCollection.replaceOne(
                    Filters.eq("_id", data.getUuid().toString()),
                    doc,
                    new ReplaceOptions().upsert(true));
        });
    }
}
