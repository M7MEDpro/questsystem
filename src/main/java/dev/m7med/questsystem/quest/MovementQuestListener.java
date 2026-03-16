package dev.m7med.questsystem.quest;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MovementQuestListener implements Listener {

    private static final long BIOME_THROTTLE_MS = 1_000L;

    private final QuestManager questManager;
    private final PlayerDataManager dataManager;
    private final Map<UUID, Long> biomeCheckTimestamps = new ConcurrentHashMap<>();

    public MovementQuestListener(Questsystem plugin, QuestManager questManager, PlayerDataManager dataManager) {
        this.questManager = questManager;
        this.dataManager = dataManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Player player = event.getPlayer();
        if (dataManager.isLoading(player.getUniqueId())) return;

        PlayerQuestData data = dataManager.get(player);
        if (data == null) return;

        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();

        if (fromWorld != null && fromWorld.equals(toWorld)) {
            handleRunning(player, data, from.distance(to));
        }

        if ((to.getBlockX() != from.getBlockX() || to.getBlockZ() != from.getBlockZ())
                && throttled(player.getUniqueId())) {
            handleExploring(player, data, to);
        }
    }

    private void handleRunning(Player player, PlayerQuestData data, double dist) {
        List<Quest> quests = questManager.getByType(QuestType.RUNNING);
        for (Quest q : quests) {
            if (data.hasCompleted(q.getId())) continue;

            var prog = data.getOrCreateProgress(q.getId());
            if (prog.getProgress() >= q.getRequiredAmount()) continue;

            prog.addProgress(dist);
            if (prog.getProgress() >= q.getRequiredAmount()) {
                data.completeQuest(q.getId());
                dispatchRewards(player, q);
            }
        }
    }

    private void handleExploring(Player player, PlayerQuestData data, Location loc) {
        if (loc.getWorld() == null) return;

        String biome = loc.getBlock().getBiome().getKey().getKey().toUpperCase();
        String world = loc.getWorld().getName();

        List<Quest> quests = questManager.getByType(QuestType.EXPLORING);
        for (Quest q : quests) {
            String target = q.getTarget();
            if (!target.equalsIgnoreCase(biome) &&
                    !target.equalsIgnoreCase(world)) continue;
            if (data.hasCompleted(q.getId())) continue;

            var prog = data.getOrCreateProgress(q.getId());
            if (prog.getProgress() >= q.getRequiredAmount()) continue;

            prog.addProgress(1.0);
            if (prog.getProgress() >= q.getRequiredAmount()) {
                data.completeQuest(q.getId());
                dispatchRewards(player, q);
            }
        }
    }

    private boolean throttled(UUID uuid) {
        long now = System.currentTimeMillis();
        Long last = biomeCheckTimestamps.get(uuid);
        if (last != null && now - last < BIOME_THROTTLE_MS) return false;
        biomeCheckTimestamps.put(uuid, now);
        return true;
    }

    private void dispatchRewards(Player player, Quest quest) {
        quest.getRewardCommands().forEach(cmd ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("%player%", player.getName())));
    }
}