package fr.naruse.servermanager.core.api.events.plugin;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;

public class PluginFileManagerEvent {

    public static class AsyncPreCreateServerEvent implements ICancellableEvent {
        private boolean isCancelled = false;
        private final String templateName;

        public AsyncPreCreateServerEvent(String templateName) {
            this.templateName = templateName;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.isCancelled = cancelled;
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        public String getTemplateName() {
            return templateName;
        }
    }

}
