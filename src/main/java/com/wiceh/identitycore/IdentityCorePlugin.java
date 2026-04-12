package com.wiceh.identitycore;

import com.wiceh.identitycore.api.IdentityAPI;
import com.wiceh.identitycore.impl.*;
import com.wiceh.identitycore.impl.command.IdentityCommand;
import com.wiceh.identitycore.storage.DatabaseManager;
import com.wiceh.identitycore.storage.DatabaseManagerFactory;
import com.wiceh.identitycore.storage.DriverLoader;
import com.wiceh.identitycore.storage.IdentityRepository;
import it.ytnoos.loadit.Loadit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class IdentityCorePlugin extends JavaPlugin {

    private static IdentityCorePlugin instance;

    private DatabaseManager databaseManager;
    private Loadit<PlayerIdentityData> loadit;
    private IdentityAPI identityAPI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        String dbType = getConfig().getString("database.type", "mysql");
        try {
            DriverLoader.load(dbType, this);
        } catch (Exception e) {
            getLogger().severe("Failed to load database driver: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = DatabaseManagerFactory.create(getConfig());
        try {
            databaseManager.init(getConfig());
        } catch (Exception e) {
            getLogger().severe("Failed to connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        IdentityRepository identityRepository = new IdentityRepository(databaseManager);
        IdentityLoader loader = new IdentityLoader(identityRepository, getLogger());

        loadit = Loadit.createInstance(this, loader);
        loadit.addListener(new IdentityLoaditListener(this));
        loadit.init();

        getServer().getPluginManager().registerEvents(
                new IdentityListener(loadit.getContainer(), identityRepository, getLogger()),
                this
        );

        identityAPI = new IdentityAPIImpl(this, loadit.getContainer(), identityRepository, getLogger());

        PluginCommand cmd = getCommand("identity");
        if (cmd != null) {
            IdentityCommand command = new IdentityCommand(identityAPI, this);
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        getLogger().info("IdentityCore enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (loadit != null) loadit.stop();
        if (databaseManager != null) databaseManager.close();
        getLogger().info("IdentityCore disabled.");
    }

    public static IdentityAPI getAPI() {
        return instance.identityAPI;
    }

    public static IdentityCorePlugin getInstance() {
        return instance;
    }
}