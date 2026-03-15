package dev.m7med.questsystem.quest;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;

public class MovementQuestListener implements Listener {
    private final Questsystem plugin;
    private final QuestManager questManager;
    private final PlayerDataManager dataManager;
    public MovementQuestListener(Questsystem plugin, QuestManager questManager, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.dataManager = dataManager;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        Player player = event.getPlayer();
        PlayerQuestData data = dataManager.getPlayerData(player);
        if (data == null)
            return;

        double distanceTraveled = from.distance(to);

        processRunning(player, data, distanceTraveled);
        processExploring(player, data, to);
    }

    private void processRunning(Player player, PlayerQuestData data, double distance) {
        Collection<Quest> runningQuests = questManager.getQuestsByType(QuestType.RUNNING);

        for (Quest quest : runningQuests) {
            if (data.getActiveQuests().containsKey(quest.getId())) {
                QuestProgress progress = data.getActiveQuests().get(quest.getId());

                if (progress.getProgress() >= quest.getRequiredAmount())
                    continue;

                progress.addProgress(distance);

                if (progress.getProgress() >= quest.getRequiredAmount()) {
                    executeRewards(player, quest);
                    data.completeQuest(quest.getId());
                }
            }
        }
    }

    private void processExploring(Player player, PlayerQuestData data, Location to) {
        Collection<Quest> exploringQuests = questManager.getQuestsByType(QuestType.EXPLORING);

        String currentBiome = to.getBlock().getBiome().getKey().getKey().toUpperCase();
        String currentWorld = to.getWorld().getName();

        for (Quest quest : exploringQuests) {
            if (quest.getTarget().equalsIgnoreCase(currentBiome) || quest.getTarget().equalsIgnoreCase(currentWorld)) {

                if (data.getActiveQuests().containsKey(quest.getId())) {
                    QuestProgress progress = data.getActiveQuests().get(quest.getId());

                    if (progress.getProgress() >= quest.getRequiredAmount())
                        continue;

                    progress.addProgress(1.0);

                    if (progress.getProgress() >= quest.getRequiredAmount()) {
                        executeRewards(player, quest);
                        data.completeQuest(quest.getId());
                    }
                }
            }
        }
    }

    private void executeRewards(Player player, Quest quest) {
        for (String cmd : quest.getCommandsToExecute()) {
            String formattedCmd = cmd.replace("%player%", player.getName());
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            });
        }
    }
}
