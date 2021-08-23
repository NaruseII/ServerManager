package fr.naruse.servermanager.bukkit.inventory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fr.naruse.servermanager.core.connection.packet.PacketShutdown;
import fr.naruse.servermanager.core.connection.packet.PacketSwitchServer;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryServer extends AbstractInventory {

    private final Server server;

    public InventoryServer(JavaPlugin pl, Player p, Server server) {
        super(pl, p, "§lServer List", 9*6, false);
        this.server = server;

        initInventory(inventory);
        p.openInventory(inventory);
    }

    @Override
    protected void initInventory(Inventory inventory) {
        setDecoration();

        for (int i = 0; i < 7; i++) {
            inventory.addItem(buildItem(Material.GLASS, 0, Utils.RANDOM.nextLong()+"", true, null));
        }

        Set<String> set = Sets.newHashSet(server.getData().getUUIDByNameMap().keySet());

        inventory.setItem(43, buildItem(Material.PAPER, 0, "§3Full Player List:", true, Lists.newArrayList("§5"+set.stream().collect(Collectors.joining("§d,§5 ")))));

        for (String name : set) {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setOwner(name);
            meta.setDisplayName("§a"+name);
            itemStack.setItemMeta(meta);

            inventory.addItem(itemStack);
        }

        inventory.remove(Material.GLASS);

        inventory.addItem(buildItem(Material.TNT, 0, "§c§lShutdown", true, null));
        inventory.addItem(buildItem(Material.COMPASS, 0, "§c§lConnect", true, null));

        inventory.setItem(inventory.getSize()-1, buildItem(Material.BARRIER, 0, "§cBack", false, null));
    }

    @Override
    protected void actionPerformed(Player p, ItemStack item, InventoryAction action, int slot) {
        boolean back = false;

        if(item != null && item.getType() != Material.STAINED_GLASS_PANE){
            if(item.getType() == Material.TNT){
                back = true;
                server.sendPacket(new PacketShutdown());
            }else if(item.getType() == Material.COMPASS){
                Optional<Server> proxyOptional = ServerList.findPlayerProxyServer(p.getName());
                if(proxyOptional.isPresent()){
                    proxyOptional.get().sendPacket(new PacketSwitchServer(server, p.getName()));
                }else{
                    p.sendMessage("§cYou're not on a proxy.");
                }
            }else if(item.getType() == Material.SKULL_ITEM){
                String owner = ((SkullMeta) item.getItemMeta()).getOwner();
                if(owner != null){
                    new InventoryPlayer(pl, p, server, owner);
                }
            }
        }

        if(slot == inventory.getSize()-1 || back){
            new InventoryServerList(pl, p);
        }
    }
}
