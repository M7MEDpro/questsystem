package dev.m7med.questsystem.quest;

import org.bukkit.Material;

import java.util.List;

public class Quest {

    private final String id;
    private final QuestType type;
    private final String target;
    private final double requiredAmount;
    private final List<String> rewardCommands;
    private final Material displayMaterial;
    private final String displayName;
    private final List<String> lore;
    private final List<String> itemFlags;
    private final int customModelData;
    private final boolean glow;

    public Quest(String id, QuestType type, String target, double requiredAmount,
                 List<String> rewardCommands,
                 Material displayMaterial, String displayName,
                 List<String> lore, List<String> itemFlags,
                 int customModelData, boolean glow) {

        this.id = id;
        this.type = type;
        this.target = target;
        this.requiredAmount = requiredAmount;
        this.rewardCommands = rewardCommands;
        this.displayMaterial = displayMaterial;
        this.displayName = displayName;
        this.lore = lore;
        this.itemFlags = itemFlags;
        this.customModelData = customModelData;
        this.glow = glow;
    }

    public String getId() { return id; }
    public QuestType getType() { return type; }
    public String getTarget() { return target; }
    public double getRequiredAmount() { return requiredAmount; }
    public List<String> getRewardCommands() { return rewardCommands; }
    public Material getDisplayMaterial() { return displayMaterial; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public List<String> getItemFlags() { return itemFlags; }
    public int getCustomModelData() { return customModelData; }
    public boolean isGlow() { return glow; }
}