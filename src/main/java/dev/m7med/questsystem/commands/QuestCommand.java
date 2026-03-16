package dev.m7med.questsystem.commands;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.gui.QuestMenu;
import org.bukkit.entity.Player;
import studio.mevera.imperat.annotations.types.Execute;
import studio.mevera.imperat.annotations.types.RootCommand;

@RootCommand({"quests", "quest", "q"})
public class QuestCommand {

    private final Questsystem plugin;

    public QuestCommand(Questsystem plugin) {
        this.plugin = plugin;
    }

    @Execute
    public void open(Player player) {
        new QuestMenu(player, 0).open(player);
    }
}