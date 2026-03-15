package dev.m7med.questsystem.quest;

import java.util.List;

public class Quest {

    private final String id;
    private final QuestType type;
    private final String target;
    private final double requiredAmount;
    private final List<String> commandsToExecute;
    public Quest(String id, QuestType type, String target, double requiredAmount, List<String> commandsToExecute) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.requiredAmount = requiredAmount;
        this.commandsToExecute = commandsToExecute;
    }
    public String getId() {
        return id;
    }
    public QuestType getType() {
        return type;
    }
    public String getTarget() {
        return target;
    }
    public double getRequiredAmount() {
        return requiredAmount;
    }
    public List<String> getCommandsToExecute() {
        return commandsToExecute;
    }
}
