package dev.m7med.questsystem.quest;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class QuestEventListener implements Listener {

    private final QuestManager questManager;
    private final PlayerDataManager dataManager;
    private final Questsystem plugin;

    public QuestEventListener(Questsystem plugin, QuestManager questManager, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.questManager = questManager;
        this.dataManager = dataManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        event.getBlock().getChunk().getPersistentDataContainer()
                .set(keyFor(event.getBlock()), PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        NamespacedKey key = keyFor(block);

        if (block.getChunk().getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
            block.getChunk().getPersistentDataContainer().remove(key);
            return;
        }

        award(event.getPlayer(), QuestType.MINING, block.getType().name(), 1.0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        award(event.getEntity().getKiller(), QuestType.KILLING, event.getEntity().getType().name(), 1.0);
    }

    void award(Player player, QuestType type, String target, double amount) {
        if (dataManager.isLoading(player.getUniqueId())) return;

        PlayerQuestData data = dataManager.get(player);
        if (data == null) return;

        List<Quest> quests = questManager.getByType(type);
        for (Quest quest : quests) {
            if (!quest.getTarget().equalsIgnoreCase(target)) continue;
            if (data.hasCompleted(quest.getId())) continue;

            var progress = data.getOrCreateProgress(quest.getId());
            if (progress.getProgress() >= quest.getRequiredAmount()) continue;

            progress.addProgress(amount);

            if (progress.getProgress() >= quest.getRequiredAmount()) {
                data.completeQuest(quest.getId());
                dispatchRewards(player, quest);
            }
        }
    }

    private void dispatchRewards(Player player, Quest quest) {
        quest.getRewardCommands().forEach(cmd ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("%player%", player.getName())));
    }

    private NamespacedKey keyFor(Block block) {
        Location l = block.getLocation();
        return new NamespacedKey(plugin, "pp_" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ());
    }
}