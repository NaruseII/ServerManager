package fr.naruse.servermanager.core.api.events.server;

import fr.naruse.servermanager.core.server.Server;

public class ServerPostDeleteEvent extends ServerEvent {

    public ServerPostDeleteEvent(Server server) {
        super(server);
    }

}
