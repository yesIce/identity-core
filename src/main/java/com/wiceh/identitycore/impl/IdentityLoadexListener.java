package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.IdentityCorePlugin;
import com.wiceh.identitycore.api.event.PlayerIdentityLoadEvent;
import com.wiceh.loadex.api.LoadListener;
import org.jetbrains.annotations.NotNull;

public class IdentityLoadexListener implements LoadListener<PlayerIdentityData> {

    private final IdentityCorePlugin plugin;

    public IdentityLoadexListener(IdentityCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPostLoad(@NotNull PlayerIdentityData data) {
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(
                        new PlayerIdentityLoadEvent(data.getUuid(), data)
                )
        );
    }
}