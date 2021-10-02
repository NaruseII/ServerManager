package fr.naruse.servermanager.nukkit.inventory;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryClickEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.ContainerInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.PluginBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;

import java.util.List;

public abstract class AbstractNukkitInventory implements Listener {

    protected PluginBase pl;
    protected Player p;
    private boolean isDone = false;
    protected Inventory inventory;

    public AbstractNukkitInventory(PluginBase pl, Player p, String invName) {
        this(pl, p, invName, true);
    }

    public AbstractNukkitInventory(PluginBase pl, Player p, String invName, boolean initInventory) {
        this.pl = pl;
        this.p = p;
        this.inventory = new BaseInventory(p, InventoryType.DOUBLE_CHEST, Maps.newHashMap(), 9*6, invName) {};

        pl.getServer().getPluginManager().registerEvents(this, pl);
        if(initInventory){
            initInventory(inventory);
            inventory.open(p);
        }
    }

    protected abstract void initInventory(Inventory inventory);

    protected abstract void actionPerformed(Player p, Item item, int slot);

    public void onClose() { }

    @EventHandler
    public void onClickEvent(InventoryClickEvent e){
        if(isDone){
            return;
        }

        Player p = e.getPlayer();
        if(p != this.p){
            return;
        }
        if(!e.getInventory().equals(inventory)){
            return;
        }
        if(e.getSourceItem() == null){
            return;
        }

        e.setCancelled(true);
        actionPerformed(p, e.getSourceItem(), e.getSlot());
    }

    @EventHandler
    public void onCloseEvent(InventoryCloseEvent e){
        Player p = e.getPlayer();
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
        Player p = e.getPlayer();
        if(p != this.p){
            return;
        }
        if(!e.getInventory().equals(inventory)){
            return;
        }
        isDone = false;
    }

    protected Item buildItem(int id, int data, String name, boolean enchant, List<String> lore){
        Item item = new Item(id, data);
        item.setCustomName(name);
        if(enchant) {
            item.addEnchantment(Enchantment.get(Enchantment.ID_LURE));
        }
        if(lore != null){
            item.setLore(lore.toArray(new String[0]));
        }
        return item;
    }

    protected void setDecoration() {
        int color = Utils.RANDOM.nextInt(16);
        if(color == 8){
            color++;
        }
        for (int i : this.getDecorationSlots()) {
            inventory.setItem(i, buildItem(Item.STAINED_GLASS_PANE, color, "", false, null));
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

    protected void addServer(Server server){
        int material = Item.BUCKET;
        String tag = "§a";

        switch (server.getCoreServerType()) {
            case FILE_MANAGER:
                material = Item.BOOK;
                tag = "§4";
                break;
            case SPONGE_MANAGER:
                material = Item.SPONGE;
                tag = "§§6";
                break;
            case VELOCITY_MANAGER:
                material = Item.ICE;
                tag = "§b";
                break;
            case BUNGEE_MANAGER:
                material = Item.BEACON;
                tag = "§3";
                break;
            case PACKET_MANAGER:
                material = Item.SEEDS;
                tag = "§c";
                break;
        }

        List<String> lore = Lists.newArrayList(
                "§5Address: §d"+server.getAddress().getHostAddress(),
                "§5Port: §d"+server.getPort(),
                "§5SMPort: §d"+server.getServerManagerPort(),
                "§5Capacity: §d"+server.getData().getCapacity(),
                "§5PlayerSize: §d"+server.getData().getPlayerSize(),
                "§5Players: §d"+server.getData().getUUIDByNameMap().keySet(),
                "§5Status: §d"+server.getData().getStatusSet().toString());

        inventory.addItem(buildItem(material, 0, tag+server.getName(), false, lore));
    }

    protected Server getServerFromItem(Item item){
        String name = item.getCustomName().substring(2);
        Server server = ServerList.getByName(name);
        if(name.equals("packet-manager")){
            CoreData coreData = ServerManager.get().getCoreData();
            server = new Server("packet-manager", coreData.getServerManagerPort(), coreData.getPacketManagerHost(), coreData.getServerManagerPort(), CoreServerType.PACKET_MANAGER);
        }
        return server;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isDone() {
        return isDone;
    }
}
