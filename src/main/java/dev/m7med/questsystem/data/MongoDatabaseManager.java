package dev.m7med.questsystem.data;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class MongoDatabaseManager {
    private static final ReplaceOptions UPSERT_RULE = new ReplaceOptions().upsert(true);

    private final Logger logger = Logger.getLogger("QuestSystem");
    private final ExecutorService pool = Executors.newFixedThreadPool(2,
            r -> { Thread t = new Thread(r, "quest-io-thread"); t.setDaemon(true); return t; });

    private MongoClient client;
    private MongoCollection<Document> coll;

    public void connect(String uri, String dbName) {
        try {
            client = MongoClients.create(uri);
            coll = client.getDatabase(dbName).getCollection("player_quests");
            logger.info("Connected to Mongo!");
        } catch (Exception e) {
            logger.severe("Mongo connection error: " + e.getMessage());
        }
    }

    public void disconnect() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS))
                pool.shutdownNow();
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (client != null) client.close();
    }

    public CompletableFuture<PlayerQuestData> load(UUID uuid) {
        if (coll == null) return CompletableFuture.completedFuture(new PlayerQuestData(uuid));
        return CompletableFuture.supplyAsync(() -> {
            Document doc = coll.find(Filters.eq("_id", uuid.toString())).first();
            return doc == null ? new PlayerQuestData(uuid) : PlayerQuestData.fromDocument(doc);
        }, pool);
    }

    public CompletableFuture<Void> save(PlayerQuestData data) {
        if (coll == null) return CompletableFuture.completedFuture(null);
        return CompletableFuture.runAsync(() ->
                        coll.replaceOne(
                                Filters.eq("_id", data.getUuid().toString()),
                                data.toDocument(),
                                UPSERT_RULE),
                pool);
    }

    public boolean isConnected() {
        return coll != null;
    }
}