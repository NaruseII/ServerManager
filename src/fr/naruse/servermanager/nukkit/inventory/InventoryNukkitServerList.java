package fr.naruse.servermanager.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.plugin.PluginBase;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;

public class InventoryNukkitServerList extends AbstractNukkitInventory {

    public InventoryNukkitServerList(PluginBase pl, Player p) {
        super(pl, p, "§lServer List");
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        for (Server server : ServerList.getAll()) {
            this.addServer(server);
        }

        CoreData coreData = ServerManager.get().getCoreData();
        this.addServer(new Server("packet-manager", coreData.getServerManagerPort(), coreData.getPacketManagerHost(), coreData.getServerManagerPort(), CoreServerType.PACKET_MANAGER));

        inventory.setItem(inventory.getSize()-1, buildItem(Item.IRON_BAR, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, Item item, int slot) {
        if(item != null && item.getId() != Item.STAINED_GLASS_PANE){
            Server server = getServerFromItem(item);

            if(server != null){
                new InventoryNukkitServer(pl, p, server);
                return;
            }else{
                inventory.clearAll();
                initInventory(inventory);
            }
        }
        if(slot == inventory.getSize()-1){
            new InventoryNukkitMain(pl, p);
        }
    }


}
