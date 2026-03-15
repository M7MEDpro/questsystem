package dev.m7med.questsystem.quest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
public class QuestManager {
    private final Map<String, Quest> quests;
    public QuestManager() {
        this.quests = new HashMap<>();
    }

    public void registerQuest(Quest quest) {
        quests.put(quest.getId(), quest);
    }
    public Quest getQuest(String id) {
        return quests.get(id);
    }
    public Collection<Quest> getQuestsByType(QuestType type) {
        return quests.values().stream()
                .filter(q -> q.getType() == type)
                .toList();
    }
    public Collection<Quest> getAllQuests() {
        return quests.values();
    }
    public void clearQuests() {
        quests.clear();
    }
}
