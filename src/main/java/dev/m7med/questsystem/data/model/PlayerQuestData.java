package dev.m7med.questsystem.data.model;

import org.bson.Document;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerQuestData {
    private final UUID uuid;
    private final Map<String, QuestProgress> activeQuests = new ConcurrentHashMap<>();
    private final Map<String, Long> completedQuests = new ConcurrentHashMap<>();


    public PlayerQuestData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
    public Map<String, QuestProgress> getActiveQuests() {
        return activeQuests; }

    public Map<String, Long> getCompletedQuests() { return completedQuests; }

    public boolean hasCompleted(String questId) {
        return completedQuests.containsKey(questId);
    }


    public void completeQuest(String questId) {
        activeQuests.remove(questId);
        completedQuests.put(questId, System.currentTimeMillis());
    }


    public QuestProgress getOrCreateProgress(String questId) {
        return activeQuests.computeIfAbsent(questId, k -> new QuestProgress(0.0));
    }

    public Document toDocument() {
        Document activeDoc = new Document();
        activeQuests.forEach((k, v) -> activeDoc.put(k, v.toDocument()));

        Document completedDoc = new Document();
        completedQuests.forEach(completedDoc::put);

        return new Document("_id", uuid.toString())
                .append("activeQuests", activeDoc)
                .append("completedQuests", completedDoc);
    }

    public static PlayerQuestData fromDocument(Document doc) {
        PlayerQuestData data = new PlayerQuestData(UUID.fromString(doc.getString("_id")));

        Document activeDoc = doc.get("activeQuests", Document.class);
        if (activeDoc != null) {
            activeDoc.keySet().forEach(k ->
                    data.activeQuests.put(k, QuestProgress.fromDocument(activeDoc.get(k, Document.class))));
        }

        Document completedDoc = doc.get("completedQuests", Document.class);
        if (completedDoc != null) {
            completedDoc.keySet().forEach(k -> data.completedQuests.put(k, completedDoc.getLong(k)));
        }

        return data;
    }
}