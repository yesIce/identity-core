package com.wiceh.identitycore.storage;

import com.wiceh.identitycore.impl.PlayerIdentityData;
import org.intellij.lang.annotations.Language;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IdentityRepository {

    private final DatabaseManager databaseManager;

    @Language("SQL")
    private static final String SELECT_FROM_BUKKIT_UUID =
            "SELECT * FROM identity_players " +
            "WHERE bukkit_uuid = ?";

    @Language("SQL")
    private static final String SELECT_FROM_ID =
            "SELECT * FROM identity_players " +
            "WHERE id = ?";

    @Language("SQL")
    private static final String SELECT_BY_NAME =
            "SELECT p.* FROM identity_players p " +
            "WHERE p.last_name = ? " +
            "UNION " +
            "SELECT p.* FROM identity_players p " +
            "JOIN identity_name_history h ON h.player_id = p.id " +
            "WHERE h.new_name = ? OR h.old_name = ? LIMIT 1";

    @Language("SQL")
    private static final String INSERT_IDENTITY =
            "INSERT INTO identity_players " +
            "(bukkit_uuid, registered_name, last_name, last_ip, registered_at, last_seen_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    @Language("SQL")
    private static final String INSERT_NAME_HISTORY =
            "INSERT INTO identity_name_history (player_id, old_name, new_name, changed_at) " +
            "VALUES (?, ?, ?, ?)";

    @Language("SQL")
    private static final String UPDATE_ON_JOIN =
            "UPDATE identity_players SET last_name = ?, last_ip = ?, last_seen_at = ? " +
            "WHERE id = ?";

    @Language("SQL")
    private static final String SELECT_NAME_HISTORY =
            "SELECT new_name FROM identity_name_history " +
            "WHERE player_id = ? ORDER BY changed_at ASC";

    public IdentityRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<PlayerIdentityData> findByBukkitUuid(UUID bukkitUuid) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FROM_BUKKIT_UUID)) {

            stmt.setString(1, bukkitUuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(conn, rs));
            }
        }
    }

    public Optional<PlayerIdentityData> findById(int id) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FROM_ID)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(conn, rs));
            }
        }
    }

    public Optional<PlayerIdentityData> findByName(String name) throws SQLException {
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NAME)) {

            stmt.setString(1, name);
            stmt.setString(2, name);
            stmt.setString(3, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(conn, rs));
            }
        }
    }

    public PlayerIdentityData createNew(UUID bukkitUuid, String name, String ip) throws SQLException {
        long now = System.currentTimeMillis();

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT_IDENTITY, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, bukkitUuid.toString());
            stmt.setString(2, name);
            stmt.setString(3, name);
            stmt.setString(4, ip);
            stmt.setLong(5, now);
            stmt.setLong(6, now);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("No generated key returned for new identity");
                int generatedId = keys.getInt(1);

                return new PlayerIdentityData(
                        bukkitUuid, name, generatedId,
                        name, ip, now, now, new ArrayList<>()
                );
            }
        }
    }

    public void updateOnJoin(PlayerIdentityData data, String currentName, String ip) throws SQLException {
        boolean nameChanged = !data.getLastName().equals(currentName);
        long now = System.currentTimeMillis();

        try (Connection conn = databaseManager.getConnection()) {
            if (nameChanged) {
                try (PreparedStatement stmt = conn.prepareStatement(INSERT_NAME_HISTORY)) {
                    stmt.setInt(1, data.getId());
                    stmt.setString(2, data.getLastName());
                    stmt.setString(3, currentName);
                    stmt.setLong(4, now);
                    stmt.executeUpdate();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_ON_JOIN)) {
                stmt.setString(1, currentName);
                stmt.setString(2, ip);
                stmt.setLong(3, now);
                stmt.setInt(4, data.getId());
                stmt.executeUpdate();
            }
        }
    }

    private PlayerIdentityData mapRow(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        List<String> nameHistory = getNameHistory(conn, id);

        return new PlayerIdentityData(
                UUID.fromString(rs.getString("bukkit_uuid")),
                rs.getString("last_name"),
                id,
                rs.getString("registered_name"),
                rs.getString("last_ip"),
                rs.getLong("registered_at"),
                rs.getLong("last_seen_at"),
                nameHistory
        );
    }

    private List<String> getNameHistory(Connection conn, int playerId) throws SQLException {
        List<String> history = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_NAME_HISTORY)) {
            stmt.setInt(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) history.add(rs.getString("new_name"));
            }
        }
        return history;
    }
}