package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.api.event.NameChangeEvent;
import com.wiceh.identitycore.storage.IdentityRepository;
import it.ytnoos.loadit.api.DataContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IdentityListener implements Listener {

    private final DataContainer<PlayerIdentityData> container;
    private final IdentityRepository repository;
    private final Logger logger;

    public IdentityListener(DataContainer<PlayerIdentityData> container,
                            IdentityRepository repository,
                            Logger logger) {
        this.container = container;
        this.repository = repository;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        String currentName = event.getPlayer().getName();
        String currentIp = event.getPlayer().getAddress().getAddress().getHostAddress();

        container.acceptIfCached(event.getPlayer(), data -> {
            boolean nameChanged = !data.getLastName().equals(currentName);

            try {
                repository.updateOnJoin(data, currentName, currentIp);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to updateOnJoin for " + data.getId(), e);
                return;
            }

            if (nameChanged) {
                String oldName = data.getLastName();
                data.setLastIp(currentIp);
                data.setLastSeenAt(System.currentTimeMillis());

                NameChangeEvent nameChangeEvent = new NameChangeEvent(
                        event.getPlayer(), data, oldName, currentName
                );
                event.getPlayer().getServer().getPluginManager().callEvent(nameChangeEvent);

                logger.info("Name change detected: " + oldName + " -> " + currentName +
                            " (stable: " + data.getId() + ")");
            } else {
                data.setLastIp(currentIp);
                data.setLastSeenAt(System.currentTimeMillis());
            }
        });
    }
}