package fr.naruse.servermanager.core.api.events.plugin;

import fr.naruse.servermanager.core.api.events.ICancellableEvent;
import fr.naruse.servermanager.core.api.events.IEvent;
import fr.naruse.servermanager.filemanager.ServerProcess;
import fr.naruse.servermanager.filemanager.task.CreateServerTask;

import java.util.List;
import java.util.Map;

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

    public static class AsyncPostCreateServerEvent implements IEvent{

        private final CreateServerTask createServerTask;
        private final Map<String, Object> initialServerData;

        public AsyncPostCreateServerEvent(CreateServerTask createServerTask, Map<String, Object> initialServerData) {
            this.createServerTask = createServerTask;
            this.initialServerData = initialServerData;
        }

        public CreateServerTask getCreateServerTask() {
            return createServerTask;
        }

        public Map<String, Object> getInitialServerData() {
            return initialServerData;
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
