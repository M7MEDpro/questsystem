package dev.m7med.questsystem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemBuilder {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();
    private final ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String raw, Player player, Function<String, String> replacer) {
        if (meta == null) return this;
        String resolved = applyAll(raw, player, replacer);
        meta.displayName(LEGACY.deserialize(resolved));
        return this;
    }

    public ItemBuilder lore(List<String> lines, Player player, Function<String, String> replacer) {
        if (meta == null || lines == null) return this;
        List<Component> built = new ArrayList<>(lines.size());
        for (String line : lines) {
            built.add(LEGACY.deserialize(applyAll(line, player, replacer)));
        }
        meta.lore(built);
        return this;
    }

    public ItemBuilder modelData(int data) {
        if (meta != null && data > 0) meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder flags(List<String> flagNames) {
        if (meta == null || flagNames == null) return this;
        for (String name : flagNames) {
            try { meta.addItemFlags(ItemFlag.valueOf(name.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return this;
    }

    public ItemBuilder glow(boolean enabled) {
        if (!enabled || meta == null) return this;
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        meta = item.getItemMeta();
        if (meta != null) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    private String applyAll(String text, Player player, Function<String, String> replacer) {
        String s = replacer.apply(text);
        if (player != null && isPapiPresent()) {
            s = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, s);
        }
        return s;
    }

    private static boolean isPapiPresent() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
}