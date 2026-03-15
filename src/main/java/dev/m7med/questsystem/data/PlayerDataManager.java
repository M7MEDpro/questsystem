package dev.m7med.questsystem.data;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the caching of player quest data in memory.
 * Listens to player join and quit events to load and save data efficiently.
 */
public class PlayerDataManager implements Listener {

    private final MongoDatabaseManager mongoManager;
    private final Map<UUID, PlayerQuestData> cache;

    /**
     * Constructs a new PlayerDataManager and registers its listeners.
     *
     * @param mongoManager The MongoDB manager instance used for data retrieval and
     *                     storage.
     */
    public PlayerDataManager(MongoDatabaseManager mongoManager) {
        this.mongoManager = mongoManager;
        this.cache = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(this, Questsystem.getInstance());
    }

    /**
     * Retrieves the cached quest data for a specific UUID.
     *
     * @param uuid The UUID of the player.
     * @return The PlayerQuestData, or null if not currently cached.
     */
    public PlayerQuestData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Retrieves the cached quest data for a specific Player object.
     *
     * @param player The online Player.
     * @return The PlayerQuestData, or null if not currently cached.
     */
    public PlayerQuestData getPlayerData(Player player) {
        return cache.get(player.getUniqueId());
    }

    /**
     * Saves all currently cached player data to the database synchronously.
     * Intended for usage during server shutdown.
     */
    public void saveAll() {
        for (PlayerQuestData data : cache.values()) {
            mongoManager.savePlayerData(data).join(); // join on shutdown to ensure it saves
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        mongoManager.loadPlayerData(player.getUniqueId()).thenAccept(data -> {
            cache.put(player.getUniqueId(), data);
        }).exceptionally(ex -> {
            Questsystem.getInstance().getLogger().severe("Failed to load data for " + player.getName());
            ex.printStackTrace();
            return null;
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerQuestData data = cache.remove(uuid);

        if (data != null) {
            mongoManager.savePlayerData(data);
        }
    }
}
