package fr.naruse.servermanager.sponge.api;

import fr.naruse.servermanager.core.api.events.IEvent;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class ServerManagerSpongeEvent implements Event {

    private final IEvent event;

    public ServerManagerSpongeEvent(IEvent event) {
        this.event = event;
    }

    public IEvent getEvent() {
        return event;
    }

    @Override
    public Cause getCause() {
        return null;
    }
}
