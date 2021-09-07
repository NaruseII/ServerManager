package fr.naruse.servermanager.packetmanager.database;

import com.mongodb.BasicDBObject;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.connection.packet.PacketDatabase;
import fr.naruse.servermanager.core.database.DatabaseAPI;
import fr.naruse.servermanager.core.database.DatabaseTable;
import fr.naruse.servermanager.core.database.IDatabaseTable;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.server.Server;
import fr.naruse.servermanager.core.server.ServerList;
import fr.naruse.servermanager.core.utils.Utils;
import org.bson.Document;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        if(MongoAPI.get() != null){ // MongoDB save
            MongoAPI.get().setMainThread(true);

            MongoAPI.get().getAllCollectionNames(iterable -> {
                for (String collectionName : iterable) {
                    MongoAPI.get().getCollection(collectionName, collection -> {
                        Document document = (Document) collection.find().first();
                        if(document != null){
                            this.load(new Configuration(document.toJson()));
                        }
                    });
                }
            });

            MongoAPI.get().setMainThread(false);
        }else{ // JSON save
            if(!this.databaseFolder.exists()){
                this.databaseFolder.mkdirs();
            }

            if(this.databaseFolder.listFiles() != null){
                for (File file : this.databaseFolder.listFiles()) {
                    this.load(new Configuration(file, false));
                }
            }
        }

        LOGGER.info(this.getTableMap().size()+" tables found");
        LOGGER.info("Database loaded");
    }

    private void load(Configuration configuration){
        DatabaseTable table = DatabaseTable.Builder.deserialize(configuration);
        if(table != null){
            this.getTableMap().put(table.getName(), table);
        }
    }

    public void update(IDatabaseTable table, String serverSender) {
        IDatabaseTable oldTable = this.getTableMap().get(table.getName());
        if(oldTable == null){
            this.getTableMap().put(table.getName(), table);
        }else{
            ((DatabaseTable) oldTable).replaceBy(table);
        }
        this.save();

        IDatabaseTable finalTable = this.getTableMap().get(table.getName());

        for (Server server : this.filterServers(finalTable)) {
            if(!server.getName().equals(serverSender)){
                server.sendPacket(new PacketDatabase.Update(finalTable));
            }
        }
    }

    public void save(){
        Database.LOGGER.debug("Saving...");

        if(MongoAPI.get() != null){ // MongoDB save
            for (IDatabaseTable table : new HashSet<>(this.getTableMap().values())) {
                String json = table.serialize();

                MongoAPI.get().getCollection(table.getName(), collection -> {
                    if(collection == null){
                        MongoAPI.get().createCollection(table.getName());
                    }
                    collection.deleteMany(new BasicDBObject());
                    collection.insertOne(Document.parse(json));
                });
            }
        }else{ // JSON save
            for (IDatabaseTable table : new HashSet<>(this.getTableMap().values())) {
                String json = table.serialize();

                Configuration configuration = new Configuration(json);
                configuration.save(new File(this.databaseFolder, table.getName()+".json"));
            }
        }

        Database.LOGGER.debug(this.getTableMap().size()+" tables saved");
    }

    public void destroy(String table){
        this.getTableMap().remove(table);

        File file = new File(this.databaseFolder, table+".json");
        if(file.exists()){
            Utils.delete(file);
        }

        for (Server server : ServerList.getAll(false)) {
            server.sendPacket(new PacketDatabase.Destroy(table));
        }
    }

    private Set<Server> filterServers(IDatabaseTable table){
        Set<Server> set = ServerList.getAll(false);

        if(!table.getTableStructure().getTargetTemplates().isEmpty()){
            set = set.stream().filter(server -> {
                for (String targetTemplate : table.getTableStructure().getTargetTemplates()) {
                    if(server.getName().startsWith(targetTemplate)){
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toSet());
        }

        return set;
    }

    public void destroyAll() {
        for (String name : new HashSet<>(this.getTableMap().keySet())) {
            this.destroy(name);
        }
    }

    private Map<String, IDatabaseTable> getTableMap(){
        return ((DatabaseAPI.Cache) DatabaseAPI.getCache()).getTableMap();
    }
}
