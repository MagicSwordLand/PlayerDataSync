package net.brian.playerdatasync.data.databases;

import net.brian.playerdatasync.PlayerDataSync;
import net.brian.playerdatasync.data.PlayerData;
import net.brian.playerdatasync.data.CachedTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
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
        long timeoutMillis = 600_000; // 10 minutes
        long startTime = System.currentTimeMillis();

        try (Connection connection = getConnection()) {
            // Attempt to update and fetch data within the timeout period
            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                if (attemptAcquireLock(connection, id, uuid)) {
                    Optional<String> dataJsonOpt = fetchDataJson(connection, id, uuid);
                    if (dataJsonOpt.isPresent()) {
                        String datajson = dataJsonOpt.get();
                        if (!datajson.isEmpty()) {
                            return gson.fromJson(datajson, dataClass);
                        }
                    }
                    break; // Exit if datajson is null or empty
                }
            }

            // If data was not fetched, create a new instance and set data
            T data = instantiateDataClass(uuid, dataClass);
            setData(id, uuid, data, false);
            return data;
        } catch (SQLException e) {
            throw new Exception("Database operation failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Thread was interrupted", e);
        }
    }

    private boolean attemptAcquireLock(Connection connection, String id, UUID uuid) throws SQLException {
        String updateSQL = "UPDATE " + id + " SET complete=0 WHERE uuid=? AND complete=1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setString(1, uuid.toString());
            int updated = preparedStatement.executeUpdate();
            return updated == 1;
        }
    }

    private Optional<String> fetchDataJson(Connection connection, String id, UUID uuid) throws SQLException {
        String selectSQL = "SELECT datajson FROM " + id + " WHERE uuid=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(resultSet.getString("datajson"));
                }
                return Optional.empty();
            }
        }
    }

    private <T> T instantiateDataClass(UUID uuid, Class<T> dataClass) throws Exception {
        if (PlayerData.class.isAssignableFrom(dataClass)) {
            return dataClass.getConstructor(UUID.class).newInstance(uuid);
        } else {
            return dataClass.getDeclaredConstructor().newInstance();
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
