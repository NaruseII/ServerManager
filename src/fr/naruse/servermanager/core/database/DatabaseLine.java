package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.database.structure.ColumnStructure;
import fr.naruse.servermanager.core.database.structure.TableStructure;
import fr.naruse.servermanager.core.utils.MultiMap;

import java.util.*;

public class DatabaseLine implements IDatabaseLine {

    private final DatabaseTable table;
    private final Map<Integer, ColumnStructure> structureByIndexMap = new HashMap<>();
    private MultiMap<Object, ColumnStructure> structureObjectMap = new MultiMap<>();
    private final Map<String, ColumnStructure> columnStructureByNameMap = new HashMap<>();

    public DatabaseLine(DatabaseTable table, List<ColumnStructure> columnStructures) {
        this.table = table;
        for (int i = 0; i < columnStructures.size(); i++) {
            ColumnStructure columnStructure = columnStructures.get(i);
            this.structureByIndexMap.put(columnStructure.getColumnId(), columnStructure);
            this.columnStructureByNameMap.put(columnStructure.getColumnName(), columnStructure);
        }
    }

    private DatabaseLine(DatabaseTable table, MultiMap<Object, ColumnStructure> objectByColumnStructureMap) {
        this.table = table;
        this.structureObjectMap = objectByColumnStructureMap;
        for (ColumnStructure columnStructure : this.structureObjectMap.values()) {
            this.columnStructureByNameMap.put(columnStructure.getColumnName(), columnStructure);
            this.structureByIndexMap.put(columnStructure.getColumnId(), columnStructure);
        }
    }

    @Override
    public <T> T getValue(String columnName) {
        ColumnStructure columnStructure = this.columnStructureByNameMap.get(columnName);
        if(columnStructure == null){
            return null;
        }
        return (T) this.structureObjectMap.reverse().get(columnStructure);
    }

    @Override
    public String getString(String columnName) {
        return this.getValue(columnName);
    }

    @Override
    public Integer getInt(String columnName) {
        return this.getValue(columnName);
    }

    @Override
    public Double getDouble(String columnName) {
        return this.getValue(columnName);
    }

    @Override
    public Long getLong(String columnName) {
        return this.getValue(columnName);
    }

    @Override
    public void setValue(String columnName, Object value) {
        ColumnStructure columnStructure = this.columnStructureByNameMap.get(columnName);
        if(columnStructure == null){
            return;
        }

        this.structureObjectMap.put(value, columnStructure);
    }

    @Override
    public Set getAllValues() {
        return new HashSet<>(this.structureObjectMap.keySet());
    }

    @Override
    public TableStructure getTableStructure() {
        return this.table.getTableStructure();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        for (String name : this.table.getTableStructure().getAllNames()) {
            Object object = this.getValue(name);
            if(object == null){
                continue;
            }

            map.put(name, object);
        }

        return map;
    }

    public static class Builder {

        private MultiMap<Object, ColumnStructure> structureObjectMap = new MultiMap<>();

        public static Builder init(){
            return new Builder();
        }

        public Builder addObject(ColumnStructure columnStructure, Object object){
            this.structureObjectMap.put(columnStructure.transform(object), columnStructure);
            return this;
        }

        public DatabaseLine build(DatabaseTable table){
            return new DatabaseLine(table, this.structureObjectMap);
        }

    }
}
