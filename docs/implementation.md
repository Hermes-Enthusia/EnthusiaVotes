# Implementation — EnthusiaVotes

## Layer Dependency Rules

```
domain ← application ← infrastructure
```

- **domain** — Pure Kotlin data classes and interfaces. No platform imports.
- **application** — Use cases, services, repository interfaces. Depends on domain only.
- **infrastructure** — Bukkit adapters, persistence, config, DI, messaging. Depends on domain + application.

## Forbidden Domain Annotations

```yaml
forbidden: []
```

## Module map

| Layer | Package | Contents |
|---|---|---|
| domain | `net.badgersmc.votes.domain` | `VoteRecord`, `PlayerStats`, `VoteParty`, `VotePartyState`, `VoteReward`, `StreakReward`, `VoteSite` |
| application | `net.badgersmc.votes.application` | `VoteService`, `RewardService`, `VotePartyService`, `VoteCommand`, `VoteSitesCommand`, `EVAdminCommand`, `VoteRepository`, `VoteBroadcaster`, `GoldDelivery` |
| infrastructure | `net.badgersmc.votes.infrastructure.bukkit` | `EnthusiaVotesPlugin`, `VotifierVoteListener`, `VoteBukkitCommand`, `VoteSitesBukkitCommand`, `EVAdminBukkitCommand`, `VoteItemFactory`, `BukkitGoldDelivery` |
| infrastructure | `net.badgersmc.votes.infrastructure.config` | `VoteConfig`, `VoteSite` |
| infrastructure | `net.badgersmc.votes.infrastructure.di` | `ServiceModule`, `VoteScheduler` |
| infrastructure | `net.badgersmc.votes.infrastructure.persistence` | `DatabaseFactory`, `VoteTable`, `PlayerStatsTable`, `SqliteVoteRepository`, `Migrations` |
| infrastructure | `net.badgersmc.votes.infrastructure.messaging` | `BukkitVoteBroadcaster` |
| infrastructure | `net.badgersmc.votes.infrastructure.form` | `BedrockVoteForm` |
| infrastructure | `net.badgersmc.votes.infrastructure.papi` | `EnthusiaVotesExpansion` |
| loader | `net.badgersmc.votes.loader` | `EnthusiaVotesLoader` |

## Command registration

All commands use `server.commandMap.register(name, command)` — never `getCommand()` or `paper-plugin.yml` command stanzas. This avoids the `UnsupportedOperationException` on Paper 1.21+.

## Database

SQLite via Exposed. Tables:
- `votes` — vote history (id, player_uuid, player_name, service_name, timestamp, gold_awarded)
- `player_stats` — per-player aggregates (player_uuid, total_votes, current_streak, best_streak, last_vote_at)

## DI pattern

Manual constructor injection via `ServiceModule`. Lazy initialization with `by lazy` to avoid circular references. Every component receives its dependencies as constructor parameters.
