package dev.m7med.questsystem.quest;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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

            QuestProgress progress = data.getOrCreateProgress(quest.getId());
            if (progress.getProgress() >= quest.getRequiredAmount()) continue;

            boolean wasBeforeHalf = progress.getProgress() < quest.getRequiredAmount() / 2.0;
            progress.addProgress(amount);
            boolean isNowHalfOrMore = progress.getProgress() >= quest.getRequiredAmount() / 2.0;

            if (wasBeforeHalf && isNowHalfOrMore && progress.getProgress() < quest.getRequiredAmount()) {
                sendHalfwayNotification(player, quest);
            }

            if (progress.getProgress() >= quest.getRequiredAmount()) {
                data.completeQuest(quest.getId());
                sendCompletionNotification(player, quest);
                dispatchRewards(player, quest);
            }
        }
    }

    public void awardExact(Player player, String questId, double newValue) {
        if (dataManager.isLoading(player.getUniqueId())) return;

        PlayerQuestData data = dataManager.get(player);
        if (data == null) return;

        Quest quest = questManager.getById(questId);
        if (quest == null) return;

        data.getCompletedQuests().remove(questId);
        QuestProgress progress = data.getOrCreateProgress(questId);
        progress.setProgress(0);

        boolean wasBeforeHalf = true;
        progress.addProgress(newValue);
        boolean isNowHalfOrMore = progress.getProgress() >= quest.getRequiredAmount() / 2.0;

        if (wasBeforeHalf && isNowHalfOrMore && progress.getProgress() < quest.getRequiredAmount()) {
            sendHalfwayNotification(player, quest);
        }

        if (progress.getProgress() >= quest.getRequiredAmount()) {
            data.completeQuest(questId);
            sendCompletionNotification(player, quest);
            dispatchRewards(player, quest);
        }
    }

    private void sendCompletionNotification(Player player, Quest quest) {
        List<String> lines = plugin.getConfig().getStringList("messages.quest_completed");
        for (String line : lines) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    line.replace("%quest%", quest.getDisplayName())));
        }

        if (plugin.getConfig().getBoolean("sounds.quest_completed.enabled", true)) {
            playSound(player, "sounds.quest_completed");
        }
    }

    private void sendHalfwayNotification(Player player, Quest quest) {
        List<String> lines = plugin.getConfig().getStringList("messages.quest_halfway");
        for (String line : lines) {
            player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    line.replace("%quest%", quest.getDisplayName())));
        }

        if (plugin.getConfig().getBoolean("sounds.quest_halfway.enabled", true)) {
            playSound(player, "sounds.quest_halfway");
        }
    }

    private void playSound(Player player, String path) {
        String soundName = plugin.getConfig().getString(path + ".sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) plugin.getConfig().getDouble(path + ".volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble(path + ".pitch", 1.0);
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void dispatchRewards(Player player, Quest quest) {
        quest.getRewardCommands().forEach(cmd -> {
            String parsed = cmd.replace("%player%", player.getName());

            if (parsed.toLowerCase().startsWith("tell ") || parsed.toLowerCase().startsWith("msg ")) {
                String message = parsed.substring(parsed.indexOf(" ") + 1);
                player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
        });
    }

    private NamespacedKey keyFor(Block block) {
        Location l = block.getLocation();
        return new NamespacedKey(plugin, "pp_" + l.getBlockX() + "_" + l.getBlockY() + "_" + l.getBlockZ());
    }
}