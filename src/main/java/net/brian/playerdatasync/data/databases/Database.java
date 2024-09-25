package net.brian.playerdatasync.data.databases;



import com.google.gson.Gson;
import net.brian.playerdatasync.PlayerDataSync;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public abstract class Database {
    protected final HashMap<Class<?>,String> classMap = new HashMap<>();
    protected Gson gson = PlayerDataSync.getGson();

    public Database(){

    }

    public abstract void register(String id,Class<?> dataClass) throws Exception;

    public abstract <T> T getData(UUID uuid, Class<T> dataClass)  throws Exception;

    public abstract void setData(String id, UUID uuid, Object object, boolean setComplete)  throws Exception;

}
