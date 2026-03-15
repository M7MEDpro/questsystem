package dev.m7med.questsystem.data.model;

import org.bson.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the entire persistent quest data for a single player.
 * Tracks both their currently active quests and their completed quests.
 */
public class PlayerQuestData {

    /**
     * The UUID of the player this data belongs to.
     */
    private final UUID uuid;

    /**
     * A map of quest IDs to their active progress objects.
     */
    private final Map<String, QuestProgress> activeQuests;

    /**
     * A map of quest IDs to their completion timestamp (in milliseconds).
     */
    private final Map<String, Long> completedQuests;

    /**
     * Constructs a new empty PlayerQuestData for the given UUID.
     *
     * @param uuid The player's UUID.
     */
    public PlayerQuestData(UUID uuid) {
        this.uuid = uuid;
        this.activeQuests = new HashMap<>();
        this.completedQuests = new HashMap<>();
    }

    /**
     * Gets the UUID of the player.
     *
     * @return The player's UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the map of currently active quests for this player.
     *
     * @return A map of quest ID strings to QuestProgress objects.
     */
    public Map<String, QuestProgress> getActiveQuests() {
        return activeQuests;
    }

    /**
     * Gets the map of completed quests for this player.
     *
     * @return A map of quest ID strings to their completion timestamps (Epoch MS).
     */
    public Map<String, Long> getCompletedQuests() {
        return completedQuests;
    }

    /**
     * Checks if the player has completed a specific quest.
     *
     * @param questId The ID of the quest to check.
     * @return true if the quest was completed, false otherwise.
     */
    public boolean hasCompleted(String questId) {
        return completedQuests.containsKey(questId);
    }

    /**
     * Marks a quest as successfully completed.
     * This removes it from active quests and adds it to completed quests with the
     * current timestamp.
     *
     * @param questId The ID of the quest to complete.
     */
    public void completeQuest(String questId) {
        activeQuests.remove(questId);
        completedQuests.put(questId, System.currentTimeMillis());
    }

    /**
     * Serializes this player's quest data into a MongoDB Document.
     *
     * @return A Document representing the player's quest state.
     */
    public Document toDocument() {
        Document doc = new Document("_id", uuid.toString());

        Document activeDoc = new Document();
        for (Map.Entry<String, QuestProgress> entry : activeQuests.entrySet()) {
            activeDoc.put(entry.getKey(), entry.getValue().toDocument());
        }

        Document completedDoc = new Document();
        for (Map.Entry<String, Long> entry : completedQuests.entrySet()) {
            completedDoc.put(entry.getKey(), entry.getValue());
        }

        doc.put("activeQuests", activeDoc);
        doc.put("completedQuests", completedDoc);
        return doc;
    }

    /**
     * Deserializes a MongoDB Document into a PlayerQuestData instance.
     *
     * @param doc The Document to deserialize. Must contain an "_id" field.
     * @return A reconstructed PlayerQuestData instance.
     */
    public static PlayerQuestData fromDocument(Document doc) {
        UUID uuid = UUID.fromString(doc.getString("_id"));
        PlayerQuestData data = new PlayerQuestData(uuid);

        Document activeDoc = doc.get("activeQuests", Document.class);
        if (activeDoc != null) {
            for (String key : activeDoc.keySet()) {
                data.activeQuests.put(key, QuestProgress.fromDocument(activeDoc.get(key, Document.class)));
            }
        }

        Document completedDoc = doc.get("completedQuests", Document.class);
        if (completedDoc != null) {
            for (String key : completedDoc.keySet()) {
                data.completedQuests.put(key, completedDoc.getLong(key));
            }
        }

        return data;
    }
}
