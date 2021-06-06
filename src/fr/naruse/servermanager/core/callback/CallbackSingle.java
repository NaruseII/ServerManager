package fr.naruse.servermanager.core.callback;

import fr.naruse.servermanager.core.database.Database;

import java.util.List;

public abstract class CallbackSingle extends Callback<Database.DataObject> {

    @Override
    public void runPlural(List<Database.DataObject> values) { }

}