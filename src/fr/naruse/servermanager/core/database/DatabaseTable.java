package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.config.Configuration;
import fr.naruse.servermanager.core.database.structure.ColumnStructure;
import fr.naruse.servermanager.core.database.structure.TableStructure;
import fr.naruse.servermanager.core.utils.Utils;

import java.util.*;

public class DatabaseTable implements IDatabaseTable {

    private String name;
    private TableStructure tableStructure;
    private Set<DatabaseLine> databaseLineSet;
    private HashMap<ColumnStructure, Map<Object, DatabaseLine>> objectListByColumn = new HashMap<>();

    private DatabaseTable(){ }

    public DatabaseTable(String name, TableStructure tableStructure, Set<DatabaseLine> databaseLineSet) {
        this.name = name;
        this.tableStructure = tableStructure;
        this.databaseLineSet = databaseLineSet;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<IDatabaseLine> getLine(String whereColumn, Object whereValue){
        Set<IDatabaseLine> set = this.getLines(whereColumn, whereValue);
        return set == null ? Optional.empty() : set.stream().findFirst();
    }

    @Override
    public Set<IDatabaseLine> getLines(String whereColumn, Object whereValue) {
        Set<IDatabaseLine> lineSet = new HashSet<>();
        ColumnStructure columnStructure = this.tableStructure.getColumnStructure(whereColumn);

        if(columnStructure == null){
            throw new RuntimeException(new NoSuchFieldException("No column found for name '"+whereColumn+"'"));
        }

        Map<Object, DatabaseLine> map = this.objectListByColumn.getOrDefault(columnStructure, new HashMap<>());

        Set<Object> set = new HashSet<>(map.keySet());

        for (Object object : set) {
            if(object.equals(whereValue)){
                lineSet.add(map.get(object));
            }
        }

        return lineSet;
    }

    @Override
    @Deprecated
    public IDatabaseLine getLine(Object... values){
        for (DatabaseLine databaseLine : new HashSet<>(this.databaseLineSet)) {
            boolean ret = false;
            for (Object value : values) {
                if(!databaseLine.getAllValues().contains(value)){
                    ret = false;
                    break;
                }
                ret = true;
            }
            if(ret){
                return databaseLine;
            }
        }
        return null;
    }

    @Override
    public IDatabaseLine newLine() {
        DatabaseLine databaseLine = new DatabaseLine(this, this.tableStructure.getAllColumnStructure());
        this.databaseLineSet.add(databaseLine);
        return databaseLine;
    }

    @Override
    public void removeLine(IDatabaseLine line) {
        this.databaseLineSet.remove(line);
    }

    @Override
    public Set<IDatabaseLine> getAllLines(){
        Set<IDatabaseLine> set = new HashSet<>(this.databaseLineSet);
        Set<IDatabaseLine> finalSet = new HashSet<>();
        set.stream().forEach(iDatabaseLine -> {
            if(!finalSet.contains(iDatabaseLine)){
                finalSet.add(iDatabaseLine);
            }
        });
        return finalSet;
    }

    @Override
    public TableStructure getTableStructure() {
        return this.tableStructure;
    }

    public void replaceBy(IDatabaseTable iDatabaseTable){
        DatabaseTable table = (DatabaseTable) iDatabaseTable;

        this.tableStructure = table.getTableStructure();
        this.databaseLineSet = table.getDatabaseLineSet();
        this.refreshObjectListMap();
    }

    private void setName(String name) {
        this.name = name;
    }

    private Set<DatabaseLine> getDatabaseLineSet() {
        return databaseLineSet;
    }

    private void setDatabaseLineSet(Set<DatabaseLine> databaseLineSet) {
        this.databaseLineSet = databaseLineSet;
        this.refreshObjectListMap();
    }

    private void setTableStructure(TableStructure tableStructure) {
        this.tableStructure = tableStructure;
    }

    private void refreshObjectListMap(){
        this.objectListByColumn.clear();
        for (ColumnStructure columnStructure : this.tableStructure.getAllColumnStructure()) {
            this.objectListByColumn.put(columnStructure, new HashMap<>());
        }

        for (DatabaseLine databaseLine : this.databaseLineSet) {
            for (ColumnStructure columnStructure : this.tableStructure.getAllColumnStructure()) {
                this.objectListByColumn.get(columnStructure).put(databaseLine.getValue(columnStructure.getColumnName()), databaseLine);
            }
        }
    }

    void putInObjectListMap(ColumnStructure columnStructure, Object object, DatabaseLine line){
        if(!this.objectListByColumn.containsKey(columnStructure)){
            this.objectListByColumn.put(columnStructure, new HashMap<>());
        }
        this.objectListByColumn.get(columnStructure).put(object, line);
    }

    @Override
    public String toString() {
        return serialize();
    }

    @Override
    public String serialize(){
        Map<String, Object> map = new HashMap<>();

        map.put("name", this.getName());
        map.put("structureColumns", this.getTableStructure().serializeColumns());
        map.put("structureProperties", this.getTableStructure().serializeProperties());

        List<Map<String, Object>> list = new ArrayList<>();
        for (IDatabaseLine line : this.getAllLines()) {
            list.add(((DatabaseLine) line).serialize());
        }
        map.put("data", list);

        return Utils.GSON.toJson(map);
    }

    public static class Builder{

        public static DatabaseTable deserialize(Configuration configuration){
            DatabaseTable table = new DatabaseTable();
            String name = configuration.get("name");

            table.setName(name);
            if(!configuration.contains("structureColumns")){
                return null;
            }

            // Loading structure
            Configuration.ConfigurationSection propertiesSection = configuration.getSection("structureProperties");

            List<Configuration> columnsStructureList = configuration.getMainSection().getSectionList("structureColumns");
            if(columnsStructureList == null){
                return null;
            }
            TableStructure tableStructure = new TableStructure();
            for (Configuration columnConfig : columnsStructureList) {
                int columnId = columnConfig.getInt("id");
                String columnName = columnConfig.get("name");
                ValueType valueType = ValueType.valueOf(columnConfig.get("valueType"));
                tableStructure.registerColumnStructure(new ColumnStructure(columnId, columnName, valueType));
            }

            List<String> list = propertiesSection.get("targetTemplates");
            tableStructure.registerTarget(list.toArray(new String[0]));

            table.setTableStructure(tableStructure);

            // Loading Data
            Set<DatabaseLine> databaseLineSet = new HashSet<>();
            List<String> allNames = tableStructure.getAllNames();
            List<Configuration> dataList = configuration.getMainSection().getSectionList("data");

            if(dataList != null){

                for (Configuration dataConfiguration : dataList) {
                    // New Line

                    DatabaseLine.Builder builder = DatabaseLine.Builder.init();

                    for (String columnName : allNames) {
                        if(dataConfiguration.contains(columnName)){
                            builder.addObject(tableStructure.getColumnStructure(columnName), dataConfiguration.get(columnName));
                        }
                    }

                    DatabaseLine line = builder.build(table);
                    databaseLineSet.add(line);
                }

            }
            table.setDatabaseLineSet(databaseLineSet);
            return table;
        }

    }
}
