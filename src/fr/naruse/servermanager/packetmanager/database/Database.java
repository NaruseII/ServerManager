package fr.naruse.servermanager.packetmanager.database;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketDatabase;
import fr.naruse.servermanager.core.database.DatabaseAPI;
import fr.naruse.servermanager.core.database.DatabaseTable;
import fr.naruse.servermanager.core.database.IDatabaseTable;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;

import java.io.*;
import java.util.HashSet;
import java.util.Map;

public class Database {

    public static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("Database");

    private final File databaseFolder = new File("database");

    public Database() {
        try {
            this.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() throws IOException {
        LOGGER.info("Loading Database...");

        if(!this.databaseFolder.exists()){
            this.databaseFolder.mkdirs();
        }

        if(this.databaseFolder.listFiles() != null){
            for (File file : this.databaseFolder.listFiles()) {
                Configuration configuration = new Configuration(file, false);

                DatabaseTable table = DatabaseTable.Builder.deserialize(configuration);
                if(table != null){
                    this.getTableMap().put(table.getName(), table);
                }
            }
        }

        LOGGER.info(this.getTableMap().size()+" tables found");
        LOGGER.info("Database loaded");
    }

    public void update(IDatabaseTable table) {
        this.getTableMap().put(table.getName(), table);
        this.save();

        for (Server server : ServerList.getAll(false)) {
            server.sendPacket(new PacketDatabase.UpdateCache(new HashSet<>(this.getTableMap().values())));
        }
    }

    public void save(){
        Database.LOGGER.debug("Saving...");
        for (IDatabaseTable table : new HashSet<>(this.getTableMap().values())) {
            String json = table.serialize();

            Configuration configuration = new Configuration(json);
            configuration.save(new File(this.databaseFolder, table.getName()+".json"));
        }
        Database.LOGGER.debug(this.getTableMap().size()+" tables saved");
    }

    public void destroy(IDatabaseTable table){
        this.getTableMap().remove(table.getName());

        File file = new File(this.databaseFolder, table.getName()+".json");
        if(file.exists()){
            Utils.delete(file);
        }
    }

    private Map<String, IDatabaseTable> getTableMap(){
        return ((DatabaseAPI.Cache) DatabaseAPI.getCache()).getTableMap();
    }
}
