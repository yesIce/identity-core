package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.storage.IdentityRepository;
import com.wiceh.loadex.api.DataLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdentityLoader implements DataLoader<PlayerIdentityData> {

    private final IdentityRepository repository;
    private final Logger logger;

    public IdentityLoader(IdentityRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    @Override
    public @NotNull Optional<PlayerIdentityData> loadOrCreate(@NotNull UUID bukkitUuid, @NotNull String name) {
        try {
            Optional<PlayerIdentityData> existing = repository.findByBukkitUuid(bukkitUuid);
            if (existing.isPresent()) return existing;

            return Optional.of(repository.createNew(bukkitUuid, name, "unknown"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to getOrCreate identity for " + bukkitUuid + " (" + name + ")", e);
            return Optional.empty();
        }
    }

    @Override
    public @NotNull Optional<PlayerIdentityData> loadByUuid(@NotNull UUID bukkitUuid) {
        try {
            return repository.findByBukkitUuid(bukkitUuid);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load identity for " + bukkitUuid, e);
            return Optional.empty();
        }
    }

    @Override
    public @NotNull Optional<PlayerIdentityData> loadByName(@NotNull String name) {
        try {
            return repository.findByName(name);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load identity for name " + name, e);
            return Optional.empty();
        }
    }
}