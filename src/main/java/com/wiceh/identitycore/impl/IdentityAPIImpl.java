package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.api.IdentityAPI;
import com.wiceh.identitycore.api.constants.TransferResult;
import com.wiceh.identitycore.api.event.IdentityTransferEvent;
import com.wiceh.identitycore.api.model.PlayerIdentity;
import com.wiceh.identitycore.storage.IdentityRepository;
import it.ytnoos.loadit.api.DataContainer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdentityAPIImpl implements IdentityAPI {

    private final Plugin plugin;
    private final DataContainer<PlayerIdentityData> container;
    private final IdentityRepository repository;
    private final Logger logger;

    public IdentityAPIImpl(Plugin plugin,
                           DataContainer<PlayerIdentityData> container,
                           IdentityRepository repository,
                           Logger logger) {
        this.plugin = plugin;
        this.container = container;
        this.repository = repository;
        this.logger = logger;
    }

    @Override
    public Optional<PlayerIdentity> getIdentity(Player player) {
        return container.getCached(player.getUniqueId())
                .map(data -> data);
    }

    @Override
    public Optional<PlayerIdentity> getIdentity(UUID bukkitUUID) {
        return container.getCached(bukkitUUID)
                .map(data -> data);
    }

    @Override
    public CompletableFuture<Optional<PlayerIdentity>> findByBukkitUuid(UUID bukkitUuid) {
        Optional<PlayerIdentity> cached = getIdentity(bukkitUuid);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return repository.findByBukkitUuid(bukkitUuid)
                        .map(data -> data);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to findByBukkitUuid " + bukkitUuid, e);
                return Optional.empty();
            }
        }, container.getExecutor());
    }

    @Override
    public CompletableFuture<Optional<PlayerIdentity>> findByName(String name) {
        Optional<PlayerIdentity> cached = findInCacheByName(name);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return repository.findByName(name)
                        .map(data -> data);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to findByName " + name, e);
                return Optional.empty();
            }
        }, container.getExecutor());
    }

    @Override
    public CompletableFuture<Optional<PlayerIdentity>> findById(int id) {
        Optional<PlayerIdentity> cached = findInCacheById(id);
        if (cached.isPresent()) return CompletableFuture.completedFuture(cached);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return repository.findById(id).map(data -> (PlayerIdentity) data);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to findById " + id, e);
                return Optional.empty();
            }
        }, container.getExecutor());
    }

    @Override
    public boolean isOnline(int id) {
        return findInCacheById(id).isPresent();
    }

    @Override
    public void forEach(Consumer<PlayerIdentity> consumer) {
        container.forEach(consumer::accept);
    }

    private Optional<PlayerIdentity> findInCacheByName(String name) {
        final Optional<PlayerIdentity>[] result = new Optional[]{Optional.empty()};
        container.forEach(data -> {
            if (data.getLastName().equalsIgnoreCase(name))
                result[0] = Optional.of(data);
        });
        return result[0];
    }

    private Optional<PlayerIdentity> findInCacheById(int id) {
        final Optional<PlayerIdentity>[] result = new Optional[]{Optional.empty()};
        container.forEach(data -> {
            if (data.getId() == id) result[0] = Optional.of(data);
        });
        return result[0];
    }

    @Override
    public CompletableFuture<TransferResult> transfer(String fromName, String toName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<PlayerIdentityData> fromOpt = repository.findByName(fromName);
                if (!fromOpt.isPresent()) return TransferResult.FROM_NOT_FOUND;

                Optional<PlayerIdentityData> toOpt = repository.findByName(toName);
                if (!toOpt.isPresent()) return TransferResult.TO_NOT_FOUND;

                PlayerIdentityData from = fromOpt.get();
                PlayerIdentityData to = toOpt.get();

                if (from.getId() == to.getId()) return TransferResult.SAME_IDENTITY;
                if (isOnline(from.getId())) return TransferResult.FROM_IS_ONLINE;

                plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getServer().getPluginManager().callEvent(
                                new IdentityTransferEvent(from.getId(), to.getId(), fromName, toName, from.getUUID(), to.getUUID())
                        )
                );

                repository.transfer(from.getId(), to.getId());
                return TransferResult.SUCCESS;

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to transfer " + fromName + " -> " + toName, e);
                return TransferResult.ERROR;
            }
        }, container.getExecutor());
    }
}