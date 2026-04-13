package com.wiceh.identitycore.api.constants;

import org.bukkit.command.CommandSender;

public enum Permission {

    COMMAND("command.identity"),
    TRANSFER("command.identity.transfer"),
    RELOAD("command.identity.reload"),

    ;

    private final String permission;

    Permission(String permission) {
        this.permission = "identitycore." + permission;
    }

    public boolean has(CommandSender sender) {
        return sender.hasPermission(permission);
    }

    public String getPermission() {
        return permission;
    }
}
