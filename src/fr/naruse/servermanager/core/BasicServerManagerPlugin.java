package fr.naruse.servermanager.core;

import fr.naruse.servermanager.core.api.events.EventListener;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.core.api.events.server.ServerDeleteEvent;
import fr.naruse.servermanager.core.api.events.server.ServerRegisterEvent;

import java.util.Set;

public class BasicServerManagerPlugin implements IServerManagerPlugin {

    private final Set<EventListener> eventListenerSet;
    public BasicServerManagerPlugin(Set<EventListener> eventListenerSet) {
        this.eventListenerSet = eventListenerSet;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void callEvent(IEvent event) {
        if(event instanceof ServerRegisterEvent){
            this.eventListenerSet.forEach(eventListener -> eventListener.onServerRegisterEvent((ServerRegisterEvent) event));
        }else if(event instanceof ServerDeleteEvent){
            this.eventListenerSet.forEach(eventListener -> eventListener.onServerDeleteEvent((ServerDeleteEvent) event));
        }
    }
}
