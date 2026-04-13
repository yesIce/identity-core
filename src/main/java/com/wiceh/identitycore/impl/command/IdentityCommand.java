package com.wiceh.identitycore.impl.command;

import com.wiceh.identitycore.IdentityCorePlugin;
import com.wiceh.identitycore.api.IdentityAPI;
import com.wiceh.identitycore.api.constants.MessageKey;
import com.wiceh.identitycore.api.constants.Permission;
import com.wiceh.identitycore.api.manager.MessageManager;
import com.wiceh.identitycore.api.model.PlayerIdentity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IdentityCommand implements CommandExecutor, TabCompleter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String LINE = "§8§m" + new String(new char[30]).replace("\0", "─");

    private final IdentityAPI identityAPI;
    private final Plugin plugin;
    private final MessageManager messageManager;

    public IdentityCommand(IdentityAPI identityAPI, Plugin plugin) {
        this.identityAPI = identityAPI;
        this.plugin = plugin;
        this.messageManager = IdentityCorePlugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!Permission.COMMAND.has(sender)) {
            messageManager.send(sender, MessageKey.NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                handleInfo(sender, args);
                break;
            case "history":
                handleHistory(sender, args);
                break;
            case "transfer":
                handleTransfer(sender, args);
                break;
            case "lookup":
                handleLookup(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendUsage(sender, label);
        }

        return true;
    }

    // /identity info <name>
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                messageManager.send(sender, MessageKey.USAGE_INFO);
                return;
            }
            showIdentity(sender, identityAPI.getIdentity(((Player) sender).getUniqueId())
                    .orElse(null));
            return;
        }

        String input = args[1];
        messageManager.send(sender, MessageKey.SEARCHING);

        identityAPI.findByName(input).thenAcceptAsync(opt -> {
            if (!opt.isPresent()) {
                messageManager.send(sender, MessageKey.PLAYER_NOT_FOUND, "player", input);
                return;
            }
            showIdentity(sender, opt.get());
        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity history <name>
    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageManager.send(sender, MessageKey.USAGE_HISTORY);
            return;
        }

        String input = args[1];
        messageManager.send(sender, MessageKey.SEARCHING);

        identityAPI.findByName(input).thenAcceptAsync(opt -> {
            if (!opt.isPresent()) {
                messageManager.send(sender, MessageKey.PLAYER_NOT_FOUND, "player", input);
                return;
            }

            PlayerIdentity identity = opt.get();
            List<String> history = identity.getNameHistory();

            sender.sendMessage(LINE);
            sender.sendMessage("§eName history §7- §f" + identity.getLastName());

            if (history.isEmpty()) {
                sender.sendMessage("  §7No name change registered.");
            } else {
                sender.sendMessage("  §7Previous names §8(" + history.size() + ")§7:");
                for (int i = 0; i < history.size(); i++) {
                    sender.sendMessage("  §8" + (i + 1) + ". §f" + history.get(i));
                }
            }

            sender.sendMessage(LINE);

        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity transfer <old name> <new name>
    private void handleTransfer(CommandSender sender, String[] args) {
        if (!Permission.TRANSFER.has(sender)) {
            messageManager.send(sender, MessageKey.NO_PERMISSION);
            return;
        }

        if (args.length < 3) {
            messageManager.send(sender, MessageKey.USAGE_TRANSFER);
            return;
        }

        String fromName = args[1];
        String toName = args[2];

        if (fromName.equalsIgnoreCase(toName)) {
            messageManager.send(sender, MessageKey.TRANSFER_SAME_NAMES);
            return;
        }

        messageManager.send(sender, MessageKey.TRANSFER_SEARCHING, "from", fromName, "to", toName);

        identityAPI.transfer(fromName, toName).thenAcceptAsync(result -> {
            switch (result) {
                case SUCCESS:
                    sender.sendMessage(LINE);
                    messageManager.send(sender, MessageKey.TRANSFER_SUCCESS);
                    messageManager.send(sender, MessageKey.TRANSFER_FROM, "player", fromName);
                    messageManager.send(sender, MessageKey.TRANSFER_TO, "player", toName);
                    messageManager.send(sender, MessageKey.TRANSFER_DATA_MOVED);
                    sender.sendMessage(LINE);
                    break;
                case FROM_NOT_FOUND:
                    sender.sendMessage(messageManager.format(MessageKey.PLAYER_NOT_FOUND, "player", fromName));
                    break;
                case TO_NOT_FOUND:
                    sender.sendMessage(messageManager.format(MessageKey.PLAYER_NOT_FOUND, "player", toName));
                    break;
                case SAME_IDENTITY:
                    sender.sendMessage(messageManager.format(MessageKey.TRANSFER_SAME_IDENTITY));
                    break;
                case FROM_IS_ONLINE:
                    sender.sendMessage(messageManager.format(MessageKey.TRANSFER_FROM_ONLINE, "player", fromName));
                    break;
                case ERROR:
                    sender.sendMessage(messageManager.format(MessageKey.TRANSFER_ERROR));
                    break;
            }
        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity lookup <name>
    private void handleLookup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageManager.send(sender, MessageKey.USAGE_LOOKUP);
            return;
        }

        String player = args[1];
        messageManager.send(sender, MessageKey.LOOKUP_SEARCHING, "player", player);

        identityAPI.findByName(player).thenComposeAsync(optIdentity -> {
            if (!optIdentity.isPresent()) {
                sender.sendMessage(messageManager.format(MessageKey.PLAYER_NOT_FOUND, "player", player));
                return CompletableFuture.completedFuture(Collections.emptyList());
            }

            String ip = optIdentity.get().getLastIp();
            return identityAPI.findAllByIp(ip);
        }).thenAcceptAsync(accounts -> {
            if (accounts.isEmpty()) return;

            List<PlayerIdentity> alts = accounts.stream()
                    .filter(i -> !i.getLastName().equalsIgnoreCase(player))
                    .collect(Collectors.toList());

            sender.sendMessage(LINE);
            messageManager.send(sender, MessageKey.LOOKUP_HEADER, "player", player);

            if (alts.isEmpty()) {
                sender.sendMessage(messageManager.format(MessageKey.LOOKUP_EMPTY));
            } else {
                messageManager.send(sender, MessageKey.LOOKUP_ACCOUNTS_HEADER, "count", String.valueOf(alts.size()));
                for (PlayerIdentity alt : alts) {
                    boolean online = identityAPI.isOnline(alt.getId());
                    messageManager.send(sender,
                            MessageKey.LOOKUP_ENTRY,
                            "name", alt.getLastName(),
                            "id", String.valueOf(alt.getId()),
                            "status", online ? "§a●" : "§c●");
                }
            }

            sender.sendMessage(LINE);

        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    private void handleReload(CommandSender sender) {
        if (!Permission.RELOAD.has(sender)) {
            sender.sendMessage(messageManager.getMessage(MessageKey.NO_PERMISSION));
            return;
        }

        messageManager.reload();
        messageManager.send(sender, MessageKey.CONFIG_RELOADED);
    }

    private void showIdentity(CommandSender sender, PlayerIdentity identity) {
        if (identity == null) {
            messageManager.send(sender, MessageKey.IDENTITY_NOT_AVAILABLE);
            return;
        }

        boolean isOnline = identityAPI.isOnline(identity.getId());

        sender.sendMessage(LINE);
        sender.sendMessage("§eIdentity Info §8| "
                + (isOnline ? "§a● Online" : "§c● Offline"));
        sender.sendMessage(LINE);
        sender.sendMessage("  §7ID §8» §f" + identity.getId());
        sender.sendMessage("  §7Last name §8» §a" + identity.getLastName()
                + (isOnline ? " §7(online)" : ""));
        sender.sendMessage("  §7Registered   §8» §f" + identity.getRegisteredName());
        sender.sendMessage("  §7Last IP    §8» §f" + identity.getLastIp());
        sender.sendMessage("  §7Registered at §8» §f" + format(identity.getRegisteredAt()));
        sender.sendMessage("  §7Last seen at §8» §f" + format(identity.getLastSeenAt()));

        List<String> history = identity.getNameHistory();
        if (history.isEmpty()) {
            messageManager.send(sender, MessageKey.INFO_NAME_HISTORY_EMPTY);
        } else {
            sender.sendMessage("  §7Name history §8» §e"
                    + String.join("§7, §e", history)
                    + " §8(" + history.size() + ")");
        }

        sender.sendMessage(LINE);
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(LINE);
        sender.sendMessage("§eIdentityCore §7- Commands");
        sender.sendMessage("  §f/" + label + " info §7<name> §8- §7Identity info");
        sender.sendMessage("  §f/" + label + " history §7<name> §8- §7Name history");
        sender.sendMessage("  §f/" + label + " transfer §7<old name> <new name> §8- §7Identity transfer");
        sender.sendMessage("  §f/" + label + " lookup §7<name> §8- §7Alt accounts");
        sender.sendMessage("  §f/" + label + " reload §8- §7Reload config");
        sender.sendMessage(LINE);
    }

    private String format(long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(FORMATTER);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String[] args) {
        if (!Permission.COMMAND.has(sender)) return Collections.emptyList();

        if (args.length == 1) {
            return Stream.of("info", "history", "transfer", "lookup")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("info")
                || args[0].equalsIgnoreCase("history")
                || args[0].equalsIgnoreCase("lookup"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}