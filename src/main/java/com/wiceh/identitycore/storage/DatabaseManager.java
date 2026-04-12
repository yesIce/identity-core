package com.wiceh.identitycore.storage;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {

    void init(FileConfiguration config) throws SQLException;

    Connection getConnection() throws SQLException;

    void close();

}