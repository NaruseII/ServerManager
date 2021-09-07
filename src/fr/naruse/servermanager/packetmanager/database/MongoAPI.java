package fr.naruse.servermanager.packetmanager.database;

import com.mongodb.client.*;
import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.logging.ServerManagerLogger;
import fr.naruse.servermanager.core.utils.CustomRunnable;
import org.bson.Document;

import java.util.concurrent.*;

public class MongoAPI {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final ServerManagerLogger.Logger LOGGER = new ServerManagerLogger.Logger("MongoDB");
    private static MongoAPI instance;

    public static MongoAPI get() {
        return instance;
    }

    private final MongoClient mongoClient;
    private final Configuration configuration;
    private boolean isMainThread = false;

    public MongoAPI(Configuration configuration) {
        this.configuration = configuration;
        instance = this;
        LOGGER.info("Starting...");
        this.mongoClient = MongoClients.create((String) configuration.get("url"));


        LOGGER.info("Started");
    }

    public void getDatabase(String databaseName, CustomRunnable<MongoDatabase> response){
        this.submit(() -> response.run(this.mongoClient.getDatabase(databaseName)));
    }

    public void getCollection(String databaseName, String collectionName, CustomRunnable<MongoCollection<Document>> response){
        this.getDatabase(databaseName, database -> response.run(database.getCollection(collectionName)));
    }

    public void getCollection(String collectionName, CustomRunnable<MongoCollection> response){
        this.getDatabase(this.configuration.get("databaseName"), database -> response.run(database.getCollection(collectionName)));
    }

    public void getCollection(MongoDatabase database, String collectionName, CustomRunnable<MongoCollection<Document>> response){
        this.submit(() -> response.run(database.getCollection(collectionName)));
    }

    public void createCollection(String collectionName){
        this.getDatabase(this.configuration.get("databaseName"), database -> database.createCollection(collectionName));
    }

    public void createCollection(String databaseName, String collectionName){
        this.getDatabase(databaseName, database -> database.createCollection(collectionName));
    }

    public void createCollection(MongoDatabase database, String collectionName){
        this.submit(() -> database.createCollection(collectionName));
    }

    public void getAllCollectionNames(String databaseName, CustomRunnable<MongoIterable<String>> response){
        this.getDatabase(databaseName, database -> response.run(database.listCollectionNames()));
    }

    public void getAllCollectionNames(CustomRunnable<MongoIterable<String>> response){
        this.getDatabase(this.configuration.get("databaseName"), database -> response.run(database.listCollectionNames()));
    }

    public void getAllCollectionNames(MongoDatabase database, CustomRunnable<MongoIterable<String>> response){
        this.submit(() -> response.run(database.listCollectionNames()));
    }

    public void dropCollection(String collectionName){
        this.getCollection(collectionName, collection -> collection.drop());
    }

    public void dropCollection(String databaseName, String collectionName){
        this.getCollection(databaseName, collectionName, collection -> collection.drop());
    }

    public void dropCollection(MongoDatabase database, String collectionName){
        this.getCollection(database, collectionName, collection -> collection.drop());
    }

    public void setMainThread(boolean set){
        this.isMainThread = set;
    }

    private void submit(Runnable runnable){
        if(this.isMainThread){
            runnable.run();
            return;
        }
        Future future = EXECUTOR.submit(runnable);
        EXECUTOR.submit(() -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void shutdown(){
        LOGGER.info("Shutting down threads...");
        this.isMainThread = true;
        EXECUTOR.shutdown();
        long startEndMillis = System.currentTimeMillis();
        while (!EXECUTOR.isTerminated()){
            if(System.currentTimeMillis()-startEndMillis > 5000){
                LOGGER.info("Killing interrupted remaining threads...");
                EXECUTOR.shutdownNow();
                break;
            }
        }
    }
}
