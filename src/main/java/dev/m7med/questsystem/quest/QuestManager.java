package dev.m7med.questsystem.quest;

import java.util.*;

public class QuestManager {

    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private final Map<QuestType, List<Quest>> byType = new EnumMap<>(QuestType.class);
    public void register(Quest quest) {
        quests.put(quest.getId(), quest);
        byType.computeIfAbsent(quest.getType(),
                t -> new ArrayList<>()).add(quest);
    }

    public Quest getById(String id) {
        return quests.get(id);
    }

    public List<Quest> getByType(QuestType type) {
        return byType.getOrDefault(type, Collections.emptyList());
    }

    public Collection<Quest> getAll() {
        return quests.values();
    }

    public void clear() {
        quests.clear();
        byType.clear();
    }
}