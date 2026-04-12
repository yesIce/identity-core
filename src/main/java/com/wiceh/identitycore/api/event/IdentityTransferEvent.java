package com.wiceh.identitycore.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class IdentityTransferEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int fromId;
    private final int toId;
    private final String fromName;
    private final String toName;

    public IdentityTransferEvent(int fromId, int toId, String fromName, String toName) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromName = fromName;
        this.toName = toName;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    public String getFromName() {
        return fromName;
    }

    public String getToName() {
        return toName;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}