package com.wiceh.identitycore.api.event;

import com.wiceh.identitycore.api.model.PlayerIdentity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerIdentityLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID bukkitUuid;
    private final PlayerIdentity identity;

    public PlayerIdentityLoadEvent(UUID bukkitUuid, PlayerIdentity identity) {
        this.bukkitUuid = bukkitUuid;
        this.identity = identity;
    }

    public UUID getBukkitUuid() {
        return bukkitUuid;
    }

    public PlayerIdentity getIdentity() {
        return identity;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}