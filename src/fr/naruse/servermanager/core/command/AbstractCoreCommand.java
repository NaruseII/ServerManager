package fr.naruse.servermanager.core.command;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class AbstractCoreCommand {

    private final Map<String, ICommand> commandMap = new HashMap<>();

    public void run(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line;
            try {
                line = scanner.nextLine();
            } catch (NoSuchElementException e) {
                continue;
            }

            String[] args = line.split(" ");

            ICommand command = this.commandMap.get(args.length == 0 ? line : args[0]);
            if(command == null){
                this.help();
            }else{
                command.onCommand(line, args);
            }
        }
    }

    public abstract void help() ;

    protected void registerCommand(String startWith, ICommand iCommand){
        this.commandMap.put(startWith, iCommand);
    }

    public interface ICommand {

        void onCommand(String line, String[] args);

    }

    public class CommandStatus implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            ServerManager.get().printStatus();
        }
    }

    public class CommandGenerateSecretKey implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            ServerManagerLogger.info("Generation...");
            ServerManagerLogger.info("Key generated: "+ServerManager.get().generateNewSecretKey());
        }
    }

    public class CommandStop implements ICommand {

        @Override
        public void onCommand(String line, String[] args) {
            System.exit(0);
        }
    }
}
