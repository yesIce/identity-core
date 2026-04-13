package com.wiceh.identitycore.api.constants;

public enum MessageKey {

    NO_PERMISSION("§cYou don't have permission."),
    ONLY_PLAYERS("§cOnly players can use this command."),
    PLAYER_NOT_FOUND("§cNo identity found for §f{player}"),
    IDENTITY_NOT_AVAILABLE("§cIdentity not available."),
    SEARCHING("§7Searching..."),

    USAGE_INFO("§cUsage: /identity info <name>"),
    USAGE_HISTORY("§cUsage: /identity history <name>"),
    USAGE_TRANSFER("§cUsage: /identity transfer <old name> <new name>"),
    USAGE_LOOKUP("§cUsage: /identity lookup <name>"),

    TRANSFER_SEARCHING("§7Transferring from §f{from} §7→ §f{to}§7..."),
    TRANSFER_SUCCESS("§aTransfer completed successfully."),
    TRANSFER_FROM("  §7From     §8» §f{player}"),
    TRANSFER_TO("  §7To       §8» §f{player}"),
    TRANSFER_DATA_MOVED("  §7Name history and data transferred."),
    TRANSFER_SAME_NAMES("§cThe two names cannot be the same."),
    TRANSFER_SAME_IDENTITY("§cThe two names already belong to the same identity."),
    TRANSFER_FROM_ONLINE("§c§f{player} §cis online. Make them disconnect before transferring."),
    TRANSFER_FROM_NOT_FOUND("§cAccount §f{player} §cnot found."),
    TRANSFER_TO_NOT_FOUND("§cAccount §f{player} §cnot found."),
    TRANSFER_ERROR("§cError during transfer. Check the logs."),

    LOOKUP_SEARCHING("§7Searching accounts by IP of §f{player}§7..."),
    LOOKUP_HEADER("§eLookup IP §8| §f{player}"),
    LOOKUP_EMPTY("  §7No other accounts found for this IP."),
    LOOKUP_ACCOUNTS_HEADER("  §7Accounts with same IP §8({count})§7:"),
    LOOKUP_ENTRY("  §8- §f{name} §8(ID: {id}) {status}"),
    INFO_NAME_HISTORY_EMPTY(""),

    CONFIG_RELOADED("§aConfiguration reloaded.");

    private final String defaultMessage;

    MessageKey(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getFormattedKey() {
        return name().toLowerCase().replace("_", "-");
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
