package fr.naruse.servermanager.filemanager;

import com.diogonunes.jcolor.Attribute;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Screen {

    private final ServerManagerLogger.Logger logger = new ServerManagerLogger.Logger("");
    private final ConcurrentLinkedDeque<String> printedLines = new ConcurrentLinkedDeque<>();
    private final Attribute screenColor = Attribute.TEXT_COLOR(Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256), Utils.RANDOM.nextInt(256));
    private final ServerProcess serverProcess;
    private boolean isAttached = false;

    public Screen(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
        this.logger.setTag(serverProcess.getName());
        this.logger.setAttribute(this.screenColor);
        this.logger.setHideTimeAndThread(true);
    }

    public void attachToScreen() {
        this.isAttached = true;
        if(FileManager.EXECUTOR_SERVICE.isTerminated() || FileManager.EXECUTOR_SERVICE.isShutdown()){
            return;
        }
        ServerManagerLogger.info(Attribute.CYAN_TEXT(), "Attached to screen '"+this.serverProcess.getName()+"'");
        FileManager.EXECUTOR_SERVICE.submit(() -> this.screenRunnable());
    }

    private void screenRunnable(){
        while (true){

            if(this.serverProcess.getLogFile().exists()){
                try {
                    Files.lines(Paths.get(this.serverProcess.getLogFile().toURI())).forEach(line -> {
                        if(!this.printedLines.contains(line)){
                            this.printedLines.add(line);
                            if(line.contains("ERROR") || line.contains("SEVERE")){
                                this.logger.error(line);
                            }else if(line.contains("DEBUG") || line.contains("OFF")){
                                this.logger.debug(line);
                            }else if(line.contains("WARN") || line.contains("WARNING")){
                                this.logger.warn(line);
                            }else{
                                this.logger.info(line);
                            }
                        }
                    });
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }else{
                this.detachFromScreen();
            }

            this.serverProcess.sleep(5000);
            if(!this.isAttached){
                break;
            }
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
