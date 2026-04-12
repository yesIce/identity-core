package com.wiceh.identitycore.storage.impl;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;

public class MySQLDatabaseManager extends AbstractMySQLDatabaseManager {

    @Override
    public void init(FileConfiguration config) throws SQLException {
        String url = "jdbc:mysql://" +
                     config.getString("database.host") + ":" +
                     config.getInt("database.port") + "/" +
                     config.getString("database.name") +
                     "?useSSL=false&autoReconnect=true&characterEncoding=utf8";

        initPool(url, config.getString("database.user"), config.getString("database.password"));
        createTables();
    }
}