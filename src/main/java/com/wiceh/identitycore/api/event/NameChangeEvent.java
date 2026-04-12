package com.wiceh.identitycore.api.event;

import com.wiceh.identitycore.api.model.PlayerIdentity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NameChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final PlayerIdentity identity;
    private final String oldName;
    private final String newName;

    public NameChangeEvent(Player player, PlayerIdentity identity,
                           String oldName, String newName) {
        this.player = player;
        this.identity = identity;
        this.oldName = oldName;
        this.newName = newName;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerIdentity getIdentity() {
        return identity;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}