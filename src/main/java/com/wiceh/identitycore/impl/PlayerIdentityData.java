package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.api.model.PlayerIdentity;
import it.ytnoos.loadit.api.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PlayerIdentityData extends UserData implements PlayerIdentity {

    private final int id;
    private final String registeredName;
    private final long registeredAt;

    private String lastIp;
    private long lastSeenAt;
    private List<String> nameHistory;

    public PlayerIdentityData(@NotNull UUID bukkitUuid, @NotNull String name, @NotNull int id,
                              @NotNull String registeredName, @NotNull String lastIp, long registeredAt,
                              long lastSeenAt, @NotNull List<String> nameHistory) {
        super(bukkitUuid, name);

        this.id = id;
        this.registeredName = registeredName;
        this.registeredAt = registeredAt;

        this.lastIp = lastIp;
        this.lastSeenAt = lastSeenAt;
        this.nameHistory = nameHistory;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getRegisteredName() {
        return registeredName;
    }

    @Override
    public long getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public String getLastIp() {
        return lastIp;
    }

    @Override
    public long getLastSeenAt() {
        return lastSeenAt;
    }

    @Override
    public List<String> getNameHistory() {
        return nameHistory;
    }

    @Override
    public String getLastName() {
        return name;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public void setLastSeenAt(long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void setNameHistory(List<String> nameHistory) {
        this.nameHistory = nameHistory;
    }
}