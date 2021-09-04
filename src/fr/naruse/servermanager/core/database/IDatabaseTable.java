package fr.naruse.servermanager.core.database;

import fr.naruse.servermanager.core.database.structure.TableStructure;

import java.util.Optional;
import java.util.Set;

public interface IDatabaseTable {

    String getName();

    Optional<IDatabaseLine> getLine(String whereColumn, Object whereValue);

    Set<IDatabaseLine> getLines(String whereColumn, Object whereValue);

    @Deprecated
    IDatabaseLine getLine(Object... values);

    IDatabaseLine newLine();

    void removeLine(IDatabaseLine line);

    Set<IDatabaseLine> getAllLines();

    TableStructure getTableStructure();

    String serialize();

}
