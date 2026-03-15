package dev.m7med.questsystem;

import dev.m7med.questsystem.data.MongoDatabaseManager;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.hooks.QuestsPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main plugin class for the Quest System.
 * Handles the initialization of the database, data managers, and external
 * hooks.
 */
public final class Questsystem extends JavaPlugin {

    private static Questsystem instance;
    private MongoDatabaseManager mongoDatabaseManager;
    private PlayerDataManager playerDataManager;

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

    /**
     * Retrieves the singleton instance of the plugin.
     *
     * @return The Questsystem plugin instance.
     */
    public static Questsystem getInstance() {
        return instance;
    }

    /**
     * Retrieves the manager responsible for caching and handling player quest data.
     *
     * @return The PlayerDataManager.
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * Retrieves the manager responsible for MongoDB database operations.
     *
     * @return The MongoDatabaseManager.
     */
    public MongoDatabaseManager getMongoDatabaseManager() {
        return mongoDatabaseManager;
    }
}
