package fr.naruse.servermanager.core.api.events;

public interface ICancellableEvent {

    void setCancelled(boolean cancelled);

    boolean isCancelled();

}
