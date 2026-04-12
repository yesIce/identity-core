package com.wiceh.identitycore.impl;

import com.wiceh.identitycore.IdentityCorePlugin;
import com.wiceh.identitycore.api.event.PlayerIdentityLoadEvent;
import it.ytnoos.loadit.api.LoaditLoadListener;

public class IdentityLoaditListener implements LoaditLoadListener<PlayerIdentityData> {

    private final IdentityCorePlugin plugin;

    public IdentityLoaditListener(IdentityCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPostLoad(PlayerIdentityData data) {
        plugin.getServer().getScheduler().runTask(plugin, () ->
                plugin.getServer().getPluginManager().callEvent(
                        new PlayerIdentityLoadEvent(data.getUUID(), data)
                )
        );
    }
}