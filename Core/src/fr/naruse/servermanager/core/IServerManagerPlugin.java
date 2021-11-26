package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.api.events.IEvent;

public interface IServerManagerPlugin {

    void shutdown();

    void callEvent(IEvent event);

}
