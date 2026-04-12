package com.wiceh.identitycore.storage;

import com.wiceh.identitycore.storage.impl.MariaDBDatabaseManager;
import com.wiceh.identitycore.storage.impl.MySQLDatabaseManager;
import com.wiceh.identitycore.storage.impl.PostgreSQLDatabaseManager;
import com.wiceh.identitycore.storage.impl.SQLiteDatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;

public class DatabaseManagerFactory {

    public static DatabaseManager create(FileConfiguration config) {
        String type = config.getString("database.type", "mysql").toLowerCase();

        switch (type) {
            case "mysql":
                return new MySQLDatabaseManager();
            case "mariadb":
                return new MariaDBDatabaseManager();
            case "postgresql":
            case "postgres":
                return new PostgreSQLDatabaseManager();
            case "sqlite":
                return new SQLiteDatabaseManager();
            default:
                throw new IllegalArgumentException("Database type not supported: " + type);
        }
    }
}
