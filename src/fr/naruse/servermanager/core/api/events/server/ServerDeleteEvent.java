package fr.naruse.servermanager.core.api.events.server;

import fr.naruse.servermanager.core.server.Server;

public class ServerDeleteEvent extends ServerEvent {

    public ServerDeleteEvent(Server server) {
        super(server);
    }

}
