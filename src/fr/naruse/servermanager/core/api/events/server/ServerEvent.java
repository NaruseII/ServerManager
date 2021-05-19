package fr.naruse.servermanager.core.api.events.server;

import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.server.Server;

public class ServerEvent implements IEvent {

    private final Server server;

    public ServerEvent(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
