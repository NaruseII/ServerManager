package fr.naruse.servermanager.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemSkull;
import cn.nukkit.plugin.PluginBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.connection.packet.PacketSwitchServer;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryNukkitServer extends AbstractNukkitInventory {

    private final Server server;

    public InventoryNukkitServer(PluginBase pl, Player p, Server server) {
        super(pl, p, "§lServer List", false);
        this.server = server;

        initInventory(inventory);
        inventory.open(p);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        for (int i = 0; i < 7; i++) {
            inventory.addItem(buildItem(Item.GLASS, 0, Utils.RANDOM.nextLong()+"", true, null));
        }

        Set<String> set = Sets.newHashSet(server.getData().getUUIDByNameMap().keySet());

        inventory.setItem(43, buildItem(Item.PAPER, 0, "§3Full Player List:", true, Lists.newArrayList("§5"+set.stream().collect(Collectors.joining("§d,§5 ")))));

        for (String name : set) {
            ItemSkull itemStack = new ItemSkull(3);
            itemStack.setCustomName("§a"+name);

            inventory.addItem(itemStack);
        }

        inventory.remove(new Item(Item.GLASS));

        inventory.addItem(buildItem(Item.TNT, 0, "§c§lShutdown", true, null));
        inventory.addItem(buildItem(Item.COMPASS, 0, "§c§lConnect", true, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Item.IRON_BAR, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, Item item, int slot) {
        boolean back = false;

        if(item != null && item.getId() != Item.STAINED_GLASS_PANE){
            if(item.getId() == Item.TNT){
                back = true;
                server.sendPacket(new PacketShutdown());
            }else if(item.getId() == Item.COMPASS){
                Optional<Server> proxyOptional = ServerList.findPlayerProxyServer(p.getName());
                if(proxyOptional.isPresent()){
                    proxyOptional.get().sendPacket(new PacketSwitchServer(server, p.getName()));
                }else{
                    p.sendMessage("§cYou're not on a proxy.");
                }
            }else if(item.getId() == Item.SKULL){
                String owner = item.getCustomName().substring(2);
                if(owner != null){
                    new InventoryNukkiPlayer(pl, p, server, owner);
                }
            }
        }

        if(slot == inventory.getSize()-1 || back){
            new InventoryNukkitServerList(pl, p);
        }
    }
}
