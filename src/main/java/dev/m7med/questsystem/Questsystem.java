package dev.m7med.questsystem;

import dev.m7med.questsystem.commands.QuestAdminCommand;
import dev.m7med.questsystem.commands.QuestCommand;
import dev.m7med.questsystem.data.MongoDatabaseManager;
import dev.m7med.questsystem.data.PlayerDataManager;
import dev.m7med.questsystem.gui.GuiListener;
import dev.m7med.questsystem.gui.QuestMenu;
import dev.m7med.questsystem.hooks.QuestsPlaceholderExpansion;
import dev.m7med.questsystem.quest.*;
import dev.m7med.questsystem.util.ProgressBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import studio.mevera.imperat.BukkitImperat;

import java.util.ArrayList;
import java.util.List;

public final class Questsystem extends JavaPlugin {
    private static Questsystem instance;
    private MongoDatabaseManager db;
    private PlayerDataManager dataHandler;
    private QuestManager questHandler;
    private QuestEventListener questEventListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        db = new MongoDatabaseManager();
        db.connect(
                getConfig().getString("mongodb.uri", "mongodb://localhost:27017"),
                getConfig().getString("mongodb.database", "quest_system")
        );

        questHandler = new QuestManager();
        dataHandler = new PlayerDataManager(this, db);

        initQuests();

        questEventListener = new QuestEventListener(this, questHandler, dataHandler);
        new MovementQuestListener(this, questHandler, dataHandler);
        new GuiListener(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestsPlaceholderExpansion(this, dataHandler).register();
            getLogger().info("PAPI hooked!");
        }

        BukkitImperat imperat = BukkitImperat.builder(this).build();
        imperat.config().registerNamedSuggestionResolver("quest_ids", (context, parameter) ->
                questHandler.getAll().stream()
                        .map(Quest::getId)
                        .toList()
        );
        imperat.registerCommand(new QuestCommand(this));
        imperat.registerCommand(new QuestAdminCommand(this));

        getLogger().info("Plugin started successfully.");
    }

    @Override
    public void onDisable() {
        if (dataHandler != null) dataHandler.saveAll();
        if (db != null) db.disconnect();
        getLogger().info("Plugin shut down.");
    }

    public void initQuests() {
        questHandler.clear();
        ProgressBarUtil.reload(getConfig());
        QuestMenu.reload(getConfig());

        ConfigurationSection section = getConfig().getConfigurationSection("quests");
        if (section == null) {
            getLogger().warning("Quests section is missing in config!");
            return;
        }

        int count = 0;
        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection sec = section.getConfigurationSection(key);
                if (sec == null) continue;

                QuestType type = QuestType.valueOf(sec.getString("type",
                        "MINING").toUpperCase());

                String target = sec.getString("target", "STONE");
                double amount = sec.getDouble("required_amount", 10.0);
                List<String> rewards = sec.getStringList("rewards");

                Material mat = Material.PAPER;
                String name = "&e" + key;
                List<String> lore = new ArrayList<>();
                List<String> flags = new ArrayList<>();

                int model = 0;
                boolean glow = false;

                ConfigurationSection display = sec.getConfigurationSection("display");
                if (display != null) {
                    Material m = Material.matchMaterial(display.getString("material", "PAPER"));
                    if (m != null) mat = m;
                    name = display.getString("name", name);
                    lore = display.getStringList("lore");
                    flags = display.getStringList("flags");
                    model = display.getInt("custom_model_data", 0);
                    glow = display.getBoolean("glow", false);
                }

                questHandler.register(new Quest(key,
                        type, target, amount,
                        rewards, mat, name,
                        lore, flags, model,
                        glow));

                count++;

            } catch (Exception e) {
                getLogger().severe("Failed to load quest " + key + ": " + e.getMessage());
            }
        }

        getLogger().info("Found and loaded " + count + " quests.");
    }

    public static Questsystem getInstance() { return instance; }
    public PlayerDataManager getPlayerDataManager() { return dataHandler; }
    public QuestManager getQuestManager() { return questHandler; }
    public QuestEventListener getQuestEventListener() { return questEventListener; }
}