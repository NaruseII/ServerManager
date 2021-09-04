package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.ServerManager;
import fr.naruse.servermanager.core.connection.packet.PacketDatabase;
import fr.naruse.servermanager.core.database.structure.ColumnStructure;
import fr.naruse.servermanager.core.database.structure.TableStructure;
import fr.naruse.servermanager.packetmanager.database.Database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DatabaseAPI {

    private static final Cache cache = new Cache();

    public static IDatabaseTable newTable(String name, TableStructure tableStructure){
        if(tableStructure.getAllNames().isEmpty()){
            Database.LOGGER.error("TableStructure cannot be empty!");
            return null;
        }
        for (ColumnStructure structure : tableStructure.getAllColumnStructure()) {
            for (ColumnStructure columnStructure1 : tableStructure.getAllColumnStructure()) {
                if (structure.getColumnName().equals(columnStructure1)) {
                    Database.LOGGER.error("Different structure have the same name! '"+structure.getColumnName()+"'");
                    return null;
                }
            }
        }

        return new DatabaseTable(name, tableStructure, new HashSet<>());
    }

    public static void update(IDatabaseTable databaseTable){
        ServerManager.get().getConnectionManager().sendPacket(new PacketDatabase.Update(databaseTable));
    }

    public static void destroy(IDatabaseTable databaseTable){
        ServerManager.get().getConnectionManager().sendPacket(new PacketDatabase.Destroy(databaseTable.getName()));
    }

    public static ICache getCache() {
        return cache;
    }

    public static class Cache implements ICache{

        private final Map<String, IDatabaseTable> tableMap = new ConcurrentHashMap<>();

        public Map<String, IDatabaseTable> getTableMap() {
            return tableMap;
        }

        @Override
        public IDatabaseTable getDatabaseTable(String tableName) {
            return this.tableMap.get(tableName);
        }

        @Override
        public boolean databaseTableExists(String tableName) {
            return this.tableMap.containsKey(tableName);
        }

        public void updateCache(IDatabaseTable newTable){
            IDatabaseTable oldTable = this.getDatabaseTable(newTable.getName());
            if(oldTable == null){
                this.tableMap.put(newTable.getName(), newTable);
            }else{
                ((DatabaseTable) oldTable).replaceBy(newTable);
            }
        }

        public void destroy(String table){
            this.tableMap.remove(table);
        }
    }

    public interface ICache {

        IDatabaseTable getDatabaseTable(String tableName);

        boolean databaseTableExists(String tableName);

    }
}
