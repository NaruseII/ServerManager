package fr.naruse.servermanager.core.command;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;

import java.util.*;

public abstract class AbstractCoreCommand {

    private static AbstractCoreCommand instance;
    public static AbstractCoreCommand get() {
        return instance;
    }

    public AbstractCoreCommand() {
        instance = this;
    }

    private final Map<String, ICommand> commandMap = new HashMap<>();
    protected final List<String> pluginCommandUsages = new ArrayList<>();

    public void run(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line;
            try {
                line = scanner.nextLine();
            } catch (NoSuchElementException e) {
                continue;
            }

            this.execute(line);
        }
    }

    public abstract void help() ;

    public void registerCommand(String startWith, ICommand iCommand){
        this.registerCommand(startWith, iCommand, null);
    }

    public void registerCommand(String startWith, ICommand iCommand, String usage){
        this.commandMap.put(startWith, iCommand);
        if(usage != null){
            this.registerUsage(usage);
        }
    }

    public void registerUsage(String usage){
        this.pluginCommandUsages.add(usage);
    }

    public void execute(String commandLine){
        String[] args = commandLine.split(" ");

        ICommand command = this.commandMap.get(args.length == 0 ? commandLine : args[0]);
        if(command == null){
            this.help();
        }else{
            command.onCommand(commandLine, args);
        }
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
