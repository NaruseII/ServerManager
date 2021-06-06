package fr.naruse.servermanager.core.callback;

import fr.naruse.servermanager.core.database.Database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Callback<T> {

    public static final Map<Integer, Callback> CALLBACK_ID_MAP = new HashMap<>();

    public static void process(int id, Database.DataObject[] array){
        Callback callback = CALLBACK_ID_MAP.remove(id);
        if(callback != null){
            if(array == null){
                callback.runSingle(null);
                callback.runPlural(null);
            }else{
                List list = Arrays.asList(array.clone());
                if(list.size() == 1){
                    callback.runSingle(list.get(0));
                }
                callback.runPlural(list);
            }
        }
    }

    public abstract void runPlural(List<T> values) ;

    public abstract void runSingle(T value) ;

}


