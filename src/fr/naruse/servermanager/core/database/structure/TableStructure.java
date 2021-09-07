package fr.naruse.servermanager.core.database.structure;

import java.util.*;

public class TableStructure {

    private final Map<String, ColumnStructure> columnStructureMap = new HashMap<>();
    private final Set<String> targetTemplates = new HashSet<>();

    public TableStructure registerColumnStructure(ColumnStructure columnStructure){
        this.columnStructureMap.put(columnStructure.getColumnName(), columnStructure);
        return this;
    }

    public TableStructure registerTarget(String... templateTarget){
        this.targetTemplates.addAll(Arrays.asList(templateTarget));
        return this;
    }

    public ColumnStructure getColumnStructure(int index){
        return new ArrayList<>(this.columnStructureMap.values()).get(index);
    }

    public ColumnStructure getColumnStructure(String columnName){
        return this.columnStructureMap.get(columnName);
    }

    public List<String> getAllNames(){
        return new ArrayList<>(this.columnStructureMap.keySet());
    }

    public List<ColumnStructure> getAllColumnStructure(){
        return new ArrayList<>(this.columnStructureMap.values());
    }

    public Set<String> getTargetTemplates() {
        return targetTemplates;
    }

    public Map serialize(){
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        for (ColumnStructure columnStructure : new HashSet<>(columnStructureMap.values())) {
            list.add(columnStructure.serialize());
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("targetTemplates", this.targetTemplates);

        map.put("columns", list);
        map.put("properties", properties);

        return map;
    }
}
