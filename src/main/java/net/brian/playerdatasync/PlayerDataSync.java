package net.brian.playerdatasync;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import net.brian.playerdatasync.data.CachedPlayersDatas;
import net.brian.playerdatasync.data.databases.Database;
import net.brian.playerdatasync.data.databases.FileDatabase;
import net.brian.playerdatasync.data.databases.MysqlDatabase;
import net.brian.playerdatasync.data.databases.SqlDatabase;
import net.brian.playerdatasync.data.gson.PostProcessable;

import net.brian.playerdatasync.test.TestService;
import net.brian.playerdatasync.util.ItemStackSerializer;
import net.brian.playerdatasync.util.nms.Version_1_16_R3;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerDataSync extends JavaPlugin {

    private CachedPlayersDatas cachedPlayersDatas;
    private Database dbManager;
    private static PlayerDataSync instance;
    private static HashMap<Class<?>,Object> typeAdapters;
    private ItemStackSerializer itemStackSerializer;

    private static Gson gson;

    public static boolean isDisabling = false;

    @Override
    public void onLoad(){
        getMcVersion();
    }



    @Override
    public void onEnable() {
        instance = this;
        gson = new GsonBuilder().registerTypeAdapterFactory(new PostProcessable.PostProcessingEnabler()).create();
        saveDefaultConfig();

        Config.input(getConfig());

        if (Config.useMysql()) {
            dbManager = new MysqlDatabase();
            log("Using Mysql Database");
        } else {
            dbManager = new FileDatabase(this);
            log("Using Flat File Database");
        }


        cachedPlayersDatas = new CachedPlayersDatas();
        getServer().getPluginManager().registerEvents(cachedPlayersDatas, this);

        new TestService();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        isDisabling = true;
        cachedPlayersDatas.tableMap.forEach((id, table)->{
            table.cacheData.forEach((uuid,data)->{
                try {
                    dbManager.setData(table.getId(),uuid,data, true);
                } catch (Exception ignored) {
                }
            });
        });
    }

    public void register(String id,Class<?> dataClass){
        try {
            dbManager.register(id,dataClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> getData(UUID uuid, Class<T> dataClass){
        return Optional.of(cachedPlayersDatas.getTable(dataClass).getData(uuid));
    }

    public static void log(String message){
        Logger.getLogger("PlayerDataSync").log(Level.INFO,message);
    }

    public static Executor getMainExecutor(){
        return (runnable) -> {
            Bukkit.getScheduler().runTask(PlayerDataSync.getInstance(), runnable);
        };
    }

    public static PlayerDataSync getInstance() {
        return instance;
    }

    public Database getDbManager(){
        return dbManager;
    }

    public CachedPlayersDatas getPlayerDatas(){
        return cachedPlayersDatas;
    }

    public static boolean isLoaded(Player player){
        return getInstance().getPlayerDatas().isLoaded(player);
    }


    private boolean getMcVersion() {
        String[] var1 = Bukkit.getBukkitVersion().split("-");
        String var2 = var1[0];
        if (!var2.matches("1.17") && !var2.matches("1.17.1")) {
            if (!var2.matches("1.16.4") && !var2.matches("1.16.5")) {
                log("You're using an unsupported version.");
            }else {
                itemStackSerializer = new Version_1_16_R3();
            }
        }else {

        }
        return true;
    }

    public Connection getConnection(){
        if(dbManager instanceof SqlDatabase){
            try {
                ((SqlDatabase) dbManager).getConnection();
            } catch (SQLException e) {
                return null;
            }
        }
        return null;
    }

    public static Gson getGson(){
        return gson;
    }



}
