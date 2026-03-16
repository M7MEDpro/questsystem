package dev.m7med.questsystem.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class ProgressBarUtil {
    private static String filledSymbol = "■";
    private static String emptySymbol  = "□";
    private static String filledColor  = "&a";
    private static String emptyColor   = "&7";
    private static int barLength = 10;

    private ProgressBarUtil() {}

    public static void reload(FileConfiguration cfg) {
        filledSymbol = cfg.getString("gui.progress_bar.symbol_complete", "■");
        emptySymbol  = cfg.getString("gui.progress_bar.symbol_incomplete", "□");
        filledColor  = cfg.getString("gui.progress_bar.color_complete", "&a");
        emptyColor   = cfg.getString("gui.progress_bar.color_incomplete", "&7");
        barLength    = cfg.getInt("gui.progress_bar.length", 10);
    }

    public static String build(double current, double max) {
        double ratio = max <= 0 ? 1.0 : Math.min(1.0, current / max);
        int filled = (int) (ratio * barLength);
        int empty  = barLength - filled;

        return color(filledColor) + filledSymbol.repeat(filled)
                + color(emptyColor) + emptySymbol.repeat(empty);
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}