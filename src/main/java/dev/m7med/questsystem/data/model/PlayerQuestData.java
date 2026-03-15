package dev.m7med.questsystem.data.model;

import org.bson.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerQuestData {
    private final UUID uuid;
    private final Map<String, QuestProgress> activeQuests;
    private final Map<String, Long> completedQuests;
    public PlayerQuestData(UUID uuid) {
        this.uuid = uuid;
        this.activeQuests = new HashMap<>();
        this.completedQuests = new HashMap<>();
    }

    public UUID getUuid() {
        return uuid;
    }


    public Map<String, QuestProgress> getActiveQuests() {
        return activeQuests;
    }


    public Map<String, Long> getCompletedQuests() {
        return completedQuests;
    }
    public boolean hasCompleted(String questId) {
        return completedQuests.containsKey(questId);
    }
    public void completeQuest(String questId) {
        activeQuests.remove(questId);
        completedQuests.put(questId, System.currentTimeMillis());
    }
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
