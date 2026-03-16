package dev.m7med.questsystem.data;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class PlayerDataManager implements Listener {
    private final MongoDatabaseManager db;
    private final Map<UUID, PlayerQuestData> cache = new ConcurrentHashMap<>();
    private final Set<UUID> loadingState = ConcurrentHashMap.newKeySet();
    private final Logger log = Logger.getLogger("QuestSystem");

    public PlayerDataManager(Questsystem plugin, MongoDatabaseManager db) {
        this.db = db;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::autoSave, 6000L, 6000L);
    }

    public PlayerQuestData get(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerQuestData get(Player player) {
        return cache.get(player.getUniqueId());
    }

    public boolean isLoading(UUID uuid) {
        return loadingState.contains(uuid);
    }

    public void saveAll() {
        List<CompletableFuture<Void>> tasks = new ArrayList<>(cache.size());
        cache.values().forEach(d -> tasks.add(db.save(d)));
        try {
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.severe("Failed to save data on shutdown! " + e.getMessage());
        }
    }

    private void autoSave() {
        cache.values().forEach(db::save);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loadingState.add(uuid);

        db.load(uuid).whenComplete((data, err) -> {
            loadingState.remove(uuid);
            if (err != null) {
                log.severe("Could not load data for " + uuid);
                return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                cache.put(uuid, data);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loadingState.remove(uuid);
        PlayerQuestData data = cache.remove(uuid);
        if (data != null) db.save(data);
    }
}