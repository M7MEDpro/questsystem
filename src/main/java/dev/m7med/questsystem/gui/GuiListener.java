package dev.m7med.questsystem.gui;

import dev.m7med.questsystem.Questsystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    public GuiListener(Questsystem plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof QuestMenu menu)) return;

        event.setCancelled(true);

        if (!(event.getClickedInventory() != null
                && event.getClickedInventory().getHolder() instanceof QuestMenu)) return;

        int slot      = event.getRawSlot();
        int invSize   = menu.getInventory().getSize();
        int navStart  = invSize - 9;

        if (slot != navStart && slot != navStart + 8) return;

        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() != Material.ARROW) return;

        Player player = (Player) event.getWhoClicked();
        int current = menu.getPage();
        if (slot == navStart && current > 0) {
            new QuestMenu(player, current - 1).open(player);
        } else if (slot == navStart + 8) {
            new QuestMenu(player, current + 1).open(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof QuestMenu) {
            event.setCancelled(true);
        }
    }
}