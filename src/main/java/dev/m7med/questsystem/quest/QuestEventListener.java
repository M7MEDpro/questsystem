package dev.m7med.questsystem.quest;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collection;
public class QuestEventListener implements Listener {
    private final Questsystem plugin;
    private final QuestManager questManager;
    private final PlayerDataManager dataManager;
    public QuestEventListener(Questsystem plugin, QuestManager questManager, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.dataManager = dataManager;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockName = event.getBlock().getType().name();

        processProgress(player, QuestType.MINING, blockName, 1.0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        String entityName = event.getEntity().getType().name();
        processProgress(killer, QuestType.KILLING, entityName, 1.0);
    }
    private void processProgress(Player player, QuestType type, String target, double amount) {
        PlayerQuestData data = dataManager.getPlayerData(player);
        if (data == null)
            return;

        Collection<Quest> possibleQuests = questManager.getQuestsByType(type);

        for (Quest quest : possibleQuests) {
            if (quest.getTarget().equalsIgnoreCase(target)) {

                if (data.getActiveQuests().containsKey(quest.getId())) {
                    QuestProgress progress = data.getActiveQuests().get(quest.getId());

                    if (progress.getProgress() >= quest.getRequiredAmount()) {
                        continue;
                    }
                    progress.addProgress(amount);

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
