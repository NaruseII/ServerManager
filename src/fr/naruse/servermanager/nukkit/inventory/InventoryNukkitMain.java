package fr.naruse.servermanager.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;

public class InventoryNukkitMain extends AbstractNukkitInventory {

    public InventoryNukkitMain(PluginBase pl, Player p) {
        super(pl, p, "§lMain Control Panel");
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        inventory.addItem(buildItem(Item.ENCHANTED_BOOK, 0, "§3Server List", false, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Item.IRON_BAR, 0, "§cClose", false, null));
    }

    @Override
    protected void actionPerformed(Player p, Item item, int slot) {
        if(item != null){
            if(item.getId() == Item.ENCHANTED_BOOK){
                new InventoryNukkitServerList(pl, p);
            }else if(slot == inventory.getSize()-1){
                inventory.close(p);
            }
        }
    }
}
