package net.brian.playerdatasync.data;

import java.util.HashMap;
import java.util.UUID;

public class CachedTable<T> {

    Class<T> dataClass;
    String id;

    public final HashMap<UUID,T> cacheData = new HashMap<>();

    public void cache(UUID uuid, Object object){
        cacheData.put(uuid, (T) object);
    }

    public T getData(UUID uuid){
        return cacheData.get(uuid);
    }


    public CachedTable(Class<T> dataClass, String id){
        this.dataClass = dataClass;
        this.id =id;
    }

    public Class<T> getDataClass(){
        return dataClass;
    }

    public String getId() {
        return id;
    }

    public void unregister(UUID uuid){
        cacheData.remove(uuid);
    }
}
