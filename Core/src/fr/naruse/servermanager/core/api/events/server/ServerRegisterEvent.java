package fr.naruse.servermanager.core.api.events.server;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;
import fr.naruse.servermanager.core.server.Server;

public class ServerRegisterEvent extends ServerEvent implements ICancellableEvent {

    private boolean cancelled = false;

    public ServerRegisterEvent(Server server) {
        super(server);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
