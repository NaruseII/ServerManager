package fr.naruse.servermanager.core.api.events.server;

import fr.naruse.servermanager.core.server.Server;

public class ServerPostRegisterEvent extends ServerEvent {

    public ServerPostRegisterEvent(Server server) {
        super(server);
    }

}
