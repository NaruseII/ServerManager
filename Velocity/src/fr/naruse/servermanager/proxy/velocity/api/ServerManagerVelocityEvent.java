package fr.naruse.servermanager.proxy.velocity.api;

import fr.naruse.servermanager.core.api.events.IEvent;

public class ServerManagerVelocityEvent {

    private final IEvent event;

    public ServerManagerVelocityEvent(IEvent event) {
        this.event = event;
    }

    public IEvent getEvent() {
        return event;
    }
}
