package net.brian.playerdatasync.data;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.events.PlayerDataFetchComplete;
import net.brian.playerdatasync.data.databases.Database;
import net.brian.playerdatasync.data.gson.QuitProcessable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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


    private final Database database = PlayerDataSync.getInstance().getDbManager();

    private final List<Player> loaded = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CompletableFuture.runAsync(()->{
            tableMap.forEach((dataClass, cachedTable) -> {
                Object object;
                try {
                    object = database.getData(uuid, dataClass);
                    cachedTable.cache(uuid,object);
                    PlayerDataSync.log("loaded "+dataClass.getName()+" for "+event.getPlayer().getName());
                } catch (Exception e) {
                    PlayerDataSync.log(ChatColor.RED+"Could not cache data "+dataClass.getName()+ " for player "+ player.getName());
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
                    try {
                        database.setData(cachedTable.getId(),uuid,data, true);
                        if(data instanceof QuitProcessable){
                            Bukkit.getScheduler().runTask(PlayerDataSync.getInstance(),()->{
                                QuitProcessable quitProcessable = (QuitProcessable) data;
                                quitProcessable.onQuit();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }
        else{
            tableMap.forEach((dataClass, cachedTable) -> {
                Object data = cachedTable.getData(uuid);

                try {
                    database.setData(cachedTable.getId(),uuid,data, true);
                    if(data instanceof QuitProcessable){
                        QuitProcessable quitProcessable = (QuitProcessable) data;
                        quitProcessable.onQuit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
