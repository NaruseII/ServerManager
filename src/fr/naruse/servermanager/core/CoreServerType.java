package fr.naruse.servermanager.core;

public enum CoreServerType {

    BUNGEE_MANAGER,
    BUKKIT_MANAGER,
    FILE_MANAGER,
    PACKET_MANAGER,
    ;

    public boolean is(CoreServerType... types) {
        for (CoreServerType type : types) {
            if(this == type){
                return true;
            }
        }
        return false;
    }
}
