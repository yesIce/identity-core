package com.wiceh.identitycore.storage.impl;

import com.wiceh.identitycore.storage.AbstractDatabaseManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseManager extends AbstractDatabaseManager {

    @Override
    public void init(FileConfiguration config) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found", e);
        }

        String path = config.getString("database.file", "plugins/IdentityCore/identitycore.db");
        String url = "jdbc:sqlite:" + path;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setPoolName("IdentityCore-Pool");
        dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    @Override
    protected void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_players (" +
                    "    id              INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "    bukkit_uuid     TEXT    NOT NULL UNIQUE," +
                    "    registered_name TEXT    NOT NULL," +
                    "    last_name       TEXT    NOT NULL UNIQUE," +
                    "    last_ip         TEXT    NOT NULL," +
                    "    registered_at   INTEGER NOT NULL," +
                    "    last_seen_at    INTEGER NOT NULL" +
                    ")"
            );
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_name_history (" +
                    "    id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "    player_id   INTEGER NOT NULL," +
                    "    old_name    TEXT    NOT NULL," +
                    "    new_name    TEXT    NOT NULL," +
                    "    changed_at  INTEGER NOT NULL" +
                    ")"
            );
            stmt.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_player_id ON identity_name_history (player_id)"
            );
            stmt.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_new_name ON identity_name_history (new_name)"
            );
        }
    }
}