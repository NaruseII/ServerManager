package fr.naruse.servermanager.core.api.events;

import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerPostDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;

public class EventListener {

    public void onServerRegisterEvent(ServerRegisterEvent e) { }

    public void onServerDeleteEvent(ServerDeleteEvent e) { }

}
