package com.wiceh.identitycore.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class IdentityTransferEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int fromId;
    private final int toId;

    private final String fromName;
    private final String toName;

    private final UUID fromUuid;
    private final UUID toUuid;

    public IdentityTransferEvent(int fromId, int toId, String fromName, String toName, UUID fromUuid, UUID toUuid) {
        this.fromId = fromId;
        this.toId = toId;
        this.fromName = fromName;
        this.toName = toName;
        this.fromUuid = fromUuid;
        this.toUuid = toUuid;
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

    public UUID getFromUuid() {
        return fromUuid;
    }

    public UUID getToUuid() {
        return toUuid;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}