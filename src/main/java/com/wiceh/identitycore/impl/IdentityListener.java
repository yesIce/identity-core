package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.api.event.IdentityTransferEvent;
import com.wiceh.identitycore.api.event.NameChangeEvent;
import com.wiceh.identitycore.storage.IdentityRepository;
import com.wiceh.loadex.api.DataCache;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdentityListener implements Listener {

    private final DataCache<PlayerIdentityData> cache;
    private final IdentityRepository repository;
    private final Logger logger;
    private final LuckPerms luckPerms;

    public IdentityListener(DataCache<PlayerIdentityData> cache,
                            IdentityRepository repository,
                            Logger logger, LuckPerms luckPerms) {
        this.cache = cache;
        this.repository = repository;
        this.logger = logger;
        this.luckPerms = luckPerms;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String currentName = player.getName();

        InetSocketAddress address = player.getAddress();
        String currentIp = address == null
                ? "unknown"
                : address.getAddress().getHostAddress();

        cache.ifCached(player, data -> {
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
                        player, data, oldName, currentName
                );
                player.getServer().getPluginManager().callEvent(nameChangeEvent);

                logger.info("Name change detected: " + oldName + " -> " + currentName +
                            " (stable: " + data.getId() + ")");
            } else {
                data.setLastIp(currentIp);
                data.setLastSeenAt(System.currentTimeMillis());
            }
        });
    }

    @EventHandler
    public void onIdentityTransfer(IdentityTransferEvent event) {
        if (luckPerms == null) return;

        UUID fromUUID = event.getFromUuid();
        UUID toUUID = event.getToUuid();

        String fromName = event.getFromName();
        String toName = event.getToName();

        logger.info("[IdentityTransfer] Start: " + fromName + " -> " + toName);

        luckPerms.getUserManager().loadUser(fromUUID).thenCompose(fromUser ->
                luckPerms.getUserManager().loadUser(toUUID).thenApply(toUser -> {
                    logger.info("[IdentityTransfer] Copying nodes...");

                    for (Node node : fromUser.getNodes()) {
                        if (toUser.getNodes().contains(node)) continue;
                        toUser.data().add(node);
                    }

                    logger.info("[IdentityTransfer] Nodes copied successfully");
                    return new User[]{fromUser, toUser};
                })
        ).thenCompose(users -> {
            User fromUser = users[0];
            User toUser = users[1];

            logger.info("[IdentityTransfer] Saving target user: " + toName);

            return luckPerms.getUserManager().saveUser(toUser)
                    .thenRun(() -> {
                        logger.info("[IdentityTransfer] Deleting old user: " + fromName);
                        luckPerms.getUserManager().deletePlayerData(fromUser.getUniqueId());
                    });
        }).exceptionally(ex -> {
            logger.severe("[IdentityTransfer] ERROR: " + ex.getMessage());
            return null;
        });
    }
}