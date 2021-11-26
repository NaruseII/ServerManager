package fr.naruse.servermanager.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryMain extends AbstractInventory {

    public InventoryMain(JavaPlugin pl, Player p) {
        super(pl, p, "§lMain Control Panel", 9*3);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        inventory.addItem(buildItem(Material.ENCHANTED_BOOK, 0, "§3Server List", false, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Material.BARRIER, 0, "§cClose", false, null));
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {
        if(item != null){
            if(item.getType() == Material.ENCHANTED_BOOK){
                new InventoryServerList(pl, p);
            }else if(slot == inventory.getSize()-1){
                p.closeInventory();
            }
        }
    }
}
