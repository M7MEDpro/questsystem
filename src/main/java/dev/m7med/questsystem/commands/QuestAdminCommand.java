package dev.m7med.questsystem.commands;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import dev.m7med.questsystem.quest.Quest;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.mevera.imperat.annotations.Command;
import studio.mevera.imperat.annotations.Permission;
import studio.mevera.imperat.annotations.SubCommand;
import studio.mevera.imperat.annotations.Usage;
import studio.mevera.imperat.annotations.Suggest;

@Command({"questadmin", "qa"})
@Permission("questsystem.admin")
public class QuestAdminCommand {
    private final Questsystem plugin;

    public QuestAdminCommand(Questsystem plugin) {
        this.plugin = plugin;
    }

    @Usage
    public void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Use /qa <reload | setprogress | reset>");
    }

    @SubCommand("reload")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.initQuests();
        sender.sendMessage(msg("messages.plugin_reloaded"));
    }

    @SubCommand("setprogress")
    public void setProgress(CommandSender sender, Player target, @Suggest("quest_ids") String id, double val) {
        Quest q = plugin.getQuestManager().getById(id);
        if (q == null) {
            sender.sendMessage(msg("messages.quest_not_found").replace("%id%", id));
            return;
        }

        PlayerQuestData data = plugin.getPlayerDataManager().get(target);
        if (data == null) {
            sender.sendMessage(msg("messages.player_not_found"));
            return;
        }

        plugin.getQuestEventListener().awardExact(target, id, val);

        sender.sendMessage(msg("messages.progress_set")
                .replace("%player%", target.getName())
                .replace("%id%", id)
                .replace("%value%", String.valueOf(val)));
    }

    @SubCommand("reset")
    public void reset(CommandSender sender, Player target, @Suggest("quest_ids") String id) {
        PlayerQuestData data = plugin.getPlayerDataManager().get(target);
        if (data == null) {
            sender.sendMessage(msg("messages.player_not_found"));
            return;
        }

        data.getCompletedQuests().remove(id);
        data.getActiveQuests().put(id, new QuestProgress(0));

        sender.sendMessage(msg("messages.progress_reset")
                .replace("%player%", target.getName())
                .replace("%id%", id));
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("messages.prefix", "&8[&6Quests&8] &7");
        String txt = plugin.getConfig().getString(path, "&cError: " + path);
        return ChatColor.translateAlternateColorCodes('&', pre + txt);
    }
}