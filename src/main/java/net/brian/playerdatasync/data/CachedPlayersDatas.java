package net.brian.playerdatasync.data;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.data.CachedTable;
import net.brian.playerdatasync.events.PlayerDataFetchComplete;
import net.brian.playerdatasync.data.databases.DatabaseManager;
import net.brian.playerdatasync.data.gson.QuitProcessable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CachedPlayersDatas implements Listener {

    private final PlayerDataSync databaseAPI = PlayerDataSync.getInstance();

    public HashMap<Class<?>, CachedTable<?>> tableMap = new HashMap<>();


    private final DatabaseManager database = PlayerDataSync.getInstance().getDbManager();

    private final List<Player> loaded = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CompletableFuture.runAsync(()->{
            tableMap.forEach((dataClass, cachedTable) -> {
                Object object = database.getData(uuid, dataClass);
                if(object != null){
                    database.setSaved(dataClass,uuid,false);
                    cachedTable.cache(uuid,object);
                    PlayerDataSync.log("loaded "+dataClass.getName()+" for "+event.getPlayer().getName());
                }
            });
        }).thenRun(()->{
            if(player.isOnline()){
                loaded.add(player);
                Bukkit.getPluginManager().callEvent(new PlayerDataFetchComplete(player));
            }
        });
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        loaded.remove(event.getPlayer());
        UUID uuid = event.getPlayer().getUniqueId();
        if(!PlayerDataSync.isDisabling){
            Bukkit.getScheduler().runTaskAsynchronously(databaseAPI,()->{
                tableMap.forEach((dataClass, cachedTable) -> {
                    Object data = cachedTable.getData(uuid);
                    cachedTable.unregister(uuid);
                    database.setData(cachedTable.getId(),uuid,data);
                    database.setSaved(dataClass,uuid,true);
                    if(data instanceof QuitProcessable){
                        Bukkit.getScheduler().runTask(PlayerDataSync.getInstance(),()->{
                            QuitProcessable quitProcessable = (QuitProcessable) data;
                            quitProcessable.onQuit();
                        });
                    }
                });
            });
        }
        else{
            tableMap.forEach((dataClass, cachedTable) -> {
                Object data = cachedTable.getData(uuid);
                database.setData(cachedTable.getId(),uuid,data);
                database.setSaved(dataClass,uuid,true);
                if(data instanceof QuitProcessable){
                    QuitProcessable quitProcessable = (QuitProcessable) data;
                    quitProcessable.onQuit();
                }
            });
        }
    }

    public <T> CachedTable<T> getTable(Class<T> dataClass){
        return (CachedTable<T>) tableMap.get(dataClass);
    }

    public boolean isLoaded(Player player){
        return loaded.contains(player);
    }

}
