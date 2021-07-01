package fr.naruse.servermanager.bukkit.inventory;

import com.google.common.collect.Lists;
import fr.naruse.servermanager.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class AbstractInventory implements Listener {

    protected JavaPlugin pl;
    protected Player p;
    private boolean isDone = false;
    protected Inventory inventory;

    public AbstractInventory(JavaPlugin pl, Player p, String invName, int size) {
        this(pl, p, invName, size, true);
    }

    public AbstractInventory(JavaPlugin pl, Player p, String invName, int size, boolean initInventory) {
        this.pl = pl;
        this.p = p;
        this.inventory = Bukkit.createInventory(null, size, invName);

        Bukkit.getPluginManager().registerEvents(this, pl);
        if(initInventory){
            initInventory(inventory);
            p.openInventory(inventory);
        }
    }

    protected abstract void initInventory(Inventory inventory);

    protected abstract void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot);

    public void onClose() { }

    @EventHandler
    public void onClickEvent(InventoryClickEvent e){
        if(isDone){
            return;
        }
        if(!(e.getWhoClicked() instanceof Player)){
            return;
        }
        Player p = (Player) e.getWhoClicked();
        if(p != this.p){
            return;
        }
        if(!e.getInventory().equals(inventory)){
            return;
        }
        if(e.getCurrentItem() == null){
            return;
        }
        e.setCancelled(true);
        actionPerformed(p, e.getCurrentItem(), e.getAction(), e.getSlot());
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        if(p != this.p){
            return;
        }
        if(!e.getInventory().equals(inventory)){
            return;
        }
        onClose();
        isDone = true;
    }

    @EventHandler
    public void openEvent(InventoryOpenEvent e){
        Player p = (Player) e.getPlayer();
        if(p != this.p){
            return;
        }
        if(!e.getInventory().equals(inventory)){
            return;
        }
        isDone = false;
    }

    protected ItemStack buildItem(Material material, int data, String name, boolean enchant, List<String> lore){
        ItemStack itemStack = new ItemStack(material, 1, (byte) data);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        if(enchant){
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }
        if(lore != null){
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    protected void setDecoration() {
        int color = Utils.RANDOM.nextInt(16);
        if(color == 8){
            color++;
        }
        for (int i : this.getDecorationSlots()) {
            inventory.setItem(i, buildItem(Material.STAINED_GLASS_PANE, color, "", false, null));
        }
    }

    private List<Integer> getDecorationSlots() {
        List<Integer> slots = Lists.newArrayList();
        int lines = inventory.getSize() / 9;

        for (int i = 0; i < 9; i++) {
            slots.add(i);
            slots.add(i + (lines - 1) * 9);
        }

        for (int i = 0; i < lines - 1; i++) {
            slots.add(i * 9);
            slots.add(i * 9 + 8);
        }

        return slots;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isDone() {
        return isDone;
    }
}
