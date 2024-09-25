package net.brian.playerdatasync.data.databases;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.data.PlayerData;
import net.brian.playerdatasync.data.CachedTable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class FileDatabase extends Database {

    PlayerDataSync plugin;

    public FileDatabase(PlayerDataSync plugin){
        this.plugin = plugin;
    }

    @Override
    public void register(String id, Class<?> dataClass) {
        File folder = new File(plugin.getDataFolder()+"/data/"+id);
        folder.mkdirs();
        classMap.put(dataClass,id);
        PlayerDataSync.getInstance().getPlayerDatas().tableMap.put(dataClass,new CachedTable<>(dataClass,id));
    }



    @Override
    public <T> T getData(UUID uuid, Class<T> dataClass) {
        String id = classMap.get(dataClass);
        File file = new File(plugin.getDataFolder()+"/data/"+id+"/"+uuid);
        if(file.exists()) {
            long currentTime = System.currentTimeMillis();
            FileConfiguration dataFile = YamlConfiguration.loadConfiguration(file);
            while (true){
                String dataJson = dataFile.getString("data");
                if(dataJson == null) break;
                if(dataFile.getBoolean("complete")){
                    return gson.fromJson(dataJson, dataClass);
                }
                if(System.currentTimeMillis() - currentTime > 10000){
                    return gson.fromJson(dataJson, dataClass);
                }
            }
        }
        try {
            T data;
            if(PlayerData.class.isAssignableFrom(dataClass)){
                data = dataClass.getConstructor(UUID.class).newInstance(uuid);
            }
            else{
                data = dataClass.newInstance();
            }
            return data;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        PlayerDataSync.log(ChatColor.RED+"Could not cache data from "+id+ " for player "+ Bukkit.getPlayer(uuid).getName());
        return null;
    }

    @Override
    public void setData(String id, UUID uuid, Object object, boolean setComplete) {
        if(object == null) return;
        File file = new File(plugin.getDataFolder()+"/data/"+id+"/"+uuid);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
        String data = gson.toJson(object);
        ymlFile.set("data",data);
        try {
            ymlFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(setComplete){
            setSaved(id, uuid, true);
        }
    }

    public void setSaved(String id, UUID uuid, boolean saved) {
        File file = new File(plugin.getDataFolder()+"/data/"+id+"/"+uuid);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileConfiguration ymlFile = YamlConfiguration.loadConfiguration(file);
            ymlFile.set("complete",saved);
            ymlFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
