package fr.naruse.servermanager.bukkit.inventory;

import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryServerList extends AbstractInventory {

    public InventoryServerList(JavaPlugin pl, Player p) {
        super(pl, p, "§lServer List", 9*6);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        for (Server server : ServerList.getAll()) {
            this.addServer(server);
        }

        CoreData coreData = ServerManager.get().getCoreData();
        this.addServer(new Server("packet-manager", coreData.getServerManagerPort(), coreData.getPacketManagerHost(), coreData.getServerManagerPort(), CoreServerType.PACKET_MANAGER));

        inventory.setItem(inventory.getSize()-1, buildItem(Material.BARRIER, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {
        if(item != null && item.getType() != Material.STAINED_GLASS_PANE){
            Server server = getServerFromItem(item);

            if(server != null){
                new InventoryServer(pl, p, server);
                return;
            }else{
                inventory.clear();
                initInventory(inventory);
            }
        }
        if(slot == inventory.getSize()-1){
            new InventoryMain(pl, p);
        }
    }


}
