package fr.naruse.servermanager.bukkit.inventory;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.naruse.servermanager.bukkit.utils.Heads;
import fr.naruse.servermanager.core.CoreData;
import fr.naruse.servermanager.core.CoreServerType;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    protected ItemStack buildSkull(Material material, Heads.Head headUrl, String name, boolean enchant, List<String> lore) {
        ItemStack head = this.buildItem(material, 3, name, enchant, lore);
        SkullMeta skullMeta = (SkullMeta)head.getItemMeta();
        StringBuilder s_url = new StringBuilder();
        s_url.append(headUrl.getURL());
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), (String)null);
        byte[] data = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", s_url).getBytes());
        gameProfile.getProperties().put("textures", new Property("textures", new String(data)));

        try {
            Field field = skullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(skullMeta, gameProfile);
        } catch (Exception var12) {
            var12.printStackTrace();
        }

        skullMeta.setDisplayName(name);
        head.setItemMeta(skullMeta);
        return head;
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

    protected void addServer(Server server){
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

    protected Server getServerFromItem(ItemStack item){
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
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
