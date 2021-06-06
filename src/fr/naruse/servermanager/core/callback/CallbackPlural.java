package fr.naruse.servermanager.core.callback;

import fr.naruse.servermanager.core.database.Database;

public abstract class CallbackPlural extends Callback<Database.DataObject> {

    @Override
    public void runSingle(Database.DataObject value) { }

}
