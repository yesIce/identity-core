package com.wiceh.identitycore.storage.impl;

import com.wiceh.identitycore.storage.AbstractDatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQLDatabaseManager extends AbstractDatabaseManager {

    @Override
    public void init(FileConfiguration config) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver not found", e);
        }

        String url = "jdbc:postgresql://" +
                     config.getString("database.host") + ":" +
                     config.getInt("database.port") + "/" +
                     config.getString("database.name");

        initPool(url, config.getString("database.user"), config.getString("database.password"));
        createTables();
    }

    @Override
    protected void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_players (" +
                    "    id              SERIAL      PRIMARY KEY," +
                    "    bukkit_uuid     CHAR(36)    NOT NULL," +
                    "    registered_name VARCHAR(16) NOT NULL," +
                    "    last_name       VARCHAR(16) NOT NULL," +
                    "    last_ip         VARCHAR(45) NOT NULL," +
                    "    registered_at   BIGINT      NOT NULL," +
                    "    last_seen_at    BIGINT      NOT NULL," +
                    "    UNIQUE (bukkit_uuid)," +
                    "    UNIQUE (last_name)" +
                    ")"
            );
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_name_history (" +
                    "    id          SERIAL  PRIMARY KEY," +
                    "    player_id   INT     NOT NULL," +
                    "    old_name    VARCHAR(16) NOT NULL," +
                    "    new_name    VARCHAR(16) NOT NULL," +
                    "    changed_at  BIGINT      NOT NULL" +
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