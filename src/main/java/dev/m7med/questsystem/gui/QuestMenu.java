package dev.m7med.questsystem.gui;

import dev.m7med.questsystem.Questsystem;
import dev.m7med.questsystem.data.model.PlayerQuestData;
import dev.m7med.questsystem.data.model.QuestProgress;
import dev.m7med.questsystem.quest.Quest;
import dev.m7med.questsystem.util.ItemBuilder;
import dev.m7med.questsystem.util.ProgressBarUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QuestMenu implements InventoryHolder {

    private static int size = 54;
    private static String title = "&8▶ &eQuest Menu";
    private static String lblNotStarted = "&cNot Started";

    private static String lblInProgress = "&eIn Progress";
    private static String lblCompleted  = "&aCompleted";

    private static List<String> defaultLoreFormat = new ArrayList<>();
    private static boolean fillerEnabled = true;
    private static Material fillerMat = Material.BLACK_STAINED_GLASS_PANE;
    private static String fillerName = " ";

    private final Inventory inv;
    private final int page;

    public static void reload(FileConfiguration cfg) {
        size        = cfg.getInt("gui.size", 54);
        title       = cfg.getString("gui.title", "&8▶ &eQuest Menu");
        lblNotStarted = cfg.getString("gui.quest_display.status_locked",
                "&cNot Started");
        lblInProgress = cfg.getString("gui.quest_display.status_in_progress",
                "&eIn Progress");
        lblCompleted  = cfg.getString("gui.quest_display.status_completed"
                , "&aCompleted");
        defaultLoreFormat = cfg.getStringList("gui.quest_display.lore_format");
        fillerEnabled = cfg.getBoolean("gui.filler_item.enabled", true);

        Material parsed = Material.
                matchMaterial(cfg.getString("gui.filler_item.material",
                        "BLACK_STAINED_GLASS_PANE"));

        fillerMat   = parsed != null ? parsed : Material.BLACK_STAINED_GLASS_PANE;
        fillerName  = cfg.getString("gui.filler_item.name", " ");
    }

    public QuestMenu(Player player, int page) {
        this.page = page;
        Questsystem plugin = Questsystem.getInstance();

        this.inv = Bukkit.createInventory(this, size,
                LegacyComponentSerializer.legacyAmpersand().
                        deserialize(title + " &8- &7Page " + (page + 1)));

        populate(player, plugin);
    }

    private void populate(Player player, Questsystem plugin) {
        PlayerQuestData data = plugin.getPlayerDataManager().get(player);
        if (data == null) return;

        List<Quest> all = new ArrayList<>(plugin.getQuestManager().getAll());
        int questSlots = size - 9;
        int from = page * questSlots;
        int to   = Math.min(from + questSlots, all.size());

        if (fillerEnabled) {
            ItemStack filler = buildFiller();
            for (int i = 0; i < size; i++) inv.setItem(i, filler);
        }

        for (int i = from; i < to; i++) {
            inv.setItem(i - from, buildQuestItem(all.get(i), data, player));
        }

        placeNavigation(page, all.size(), questSlots);
    }

    private ItemStack buildQuestItem(Quest quest, PlayerQuestData data, Player player) {
        double req = quest.getRequiredAmount();
        double cur;
        String status;

        if (data.hasCompleted(quest.getId())) {
            cur    = req;
            status = lblCompleted;
        } else {
            QuestProgress prog = data.getActiveQuests().get(quest.getId());
            if (prog != null && prog.getProgress() > 0) {
                cur    = prog.getProgress();
                status = lblInProgress;
            } else {
                cur    = 0;
                status = lblNotStarted;
            }
        }

        int pct = (int) Math.min(100, (cur / req) * 100);
        String bar = ProgressBarUtil.build(cur, req);

        Function<String, String> ph = s -> s
                .replace("%progress%",   String.valueOf((int) cur))
                .replace("%required%",   String.valueOf((int) req))
                .replace("%percentage%", String.valueOf(pct))
                .replace("%status%",     status)
                .replace("%bar%",        bar)
                .replace("%type%",       quest.getType().name())
                .replace("%target%",     quest.getTarget());

        List<String> lore = quest.getLore().
                isEmpty() ? applyDefaultLore(quest, ph) : quest.getLore();

        return new ItemBuilder(quest.getDisplayMaterial())
                .name(quest.getDisplayName(), player, ph)
                .lore(lore, player, ph)
                .modelData(quest.getCustomModelData())
                .flags(quest.getItemFlags())
                .glow(quest.isGlow())
                .build();
    }

    private List<String> applyDefaultLore(Quest quest, Function<String, String> ph) {
        List<String> result = new ArrayList<>(defaultLoreFormat.size());
        for (String line : defaultLoreFormat) result.add(ph.apply(line));
        return result;
    }

    private void placeNavigation(int currentPage, int totalQuests, int questSlots) {
        int navRow = size - 9;
        if (currentPage > 0) {
            inv.setItem(navRow, navButton(Material.ARROW,
                    "&ePrevious Page",
                    "&7Go to page " + currentPage));
        }
        if ((currentPage + 1) * questSlots < totalQuests) {
            inv.setItem(navRow + 8, navButton(Material.ARROW
                    , "&eNext Page",
                    "&7Go to page " + (currentPage + 2)));
        }
    }

    private ItemStack navButton(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(name));
            meta.lore(List.of(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildFiller() {
        ItemStack item = new ItemStack(fillerMat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().
                    deserialize(fillerName));
            meta.lore(List.of());
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getPage() { return page; }

    public void open(Player player) { player.openInventory(inv); }

    @Override
    public @NotNull Inventory getInventory() { return inv; }
}