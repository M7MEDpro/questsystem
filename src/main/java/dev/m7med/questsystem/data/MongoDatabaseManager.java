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
public class MongoDatabaseManager {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> playerCollection;
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
    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            Questsystem.getInstance().getLogger().info("Disconnected from MongoDB.");
        }
    }
    public CompletableFuture<PlayerQuestData> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document doc = playerCollection.find(Filters.eq("_id", uuid.toString())).first();
            if (doc == null) {
                return new PlayerQuestData(uuid);
            }
            return PlayerQuestData.fromDocument(doc);
        });
    }
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
