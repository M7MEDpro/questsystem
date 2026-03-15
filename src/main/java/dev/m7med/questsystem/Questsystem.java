package dev.m7med.questsystem;

import org.bukkit.plugin.java.JavaPlugin;

public final class Questsystem extends JavaPlugin {

    private static Questsystem instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {

    }

    public static Questsystem getInstance() {
        return instance;
    }
}
