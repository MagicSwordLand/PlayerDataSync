package net.brian.playerdatasync.data.databases;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.data.PlayerData;
import net.brian.playerdatasync.data.CachedTable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Database extends DatabaseManager {

    protected Connection connection;

    public Database(){
        setUp();
    }

    public abstract Connection getConnection();
    abstract void setUp();


    @Override
    public void register(String id, Class<?> dataClass) {
        PlayerDataSync.log("Registered table " + id);
        String statement = "CREATE TABLE IF NOT EXISTS "+id+" (" +
                "                uuid VARCHAR(255) NOT NULL UNIQUE," +
                "                datajson LONGTEXT NOT NULL," +
                "                complete TINYINT NOT NULL,"+
                "                primary key (uuid))";
        executeUpdate(statement);
        classMap.put(dataClass,id);
        PlayerDataSync.getInstance().getPlayerDatas().tableMap.put(dataClass,new CachedTable<>(dataClass,id));
    }

    @Override
    public <T> T getData(UUID uuid, Class<T> dataClass) {
        String id = classMap.get(dataClass);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM "+id+" WHERE uuid = ? LIMIT 1");
            preparedStatement.setString(1,uuid.toString());
            long currentTime = System.currentTimeMillis();
            while (true){
                ResultSet resultSet = preparedStatement.executeQuery();
                if(!resultSet.next()) break;
                if(System.currentTimeMillis() - currentTime > 10000 || resultSet.getInt("complete") == 1){
                    String dataJson = resultSet.getString("datajson");
                    if(dataJson == null) break;
                    return gson.fromJson(dataJson,dataClass);
                }
            }
            T data;
            if(PlayerData.class.isAssignableFrom(dataClass)){
                data = dataClass.getConstructor(UUID.class).newInstance(uuid);
            }
            else data = dataClass.newInstance();
            setData(id,uuid,data);
            return data;
        } catch (SQLException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        PlayerDataSync.log(ChatColor.RED+"Could not cache data from "+id+ " for player "+ Bukkit.getPlayer(uuid).getName());
        return null;
    }

    @Override
    public void setData(String id,UUID uuid,Object object){
        if(object == null) return;
        try {
            String dataJson = gson.toJson(object);
            PreparedStatement statement = getConnection().prepareStatement("INSERT INTO "+id+" (uuid,datajson,complete) VALUES (?,?,0)" +
                    " ON DUPLICATE KEY UPDATE datajson = ?");
            statement.setString(1,uuid.toString());
            statement.setString(2,dataJson);
            statement.setString(3,dataJson);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSaved(Class<?> dataClass, UUID uuid, boolean saved){
        String id = classMap.get(dataClass);
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE "+id+" SET complete = ? WHERE uuid = ?");
            preparedStatement.setInt(1,saved ? 1:0);
            preparedStatement.setString(2,uuid.toString());
            PlayerDataSync.log(preparedStatement.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement getInsertString(String id,UUID uuid,String dataJson){
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT INTO "+id+" (uuid,datajson,complete) VALUES (?,?,1)" +
                    " ON DUPLICATE KEY UPDATE datajson = ?");
            statement.setString(1,uuid.toString());
            statement.setString(2,dataJson);
            statement.setString(3,dataJson);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }



    private int executeUpdate(String string){
        int success=0;
        try {
            success = getConnection().prepareStatement(string).executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    private ResultSet executeQuery(String statement){
        ResultSet resultSet = null;
        try {
            resultSet = getConnection().prepareStatement(statement).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }


    private PreparedStatement getStatement(String statement){
        try {
            return connection.prepareStatement(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
