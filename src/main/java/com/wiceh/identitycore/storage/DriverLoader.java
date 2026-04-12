package com.wiceh.identitycore.storage;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class DriverLoader {

    private static final Map<String, String[]> DRIVERS = new HashMap<>();

    static {
        DRIVERS.put("mysql", new String[]{
                "com.mysql.cj.jdbc.Driver",
                "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
        });
        DRIVERS.put("mariadb", new String[]{
                "org.mariadb.jdbc.Driver",
                "https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.1.4/mariadb-java-client-3.1.4.jar"
        });
        DRIVERS.put("postgresql", new String[]{
                "org.postgresql.Driver",
                "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar"
        });
        DRIVERS.put("sqlite", new String[]{
                "org.sqlite.JDBC",
                "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar"
        });
    }

    public static void load(String dbType, Plugin plugin) throws Exception {
        String[] info = DRIVERS.get(dbType.toLowerCase());
        if (info == null) throw new IllegalArgumentException("Unsupported database type: " + dbType);

        String className = info[0];
        String downloadUrl = info[1];

        try {
            Class.forName(className);
            return;
        } catch (ClassNotFoundException ignored) {
        }

        File libsDir = new File(plugin.getDataFolder(), "libs");
        libsDir.mkdirs();

        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
        File driverJar = new File(libsDir, fileName);

        if (!driverJar.exists()) {
            plugin.getLogger().info("Downloading " + dbType + " driver...");
            downloadFile(downloadUrl, driverJar);
            plugin.getLogger().info("Driver downloaded successfully.");
        }

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{driverJar.toURI().toURL()},
                DriverLoader.class.getClassLoader()
        );
        Class.forName(className, true, classLoader);
        plugin.getLogger().info(dbType + " driver loaded from " + driverJar.getName());
    }

    private static void downloadFile(String url, File destination) throws Exception {
        try (java.io.InputStream in = new java.net.URL(url).openStream()) {
            java.nio.file.Files.copy(in, destination.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
