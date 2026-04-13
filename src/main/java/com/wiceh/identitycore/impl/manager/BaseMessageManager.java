package com.wiceh.identitycore.impl.manager;

import com.wiceh.identitycore.api.constants.MessageKey;
import com.wiceh.identitycore.api.manager.MessageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class BaseMessageManager implements MessageManager {

    private final Plugin plugin;
    private final Map<MessageKey, String> messages;

    public BaseMessageManager(Plugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }

    @Override
    public void reload() {
        messages.clear();
        plugin.reloadConfig();
        loadMessages();
    }

    private void loadMessages() {
        String path = "messages";
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null)
            section = config.createSection(path);

        boolean save = false;
        for (MessageKey key : MessageKey.values()) {
            String formattedKey = key.getFormattedKey();
            String message = section.getString(formattedKey);
            if (message == null) {
                message = key.getDefaultMessage();
                section.set(formattedKey, key.getDefaultMessage());
                save = true;
            }

            messages.put(key, message);
        }

        if (save) plugin.saveConfig();
    }

    @Override
    public void send(CommandSender sender, MessageKey key) {
        sender.sendMessage(getMessage(key));
    }

    @Override
    public void send(CommandSender sender, MessageKey key, String... placeholders) {
        sender.sendMessage(format(key, placeholders));
    }

    @Override
    public String format(MessageKey key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return message;
    }

    @Override
    public String getMessage(MessageKey key) {
        return messages.get(key);
    }
}
