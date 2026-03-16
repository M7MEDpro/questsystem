package dev.m7med.questsystem.hooks;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import dev.m7med.questsystem.quest.Quest;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestsPlaceholderExpansion extends PlaceholderExpansion {

    private final Questsystem plugin;
    private final PlayerDataManager dataManager;

    public QuestsPlaceholderExpansion(Questsystem plugin, PlayerDataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override public @NotNull String getIdentifier() { return "questsystem"; }
    @Override public @NotNull String getAuthor() { return String.join(", ", plugin.getDescription().getAuthors()); }
    @Override public @NotNull String getVersion() { return plugin.
            getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";

        PlayerQuestData data = dataManager.get(player.getUniqueId());
        if (data == null) return "0";

        if (params.startsWith("questsprogress_")) {
            String id = params.substring("questsprogress_".length());
            if (data.hasCompleted(id)) {
                Quest q = plugin.getQuestManager().getById(id);
                return q != null ? String.valueOf((int) q.getRequiredAmount()) : "0";
            }
            QuestProgress prog = data.getActiveQuests().get(id);
            return prog != null ? String.valueOf((int) prog.getProgress()) : "0";
        }

        if (params.equals("completed_total")) {
            return String.valueOf(data.getCompletedQuests().size());
        }

        return null;
    }
}