# IdentityCore

> A lightweight Bukkit plugin that provides **stable player identity** across name changes — built for cracked and premium servers alike.

---

## The Problem

On cracked (offline-mode) servers, Minecraft generates a player's UUID from their username. If a player changes their nickname, they get a **new UUID** — and every plugin that relies on that UUID treats them as a completely new player. Economy balances, ranks, statistics: all gone.

Even on premium servers, using a player's name as a database key is fragile. IdentityCore solves this once and for all.

---

## How It Works

IdentityCore assigns every player a **stable internal ID** (auto-incremented integer) the first time they join. This ID never changes, regardless of:

- Username changes (premium)
- UUID changes (cracked)
- Server transfers

Every other plugin on your server uses this ID as a foreign key. The player can change their name a hundred times — their data follows them.

```
identity_players
─────────────────────────────────────────────────────────
id=42  bukkit_uuid="550e8400-..."  last_name="Alex"  registered_name="Steve"
```

```sql
-- Economy plugin table
player_id=42  money=15000.00   -- still there after a name change
```

---

## Features

- **Stable integer ID** — immutable identifier for every player, used as FK by dependent plugins
- **Name history tracking** — full log of every nickname a player has ever used
- **Cross-plugin event system** — `PlayerIdentityLoadEvent`, `NameChangeEvent`, `IdentityTransferEvent`
- **Manual identity transfer** — `/identity transfer <old> <new>` for merging cracked accounts
- **LuckPerms support** — permissions and groups are automatically migrated on transfer (optional, requires LuckPerms)
- **Multi-database support** — MySQL, MariaDB, PostgreSQL, SQLite
- **Built on [Loadit](https://github.com/ytnoos/loadit)** — race-condition-safe async data loading
- **Zero external dependencies at runtime** — drivers are downloaded automatically on first boot

---

## Installation

1. Download the latest `identity-core-x.x.x.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Start the server once to generate `plugins/IdentityCore/config.yml`
4. Configure your database and restart

---

## Configuration

```yaml
database:
  type: mysql         # mysql | mariadb | postgresql | sqlite

  host: localhost
  port: 3306
  name: identitycore
  user: root
  password: yourpassword

  # only used when type is sqlite
  file: plugins/IdentityCore/identitycore.db
```

---

## LuckPerms Integration

IdentityCore has **built-in LuckPerms support**. No extra plugins or bridges needed.

When `/identity transfer <old> <new>` is executed, IdentityCore automatically:

1. Copies all permissions, groups and metadata from the old UUID to the new one via the LuckPerms API
2. Saves the updated user data
3. Deletes the old UUID's data from LuckPerms

This means a cracked player who changes their nickname and gets transferred will keep all their ranks and permissions without any manual intervention.

> **Note:** LuckPerms integration is optional. If LuckPerms is not installed, IdentityCore works normally and simply skips the permission migration step.

---

## API Usage

Add IdentityCore as a dependency in your `plugin.yml`:

```yaml
depend: [IdentityCore]
```

Then use the API in your plugin:

```java
IdentityAPI api = IdentityCorePlugin.getAPI();

// get identity of an online player (instant, from cache)
api.getIdentity(player).ifPresent(identity -> {
    int id = identity.getId();           // use this as FK in your tables
    String name = identity.getLastName();
    List<String> history = identity.getNameHistory();
});

// look up any player (async, hits the database)
api.findByName("Steve").thenAccept(opt -> {
    opt.ifPresent(identity -> {
        // identity.getId(), identity.getLastIp(), etc.
    });
});
```

### Listening to events

```java
// fired when a player's identity is loaded (safe to load your own data here)
@EventHandler
public void onIdentityLoad(PlayerIdentityLoadEvent event) {
    int playerId = event.getIdentity().getId();
    myRepository.getOrCreate(playerId);
}

// fired when a premium player changes their username
@EventHandler
public void onNameChange(NameChangeEvent event) {
    String oldName = event.getOldName();
    String newName = event.getNewName();
}

// fired before a transfer completes — migrate your own data here
@EventHandler
public void onTransfer(IdentityTransferEvent event) {
    myRepository.transferPlayer(event.getFromId(), event.getToId());
}
```

### Gradle dependency

```groovy
repositories {
    maven { url = "https://repo.wiceh.com/releases" }
}

dependencies {
    compileOnly "com.wiceh.identitycore:identity-core:1.0.0"
}
```

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/identity info <name>` | View identity info for any player | `identitycore.identity.info` |
| `/identity history <name>` | View full name change history | `identitycore.identity.info` |
| `/identity transfer <old> <new>` | Merge two identities — also migrates LuckPerms data | `identitycore.identity.transfer` |

---

## Compatibility

| Server Software | Versions |
|---|---|
| Spigot | 1.8 — latest |
| Paper | 1.8 — latest |
| online-mode | `true` and `false` |
| Java | 8+ |
| LuckPerms | 5.x (optional) |

---

## Building from source

```bash
git clone https://github.com/wiceh/identity-core.git
cd identity-core
./gradlew shadowJar
```

Output: `build/libs/identity-core-x.x.x.jar`

---

## License

MIT — see [LICENSE](LICENSE)
