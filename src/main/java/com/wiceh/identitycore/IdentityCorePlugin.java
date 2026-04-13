package com.wiceh.identitycore;

import com.wiceh.identitycore.api.IdentityAPI;
import com.wiceh.identitycore.api.manager.MessageManager;
import com.wiceh.identitycore.impl.*;
import com.wiceh.identitycore.impl.command.IdentityCommand;
import com.wiceh.identitycore.impl.IdentityListener;
import com.wiceh.identitycore.impl.manager.BaseMessageManager;
import com.wiceh.identitycore.storage.DatabaseManager;
import com.wiceh.identitycore.storage.DatabaseManagerFactory;
import com.wiceh.identitycore.storage.DriverLoader;
import com.wiceh.identitycore.storage.IdentityRepository;
import com.wiceh.loadex.api.Loadex;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentityCorePlugin extends JavaPlugin {

    private static IdentityCorePlugin instance;
    private LuckPerms luckPerms;

    private DatabaseManager databaseManager;
    private IdentityRepository identityRepository;
    private Loadex<PlayerIdentityData> loadex;

    private ExecutorService apiExecutor;
    private IdentityAPI identityAPI;

    private MessageManager messageManager;


    @Override
    public void onEnable() {
        instance = this;

        Plugin luckPermsPlugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin != null && luckPermsPlugin.isEnabled()) {
            luckPerms = LuckPermsProvider.get();
            getLogger().info("Loaded LuckPerms API!");
        }

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

        identityRepository = new IdentityRepository(databaseManager);

        IdentityLoader loader = new IdentityLoader(identityRepository, getLogger());
        loadex = Loadex.<PlayerIdentityData>builder(this, loader)
                .addListener(new IdentityLoadexListener(this))
                .debug(getConfig().getBoolean("debug", false))
                .build();
        loadex.enable();

        getServer().getPluginManager().registerEvents(
                new IdentityListener(loadex.cache(), identityRepository, getLogger(), luckPerms),
                this
        );

        apiExecutor = Executors.newVirtualThreadPerTaskExecutor();
        identityAPI = new IdentityAPIImpl(this, loadex.cache(), identityRepository, getLogger(), apiExecutor);

        messageManager = new BaseMessageManager(this);

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
        if (identityAPI != null && identityRepository != null) {
            identityAPI.forEach(identity -> {
                if (identity instanceof PlayerIdentityData) {
                    PlayerIdentityData data = (PlayerIdentityData) identity;
                    data.setLastSeenAt(System.currentTimeMillis());
                    try {
                        identityRepository.updateOnJoin(data, data.getLastName(), data.getLastIp());
                    } catch (Exception e) {
                        getLogger().warning("Failed to save last_seen for " + data.getLastName());
                    }
                }
            });
        }

        if (loadex != null) loadex.disable();
        if (apiExecutor != null) apiExecutor.close();
        if (databaseManager != null) databaseManager.close();
        getLogger().info("IdentityCore disabled.");
    }

    public static IdentityCorePlugin getInstance() {
        return instance;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static IdentityAPI getAPI() {
        return instance.identityAPI;
    }

    public static MessageManager getMessageManager() {
        return instance.messageManager;
    }
}