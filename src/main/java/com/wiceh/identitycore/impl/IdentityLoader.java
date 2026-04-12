package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.storage.IdentityRepository;
import it.ytnoos.loadit.api.DataLoader;
import it.ytnoos.loadit.api.LoadResult;

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
    public Optional<PlayerIdentityData> getOrCreate(UUID bukkitUuid, String name) {
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
    public Optional<PlayerIdentityData> load(UUID bukkitUuid) {
        try {
            return repository.findByBukkitUuid(bukkitUuid);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load identity for " + bukkitUuid, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PlayerIdentityData> load(String name) {
        try {
            return repository.findByName(name);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load identity for name " + name, e);
            return Optional.empty();
        }
    }

    @Override
    public String getErrorMessage(LoadResult result, UUID uuid, String name) {
        return "§cErrore durante il caricamento del tuo profilo.\n§7(" + result.name() + ")";
    }
}