package com.wiceh.identitycore.impl.command;

import com.wiceh.identitycore.api.IdentityAPI;
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

    private static final String PERMISSION = "identitycore.identity.info";
    private static final String LINE = "§8§m" + new String(new char[30]).replace("\0", "─");

    private final IdentityAPI identityAPI;
    private final Plugin plugin;

    public IdentityCommand(IdentityAPI identityAPI, Plugin plugin) {
        this.identityAPI = identityAPI;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cNon hai il permesso.");
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
            default:
                sendUsage(sender, label);
        }

        return true;
    }

    // /identity info <nome|uuid>
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cUso: /identity info <nome|uuid>");
                return;
            }
            showIdentity(sender, identityAPI.getIdentity(((Player) sender).getUniqueId())
                    .orElse(null));
            return;
        }

        String input = args[1];
        sender.sendMessage("§7Ricerca in corso...");

        identityAPI.findByName(input).thenAcceptAsync(opt -> {
            if (!opt.isPresent()) {
                sender.sendMessage("§cNessuna identità trovata per §f" + input);
                return;
            }
            showIdentity(sender, opt.get());
        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity history <nome|uuid>
    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /identity history <nome>");
            return;
        }

        String input = args[1];
        sender.sendMessage("§7Ricerca storico per §f" + input + "§7...");

        identityAPI.findByName(input).thenAcceptAsync(opt -> {
            if (!opt.isPresent()) {
                sender.sendMessage("§cNessuna identità trovata per §f" + input);
                return;
            }

            PlayerIdentity identity = opt.get();
            List<String> history = identity.getNameHistory();

            sender.sendMessage(LINE);
            sender.sendMessage("§eStorico nomi §7- §f" + identity.getLastName());

            if (history.isEmpty()) {
                sender.sendMessage("  §7Nessun cambio nome registrato.");
            } else {
                sender.sendMessage("  §7Nomi precedenti §8(" + history.size() + ")§7:");
                for (int i = 0; i < history.size(); i++) {
                    sender.sendMessage("  §8" + (i + 1) + ". §f" + history.get(i));
                }
            }

            sender.sendMessage(LINE);

        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity transfer <vecchio> <nuovo>
    private void handleTransfer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("identitycore.identity.transfer")) {
            sender.sendMessage("§cNon hai il permesso.");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUso: /identity transfer <vecchioNome> <nuovoNome>");
            return;
        }

        String fromName = args[1];
        String toName = args[2];

        if (fromName.equalsIgnoreCase(toName)) {
            sender.sendMessage("§cI due nomi non possono essere uguali.");
            return;
        }

        sender.sendMessage("§7Trasferimento in corso da §f" + fromName + " §7→ §f" + toName + "§7...");

        identityAPI.transfer(fromName, toName).thenAcceptAsync(result -> {
            switch (result) {
                case SUCCESS:
                    sender.sendMessage(LINE);
                    sender.sendMessage("§aTransferimento completato con successo.");
                    sender.sendMessage("  §7Da     §8» §f" + fromName);
                    sender.sendMessage("  §7A      §8» §f" + toName);
                    sender.sendMessage("  §7Storico nomi e dati trasferiti.");
                    sender.sendMessage(LINE);
                    break;
                case FROM_NOT_FOUND:
                    sender.sendMessage("§cAccount §f" + fromName + " §cnon trovato.");
                    break;
                case TO_NOT_FOUND:
                    sender.sendMessage("§cAccount §f" + toName + " §cnon trovato.");
                    break;
                case SAME_IDENTITY:
                    sender.sendMessage("§cI due nomi appartengono già alla stessa identità.");
                    break;
                case FROM_IS_ONLINE:
                    sender.sendMessage("§c§f" + fromName + " §cè online. Fallo disconnettere prima del transfer.");
                    break;
                case ERROR:
                    sender.sendMessage("§cErrore durante il trasferimento. Controlla i log.");
                    break;
            }
        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    // /identity lookup <player>
    private void handleLookup(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /identity lookup <nome>");
            return;
        }

        String player = args[1];
        sender.sendMessage("§7Ricerca account per IP di §f" + player + "§7...");

        identityAPI.findByName(player).thenComposeAsync(optIdentity -> {
            if (!optIdentity.isPresent()) {
                sender.sendMessage("§cNessuna identità trovata per §f" + player);
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
            sender.sendMessage("§eLookup IP §8| §f" + player);

            if (alts.isEmpty()) {
                sender.sendMessage("  §7Nessun altro account trovato per questo IP.");
            } else {
                sender.sendMessage("  §7Account con stesso IP §8(" + alts.size() + ")§7:");
                for (PlayerIdentity alt : alts) {
                    boolean online = identityAPI.isOnline(alt.getId());
                    sender.sendMessage("  §8- §f" + alt.getLastName()
                            + " §8(ID: " + alt.getId() + ")"
                            + (online ? " §a●" : " §c●"));
                }
            }

            sender.sendMessage(LINE);

        }, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
    }

    private void showIdentity(CommandSender sender, PlayerIdentity identity) {
        if (identity == null) {
            sender.sendMessage("§cIdentità non disponibile.");
            return;
        }

        boolean isOnline = identityAPI.isOnline(identity.getId());

        sender.sendMessage(LINE);
        sender.sendMessage("§eIdentity Info §8| "
                + (isOnline ? "§a● Online" : "§c● Offline"));
        sender.sendMessage(LINE);
        sender.sendMessage("  §7ID §8» §f" + identity.getId());
        sender.sendMessage("  §7Nome attuale §8» §a" + identity.getLastName()
                + (isOnline ? " §7(online)" : ""));
        sender.sendMessage("  §7Registrato   §8» §f" + identity.getRegisteredName());
        sender.sendMessage("  §7Ultimo IP    §8» §f" + identity.getLastIp());
        sender.sendMessage("  §7Primo accesso §8» §f" + format(identity.getRegisteredAt()));
        sender.sendMessage("  §7Ultimo accesso §8» §f" + format(identity.getLastSeenAt()));

        List<String> history = identity.getNameHistory();
        if (history.isEmpty()) {
            sender.sendMessage("  §7Storico nomi §8» §7nessuno");
        } else {
            sender.sendMessage("  §7Storico nomi §8» §e"
                    + String.join("§7, §e", history)
                    + " §8(" + history.size() + ")");
        }

        sender.sendMessage(LINE);
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(LINE);
        sender.sendMessage("§eIdentityCore §7- Comandi disponibili");
        sender.sendMessage("  §f/" + label + " info §7<nome> §8- §7Info identità");
        sender.sendMessage("  §f/" + label + " history §7<nome> §8- §7Storico nomi");
        sender.sendMessage("  §f/" + label + " transfer §7<vecchio nome> <nuovo nome> §8- §7Trasferisci identità");
        sender.sendMessage("  §f/" + label + " lookup §7<nome> §8- §7Alt accounts");
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
        if (!sender.hasPermission(PERMISSION)) return Collections.emptyList();

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