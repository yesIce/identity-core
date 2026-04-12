package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.api.IdentityAPI;
import com.wiceh.identitycore.api.model.PlayerIdentity;
import com.wiceh.identitycore.storage.IdentityRepository;
import it.ytnoos.loadit.api.DataContainer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdentityAPIImpl implements IdentityAPI {

    private final DataContainer<PlayerIdentityData> container;
    private final IdentityRepository repository;
    private final Logger logger;

    public IdentityAPIImpl(DataContainer<PlayerIdentityData> container,
                           IdentityRepository repository,
                           Logger logger) {
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
}