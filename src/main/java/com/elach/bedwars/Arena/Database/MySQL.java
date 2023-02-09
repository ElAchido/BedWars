package com.elach.bedwars.Arena.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL {

    private Connection connection;

    public MySQL(FileConfiguration config) {
        try {
            connection = getDataSource(
                    config.getString("mysql.host"),
                    config.getString("mysql.port"),
                    config.getString("mysql.database"),
                    config.getString("mysql.user"),
                    config.getString("mysql.password"),
                    config.getString("mysql.extra-url")).getConnection();
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // CONNECTION METHODS

    private HikariDataSource getDataSource(
            String host,
            String port,
            String database,
            String username,
            String password,
            String extraUrl)
    {
        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + extraUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        return new HikariDataSource(hikari);
    }

    private void createTables() throws SQLException {
        PreparedStatement tables = connection.prepareStatement("CREATE TABLE IF NOT EXISTS Players (UUID VARCHAR(36) UNIQUE, Points FLOAT);");
        tables.executeUpdate();
    }

    // INFORMATION METHODS

    public BedPlayer getBedPlayer(UUID playerUUID) {
        try {
            PreparedStatement information = connection.prepareStatement("SELECT UUID, Points FROM Players WHERE UUID = ?;");
            information.setString(1, playerUUID.toString());
            ResultSet info = information.executeQuery();
            info.next();
            return new BedPlayer(playerUUID, info.getFloat("Points"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setBedPlayerPoints(UUID playerUUID, int points) {
        try {
            PreparedStatement modification = connection.prepareStatement("UPDATE Players SET Points = ? WHERE UUID = ?;");
            modification.setInt(1, points);
            modification.setString(2, playerUUID.toString());
            modification.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}