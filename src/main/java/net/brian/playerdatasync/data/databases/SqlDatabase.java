package net.brian.playerdatasync.data.databases;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.data.PlayerData;
import net.brian.playerdatasync.data.CachedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public abstract class SqlDatabase extends Database {

    public SqlDatabase(){

    }

    public abstract Connection getConnection() throws SQLException;


    @Override
    public void register(String id, Class<?> dataClass) throws Exception {
        PlayerDataSync.log("Registered table " + id);
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+id+" (" +
                    "                uuid VARCHAR(255) NOT NULL UNIQUE," +
                    "                datajson LONGTEXT NOT NULL," +
                    "                complete TINYINT NOT NULL," +
                    "                primary key (uuid))")) {
                preparedStatement.executeUpdate();
            }
            classMap.put(dataClass, id);
            PlayerDataSync.getInstance().getPlayerDatas().tableMap.put(dataClass, new CachedTable<>(dataClass, id));
        }
    }

    @Override
    public <T> T getData(UUID uuid, Class<T> dataClass) throws Exception {
        String id = classMap.get(dataClass);
        try (Connection connection = getConnection()) {
            final String statement = "SELECT * FROM "+id+" WHERE uuid = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.setString(1, uuid.toString());
                final long start = System.currentTimeMillis();
                while (true){
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            break;
                        }
                        if (System.currentTimeMillis() - start > 600000 || resultSet.getInt("complete") == 1){
                            String dataJson = resultSet.getString("datajson");
                            if(dataJson == null) break;
                            final String setCompleteStatement = "UPDATE "+id+" SET complete = ? WHERE uuid = ?";
                            try (PreparedStatement preparedStatement2 = connection.prepareStatement(setCompleteStatement)){
                                preparedStatement2.setInt(1, 0);
                                preparedStatement.setString(2,uuid.toString());
                                preparedStatement2.executeUpdate();
                            }
                            return gson.fromJson(dataJson,dataClass);
                        }
                    }
                }
                T data;
                if(PlayerData.class.isAssignableFrom(dataClass)){
                    data = dataClass.getConstructor(UUID.class).newInstance(uuid);
                }
                else data = dataClass.newInstance();
                setData(id,uuid,data, false);
                return data;
            }
        }
    }

    public void setData(String id,UUID uuid,Object object, boolean setComplete) throws SQLException {
        if(object == null) return;
        try (Connection connection = getConnection()){
            final String statement = "INSERT INTO "+id+" (uuid,datajson,complete) VALUES (?,?,0) ON DUPLICATE KEY UPDATE datajson = ?";
            String dataJson = gson.toJson(object);
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)){
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setObject(2, dataJson);
                preparedStatement.setObject(3, dataJson);
                preparedStatement.executeUpdate();
            }
            if(setComplete){
                try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE "+id+" SET complete = ? WHERE uuid = ?")) {
                    preparedStatement.setInt(1, 1);
                    preparedStatement.setString(2, uuid.toString());
                    preparedStatement.executeUpdate();
                }
            }
        }
    }


}
