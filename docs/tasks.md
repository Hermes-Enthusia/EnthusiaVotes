# Tasks — EnthusiaVotes

## Completed

### TASK-001 — VoteItemFactory and gold delivery
**Tag:** INFRA
**References:** REQ-002, REQ-009
**Evidence:** ✅
- [x] `VoteItemFactory.kt` — `rawGold()` with Unbreaking 1 + HIDE_ENCHANTS
- [x] `BukkitGoldDelivery.kt` — delivers to inventory, drops excess on ground
- [x] Wired via `GoldDelivery` interface in `ServiceModule`

### TASK-002 — Votifier event listener with UUID resolution
**Tag:** INFRA
**References:** REQ-001, REQ-010
**Evidence:** ✅
- [x] `VotifierVoteListener.kt` — handles `VotifierEvent`, delegates to `VoteService`
- [x] UUID resolved via `Bukkit.getOfflinePlayer(playerName).uniqueId`
- [x] Registered in `EnthusiaVotesPlugin.onEnable` via `server.pluginManager.registerEvents`

### TASK-003 — Vote processing service
**Tag:** TDD
**References:** REQ-001, REQ-002, REQ-003, REQ-009
**Evidence:** ✅
- [x] `VoteService.processVote()` — saves to DB, delivers gold, computes streak, broadcasts
- [x] Triggers `VotePartyService.onVote()` for party tracking
- [x] Broadcasts vote announcement via `VoteBroadcaster`

### TASK-004 — Reward service with streak multipliers
**Tag:** TDD
**References:** REQ-002, REQ-003, REQ-004
**Evidence:** ✅
- [x] `RewardService.calculateGold()` — randomizes gold with streak + party multipliers
- [x] `RewardService.buildVoteMessage()` — MiniMessage shadow text with escaped player names
- [x] Streak multipliers: 1.5x (3d), 2x (7d), 3x (30d)

### TASK-005 — VoteParty state machine
**Tag:** TDD
**References:** REQ-004, REQ-005
**Evidence:** ✅
- [x] `VotePartyService` — vote counter, threshold-based activation (default 100)
- [x] Auto-deactivation via Bukkit scheduler (`runTaskLater`) after configurable duration (default 5m)
- [x] `getCurrentMultiplier()` returns 2.0 during party, 1.0 otherwise
- [x] Broadcast on activation via gradient MiniMessage shadow text

### TASK-006 — /vote command
**Tag:** INFRA
**References:** REQ-006, REQ-013
**Evidence:** ✅
- [x] `VoteCommand.execute()` — shows total votes, current streak, best streak
- [x] Lists vote sites with clickable `<click:open_url>` links
- [x] MiniMessage shadow text throughout
- [x] `VoteBukkitCommand` — registered via `server.commandMap.register`

### TASK-007 — /votesites command
**Tag:** INFRA
**References:** REQ-007, REQ-013
**Evidence:** ✅
- [x] `VoteSitesCommand.execute()` — clickable vote site menu
- [x] MiniMessage shadow text with `<click:open_url>` links
- [x] `VoteSitesBukkitCommand` — registered via `server.commandMap.register`

### TASK-008 — Bedrock form support for /vote
**Tag:** INFRA
**References:** REQ-006
**Evidence:** ✅
- [x] `BedrockVoteForm` — Cumulus `SimpleForm` with vote stats + site buttons
- [x] `isBedrockPlayer()` — Floodgate API check with graceful fallback
- [x] `VoteBukkitCommand` routes Bedrock to form, Java to chat
- [x] Button click sends URL as clickable chat message

### TASK-009 — PlaceholderAPI integration
**Tag:** INFRA
**References:** none
**Evidence:** ✅
- [x] `EnthusiaVotesExpansion` — extends `PlaceholderExpansion`
- [x] Placeholders: `%enthusiavotes_total%`, `%enthusiavotes_streak%`, `%enthusiavotes_best_streak%`
- [x] Party placeholders: `%enthusiavotes_party_active%`, `%enthusiavotes_party_votes%`, `%enthusiavotes_party_remaining%`
- [x] Auto-registration in `onEnable` if PlaceholderAPI present

### TASK-010 — /evadmin command implementation
**Tag:** INFRA
**References:** REQ-008
**Evidence:** ✅
- [x] Subcommands: `forceparty`, `stats` (top voters), `party` (status)
- [x] Permission: `enthusiavotes.admin`
- [x] `EVAdminBukkitCommand` registered via `server.commandMap.register`

### TASK-011 — PlayerStats table integration in /vote
**Tag:** TDD
**References:** REQ-006, REQ-009
**Evidence:** ✅
- [x] `/vote` pulls real stats from `SqliteVoteRepository.getStats()`
- [x] `PlayerStatsTable` Exposed table with `player_uuid` PK, `total_votes`, `current_streak`, `best_streak`, `last_vote_at`
- [x] Auto-migration via `SchemaUtils.createMissingTablesAndColumns`
- [x] Streak computation with 36-hour window in `computeNewStreak()`

## Pending

*(all tasks complete — ready for integration testing)*
