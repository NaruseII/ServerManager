package fr.naruse.servermanager.bukkit.api;

import fr.naruse.servermanager.core.api.events.IEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerManagerBukkitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final IEvent event;

    public ServerManagerBukkitEvent(IEvent event) {
        this.event = event;
    }

    public IEvent getEvent() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
