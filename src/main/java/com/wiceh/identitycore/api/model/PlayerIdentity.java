package com.wiceh.identitycore.api.model;

import java.util.List;
import java.util.UUID;

public interface PlayerIdentity {

    UUID getUuid();

    int getId();

    String getLastName();

    String getRegisteredName();

    String getLastIp();

    long getRegisteredAt();

    long getLastSeenAt();

    List<String> getNameHistory();
}
