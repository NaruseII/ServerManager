package fr.naruse.servermanager.filemanager;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.api.events.plugin.PluginFileManagerEvent;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.plugin.Plugins;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Screen {

    public static int READ_INTERVAL = 5000;
    private static boolean isStarted = false;

    private static void logReaderRunnable(){
        if(FileManager.EXECUTOR_SERVICE.isTerminated() || FileManager.EXECUTOR_SERVICE.isShutdown()){
            return;
        }
        FileManager.EXECUTOR_SERVICE.submit(() -> {
            while (true){
                for (ServerProcess serverProcess : FileManager.get().getAllServerProcess()) {
                    Screen screen = serverProcess.getScreen();
                    screen.read();
                }

                Thread.sleep(READ_INTERVAL);
            }
        });
    }

    private final ServerManagerLogger.Logger logger = new ServerManagerLogger.Logger("");
    private final ConcurrentLinkedDeque<String> printedLines = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<String> allPrintedLines = new ConcurrentLinkedDeque<>();
    private final Attribute screenColor = Attribute.TEXT_COLOR(Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256));
    private final ServerProcess serverProcess;

    private boolean isAttached = false;

    public Screen(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
        this.logger.setTag(serverProcess.getName());
        this.logger.setAttribute(this.screenColor);
        this.logger.setHideTimeAndThread(true);

        if(!isStarted){
            isStarted = true;
            logReaderRunnable();
        }
    }

    public void attachToScreen() {
        this.isAttached = true;
        ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Attached to screen '"+this.serverProcess.getName()+"'");
        this.read();
    }

    private void read(){
        if(this.serverProcess.getLogFile().exists()){
            try {
                List<String> newLines = new ArrayList<>();

                Files.lines(Paths.get(this.serverProcess.getLogFile().toURI())).forEach(line -> {
                    if(!this.allPrintedLines.contains(line)){
                        this.allPrintedLines.add(line);
                        newLines.add(line);
                    }

                    if(this.isAttached && !this.printedLines.contains(line)) {
                        this.printedLines.add(line);

                        if (line.contains("ERROR") || line.contains("SEVERE")) {
                            this.logger.error(line);
                        } else if (line.contains("DEBUG") || line.contains("OFF")) {
                            this.logger.debug(line);
                        } else if (line.contains("WARN") || line.contains("WARNING")) {
                            this.logger.warn(line);
                        } else {
                            this.logger.info(line);
                        }
                    }
                });

                if(!newLines.isEmpty()){
                    Plugins.fireEvent(new PluginFileManagerEvent.AsyncConsoleOutputEvent(this.serverProcess, newLines));
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }else{
            this.detachFromScreen();
        }
    }

    public void detachFromScreen(){
        if(this.isAttached){
            ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Detached from screen '"+this.serverProcess.getName()+"'");
        }
        this.isAttached = false;
        this.printedLines.clear();
    }

    public boolean isAttached() {
        return isAttached;
    }
}
