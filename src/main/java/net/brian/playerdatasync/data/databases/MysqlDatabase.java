package net.brian.playerdatasync.data.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.brian.playerdatasync.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDatabase extends SqlDatabase {

    final HikariDataSource ds;

    public MysqlDatabase() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPassword(Config.getPassword());
        hikariConfig.setUsername(Config.getUsername());
        hikariConfig.setJdbcUrl(Config.getJdbURL());
        ds = new HikariDataSource(hikariConfig);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
