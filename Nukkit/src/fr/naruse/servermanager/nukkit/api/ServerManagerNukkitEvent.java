package fr.naruse.servermanager.nukkit.api;

import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;
import fr.naruse.servermanager.core.api.events.IEvent;

public class ServerManagerNukkitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final IEvent event;

    public ServerManagerNukkitEvent(IEvent event) {
        this.event = event;
    }

    public IEvent getEvent() {
        return event;
    }

}
