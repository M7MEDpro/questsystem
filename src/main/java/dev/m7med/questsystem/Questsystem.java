package dev.m7med.questsystem;

import dev.m7med.questsystem.data.MongoDatabaseManager;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.hooks.QuestsPlaceholderExpansion;
import dev.m7med.questsystem.quest.MovementQuestListener;
import dev.m7med.questsystem.quest.QuestEventListener;
import dev.m7med.questsystem.quest.QuestManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Questsystem extends JavaPlugin {

    private static Questsystem instance;
    private MongoDatabaseManager mongoDatabaseManager;
    private PlayerDataManager playerDataManager;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        mongoDatabaseManager = new MongoDatabaseManager();
        mongoDatabaseManager.connect(
                getConfig().getString("mongodb.uri", "mongodb://localhost:27017"),
                getConfig().getString("mongodb.database", "quest_system"));

        playerDataManager = new PlayerDataManager(mongoDatabaseManager);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestsPlaceholderExpansion(this, playerDataManager).register();
        }

        questManager = new QuestManager();
        new QuestEventListener(this, questManager, playerDataManager);
        new MovementQuestListener(this, questManager, playerDataManager);
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (mongoDatabaseManager != null) {
            mongoDatabaseManager.disconnect();
        }
    }

    public static Questsystem getInstance() {
        return instance;
    }
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    public MongoDatabaseManager getMongoDatabaseManager() {
        return mongoDatabaseManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }
}
