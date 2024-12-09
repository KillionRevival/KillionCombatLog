package co.killionrevival.killioncombatlog.database;

import co.killionrevival.killioncombatlog.KillionCombatLog;
import co.killionrevival.killioncombatlog.util.LogUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final KillionCombatLog plugin;
    private Connection connection;

    public DatabaseManager(KillionCombatLog plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            // Create plugin directory if it doesn't exist
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            // Connect to SQLite database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" +
                    new File(plugin.getDataFolder(), "doppeldata.db").getAbsolutePath());

            // Create tables
            try (Statement stmt = connection.createStatement()) {
                // Table for death records
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS death_records (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        killer_name VARCHAR(36),
                        death_time BIGINT,
                        location TEXT
                    )
                """);

                // Table for doppel states
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS doppel_states (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        health DOUBLE,
                        inventory TEXT,
                        armor TEXT,
                        creation_time BIGINT,
                        last_location TEXT
                    )
                """);

                LogUtil.info("Database initialized successfully");
            }
        } catch (Exception e) {
            LogUtil.error("Failed to initialize database", e);
        }
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            return items;
        }
    }

    public void saveDoppelState(UUID playerUUID, double health, ItemStack[] inventory,
                                ItemStack[] armor, String location) {
        try {
            String sql = """
                INSERT OR REPLACE INTO doppel_states 
                (player_uuid, health, inventory, armor, creation_time, last_location) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setDouble(2, health);
                pstmt.setString(3, itemStackArrayToBase64(inventory));
                pstmt.setString(4, itemStackArrayToBase64(armor));
                pstmt.setLong(5, System.currentTimeMillis());
                pstmt.setString(6, location);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LogUtil.error("Failed to save doppel state", e);
        }
    }

    public DoppelState loadDoppelState(UUID playerUUID) {
        try {
            String sql = "SELECT * FROM doppel_states WHERE player_uuid = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new DoppelState(
                                playerUUID,
                                rs.getDouble("health"),
                                itemStackArrayFromBase64(rs.getString("inventory")),
                                itemStackArrayFromBase64(rs.getString("armor")),
                                rs.getLong("creation_time"),
                                rs.getString("last_location")
                        );
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("Failed to load doppel state", e);
        }
        return null;
    }

    public void saveDeathRecord(UUID playerUUID, String killerName, String location) {
        try {
            String sql = """
                INSERT OR REPLACE INTO death_records 
                (player_uuid, killer_name, death_time, location) 
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.setString(2, killerName);
                pstmt.setLong(3, System.currentTimeMillis());
                pstmt.setString(4, location);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LogUtil.error("Failed to save death record", e);
        }
    }

    public DeathRecord loadDeathRecord(UUID playerUUID) {
        try {
            String sql = "SELECT * FROM death_records WHERE player_uuid = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new DeathRecord(
                                playerUUID,
                                rs.getString("killer_name"),
                                rs.getLong("death_time"),
                                rs.getString("location")
                        );
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("Failed to load death record", e);
        }
        return null;
    }

    public void removeDoppelState(UUID playerUUID) {
        try {
            String sql = "DELETE FROM doppel_states WHERE player_uuid = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LogUtil.error("Failed to remove doppel state", e);
        }
    }

    public void removeDeathRecord(UUID playerUUID) {
        try {
            String sql = "DELETE FROM death_records WHERE player_uuid = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, playerUUID.toString());
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            LogUtil.error("Failed to remove death record", e);
        }
    }

    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LogUtil.error("Error closing database connection", e);
            }
        }
    }
}