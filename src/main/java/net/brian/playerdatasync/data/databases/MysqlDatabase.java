package net.brian.playerdatasync.data.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.brian.playerdatasync.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDatabase extends Database {

    HikariDataSource ds;

    @Override
    public Connection getConnection() {
        try {
            if(connection.isClosed()){
                connection = ds.getConnection();
                return connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    @Override
    void setUp() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword(Config.getPassword());
        hikariConfig.setUsername(Config.getUsername());
        hikariConfig.setJdbcUrl(Config.getJdbURL());

        ds = new HikariDataSource(hikariConfig);

        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
