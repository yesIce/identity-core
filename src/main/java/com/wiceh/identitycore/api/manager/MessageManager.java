package com.wiceh.identitycore.api.manager;

import com.wiceh.identitycore.api.constants.MessageKey;
import org.bukkit.command.CommandSender;

public interface MessageManager {

    void reload();

    void send(CommandSender sender, MessageKey key);

    void send(CommandSender sender, MessageKey key, String... placeholders);

    String format(MessageKey key, String... placeholders);

    String getMessage(MessageKey key);
}
