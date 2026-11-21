# Debugging & Logging

Arcaneresearch uses a central logging facade (`arcaneresearch.debug.ArcaneLogger`)
on top of a single SLF4J logger named `arcaneresearch`. All mod code should log
through it instead of creating ad-hoc loggers, using `System.out` or calling
`printStackTrace()`.

## Command

The debug command is **client-side** (`RegisterClientCommandsEvent`). It runs
locally in singleplayer and on any multiplayer server — even when the server has
no commands enabled or does not have the mod installed. It is never forwarded to
the server.

```
/arcaneresearch debug <levels> [type]
/arcaneresearch debug status | reset | stop | help
```

* `<levels>` (required) — comma separated verbosity levels:
  `TRACE, DEBUG, INFO, WARN, ERROR, ALL, OFF` (numeric aliases `0`=OFF …
  `5`=TRACE also work).
  A single level acts as a minimum threshold (e.g. `DEBUG` enables
  `DEBUG, INFO, WARN, ERROR`). `OFF` stops the session.
* `[type]` (optional, defaults to `ALL`) — profiling subsystem:
  `ALL, CORE, WORLD, AURA, NETWORK, ENTITIES, RENDER, RESEARCH, ITEMS, BLOCKS,
  COMPAT, PERFORMANCE, MEMORY`.

Examples:

```
/arcaneresearch debug DEBUG AURA   # verbose aura logs
/arcaneresearch debug TRACE        # everything, all subsystems
/arcaneresearch debug INFO NETWORK # network subsystem at INFO+
/arcaneresearch debug status       # counters, timings and memory
/arcaneresearch debug OFF          # stop logging / profiling
```

**Debug is disabled by default.** No session, no timings and no TRACE/DEBUG
output exist until the command is run. The Log4j appenders permit DEBUG
(`forge.logging.console.level=debug`), but `ArcaneLogger` is the gate: it drops
TRACE/DEBUG unless a session is active, so nothing is printed by default. For
startup problems there is an opt-in JVM flag (still off unless explicitly provided):

```
-Darcaneresearch.debug=DEBUG -Darcaneresearch.debug.type=AURA
```

`WARN` and `ERROR` are always written, regardless of the active filters:
critical diagnostics are never silently dropped.

## Using the facade

```java
private static final ArcaneLogger LOGGER =
        ArcaneLogger.forCategory(ProfileCategory.AURA);

LOGGER.debug("Aura tick for {}: {} vis", dimension, vis);
LOGGER.error("Failed to save aura", exception);

try (ArcaneLogger.Timing ignored = LOGGER.timer("aura.tick")) {
    // measured while a matching debug session is active
}

LOGGER.once("missing-node", LogLevel.WARN, "Aura node {} missing", key);
```

`Arcaneresearch.LOGGER` is the `CORE` instance of this facade. Existing
`LOGGER.info/debug/warn/error(...)` call sites keep working unchanged and now
route through the session filters.

## What the session tracks

`DebugSession` (thread-safe) keeps:

* active level/category filters;
* a start timestamp and log-line counters per level;
* named timing statistics (count, total, average, min, max);
* a bounded set of keys used by `once(...)`;
* a JVM memory summary shown by `debug status`.

Built-in timing probes: `aura.process` (category `AURA`), `maze.ensure` /
`maze.arrival.neighborhood` (category `WORLD`) and `render.architect`
(category `RENDER`). They appear in `debug status` only while a matching
session is active.

Renderer diagnostics (category `RENDER`) are emitted through `once(...)` so a
busy frame never floods the log: aspect tags, scan popup targets, architect
overlay, seals, jar liquid, fog overrides, HUD overlay, golem rendering,
post-shader and teleport-warp shader state, plus OBJ model load details for
crystal/wand meshes. Use `/arcaneresearch debug DEBUG RENDER` to see them.

Statistics are cleared when a new session starts and can be cleared manually
with `/arcaneresearch debug reset`.
