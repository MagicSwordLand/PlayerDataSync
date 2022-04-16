package net.brian.playerdatasync.data.databases;



import com.google.gson.Gson;
import net.brian.playerdatasync.PlayerDataSync;

import java.util.HashMap;
import java.util.UUID;

public abstract class DatabaseManager {
    protected final HashMap<Class<?>,String> classMap = new HashMap<>();
    protected Gson gson = PlayerDataSync.getGson();

    public DatabaseManager(){

    }

    public abstract void register(String id,Class<?> dataClass);

    public abstract <T> T getData(UUID uuid, Class<T> dataClass);

    public abstract void setData(String id, UUID uuid, Object object);

    public abstract void setSaved(Class<?> dataClass, UUID uuid, boolean saved);

}
