package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.database.structure.TableStructure;

import java.util.Set;

public interface IDatabaseLine {

    <T> T getValue(String columnName);

    String getString(String columnName);

    Integer getInt(String columnName);

    Double getDouble(String columnName);

    Long getLong(String columnName);

    void setValue(String columnName, Object value);

    Set getAllValues();

    TableStructure getTableStructure();

}
