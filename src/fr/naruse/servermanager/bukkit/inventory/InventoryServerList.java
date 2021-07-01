package fr.naruse.servermanager.bukkit.inventory;

import com.google.common.collect.Lists;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InventoryServerList extends AbstractInventory {

    public InventoryServerList(JavaPlugin pl, Player p) {
        super(pl, p, "§lServer List", 9*((ServerList.getAll().size()/7)+1+2));
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        for (Server server : ServerList.getAll()) {

            Material material = Material.WATER_BUCKET;
            String tag = "§a";

            switch (server.getCoreServerType()) {
                case FILE_MANAGER:
                    material = Material.BOOK;
                    tag = "§4";
                    break;
                case SPONGE_MANAGER:
                    material = Material.SPONGE;
                    tag = "§§6";
                    break;
                case VELOCITY_MANAGER:
                    material = Material.ICE;
                    tag = "§b";
                    break;
                case BUNGEE_MANAGER:
                    material = Material.MILK_BUCKET;
                    tag = "§3";
                    break;
                case PACKET_MANAGER:
                    material = Material.WEB;
                    tag = "§c";
                    break;
            }

            List<String> lore = Lists.newArrayList(
                    "§5Address: §d"+server.getAddress().getHostAddress(),
                    "§5Port: §d"+server.getPort(),
                    "§5SMPort: §d"+server.getServerManagerPort(),
                    "§5Capacity: §d"+server.getData().getCapacity(),
                    "§5PlayerSize: §d"+server.getData().getPlayerSize(),
                    "§5Players: §d"+server.getData().getUUIDByNameMap().keySet().toString(),
                    "§5Status: §d"+server.getData().getStatusSet().toString());

            inventory.addItem(buildItem(material, 0, tag+server.getName(), false, lore));
        }

        inventory.setItem(inventory.getSize()-1, buildItem(Material.BARRIER, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {
        if(item != null && item.getType() != Material.STAINED_GLASS_PANE){
            Server server = ServerList.getByName(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
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
