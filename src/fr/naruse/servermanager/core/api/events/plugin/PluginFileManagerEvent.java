package fr.naruse.servermanager.core.api.events.plugin;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.filemanager.ServerProcess;

import java.util.List;

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

    public static class AsyncConsoleOutputEvent implements IEvent {

        private final ServerProcess serverProcess;
        private final List<String> newLines;

        public AsyncConsoleOutputEvent(ServerProcess serverProcess, List<String> newLines) {
            this.serverProcess = serverProcess;
            this.newLines = newLines;
        }

        public List<String> getNewLines() {
            return newLines;
        }

        public ServerProcess getServerProcess() {
            return serverProcess;
        }
    }

}
