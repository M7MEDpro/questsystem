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

public class PlayerDataManager implements Listener {
    private final MongoDatabaseManager mongoManager;
    private final Map<UUID, PlayerQuestData> cache;
    public PlayerDataManager(MongoDatabaseManager mongoManager) {
        this.mongoManager = mongoManager;
        this.cache = new ConcurrentHashMap<>();

        Bukkit.getPluginManager().registerEvents(this, Questsystem.getInstance());
    }
    public PlayerQuestData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }
    public PlayerQuestData getPlayerData(Player player) {
        return cache.get(player.getUniqueId());
    }
    public void saveAll() {
        for (PlayerQuestData data : cache.values()) {
            mongoManager.savePlayerData(data).join();
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
