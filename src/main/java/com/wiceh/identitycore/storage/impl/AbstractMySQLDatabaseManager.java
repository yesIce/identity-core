package com.wiceh.identitycore.storage.impl;

import com.wiceh.identitycore.storage.AbstractDatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractMySQLDatabaseManager extends AbstractDatabaseManager {

    @Override
    protected void createTables() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_players (" +
                    "    id              INT         NOT NULL AUTO_INCREMENT," +
                    "    bukkit_uuid     CHAR(36)    NOT NULL," +
                    "    registered_name VARCHAR(16) NOT NULL," +
                    "    last_name       VARCHAR(16) NOT NULL," +
                    "    last_ip         VARCHAR(45) NOT NULL," +
                    "    registered_at   BIGINT      NOT NULL," +
                    "    last_seen_at    BIGINT      NOT NULL," +
                    "    PRIMARY KEY (id)," +
                    "    UNIQUE INDEX idx_bukkit_uuid (bukkit_uuid)," +
                    "    UNIQUE INDEX idx_last_name   (last_name)" +
                    ")"
            );
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS identity_name_history (" +
                    "    id          INT         NOT NULL AUTO_INCREMENT," +
                    "    player_id   INT         NOT NULL," +
                    "    old_name    VARCHAR(16) NOT NULL," +
                    "    new_name    VARCHAR(16) NOT NULL," +
                    "    changed_at  BIGINT      NOT NULL," +
                    "    PRIMARY KEY (id)," +
                    "    INDEX idx_player_id (player_id)," +
                    "    INDEX idx_new_name  (new_name)" +
                    ")"
            );
        }
    }
}