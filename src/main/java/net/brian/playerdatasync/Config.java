package net.brian.playerdatasync;

import org.bukkit.configuration.Configuration;

public class Config {

    private static boolean mysql = false;
    private static String host = "localhost";
    private static String port = "";
    private static String database = "";
    private static String username = "root";
    private static String password = "password";

    public static boolean useMysql() {
        return mysql;
    }

    public static String getPassword() {
        return password;
    }

    public static String getUsername() {
        return username;
    }


    public static String getJdbURL(){
        return "jdbc:mysql://"+host+":"+port+"/"+database;
    }

    public static void input(Configuration configuration){
        mysql = configuration.getBoolean("mysql",false);
        database = configuration.getString("database","");
        username = configuration.getString("username","root");
        password = configuration.getString("password","");
        host = configuration.getString("host","");
        port = configuration.getString("port");
    }
}
