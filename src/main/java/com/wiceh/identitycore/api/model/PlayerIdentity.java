package com.wiceh.identitycore.api.model;

import java.util.List;

public interface PlayerIdentity {

    int getId();

    String getLastName();

    String getRegisteredName();

    String getLastIp();

    long getRegisteredAt();

    long getLastSeenAt();

    List<String> getNameHistory();
}
