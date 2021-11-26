package fr.naruse.servermanager.core.api.events;

public interface ICancellableEvent extends IEvent {

    void setCancelled(boolean cancelled);

    boolean isCancelled();

}
