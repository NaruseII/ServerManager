package fr.naruse.servermanager.proxy.bungee.api;

import fr.naruse.servermanager.core.api.events.IEvent;
import net.md_5.bungee.api.plugin.Event;

public class ServerManagerBungeeEvent extends Event {

    private final IEvent event;

    public ServerManagerBungeeEvent(IEvent event) {
        this.event = event;
    }

    public IEvent getEvent() {
        return event;
    }
}
