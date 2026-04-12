package com.wiceh.identitycore.api;

import com.wiceh.identitycore.api.model.PlayerIdentity;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IdentityAPI {

    Optional<PlayerIdentity> getIdentity(Player player);

    Optional<PlayerIdentity> getIdentity(UUID bukkitUUID);

    CompletableFuture<Optional<PlayerIdentity>> findByBukkitUuid(UUID bukkitUuid);

    CompletableFuture<Optional<PlayerIdentity>> findByName(String name);

    CompletableFuture<Optional<PlayerIdentity>> findById(int id);

    boolean isOnline(int id);

    void forEach(Consumer<PlayerIdentity> consumer);
}