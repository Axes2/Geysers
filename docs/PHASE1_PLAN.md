# Geysers — Phase 1 Living Plan (Visuals & Audio)

Target: Minecraft 1.21.1 · NeoForge 21.1.235 · Java 21 · NeoGradle 7.1.38 (Gradle 8.14.3)

Mod id `geysers`, package `com.axes2.geysers`.

## Guiding decisions
- **State authoritative on server; visuals on client.** One `phase` enum + one
  `intensity` float (0..1) drive every emitter/renderer/sound. Phase 3 physics will
  later write the same fields — nothing downstream changes.
- **Client re-derives intensity** from `(phase, elapsed, GeyserStyle)` instead of
  streaming it per-tick. Server syncs only `phase`, `phaseStartGameTime`,
  `phaseStartIntensity`, `styleId` on phase changes (cheap, low-rate).
- **Particle-first visuals.** Build the eruption from particles alone first and judge
  it in-game. Only if the coherent water *column mass* or the *blue-bubble dome* don't
  sell do we add a minimal amount of geometry (BER) for exactly those two things.
  Everything else stays particles.
- **Maximum tunability during development.** All look/timing params live in
  `GeyserStyle`. Once it moves to a datapack registry (M2) it hot-reloads via `/reload`
  and auto-syncs to clients. `/geyser` commands give instant live overrides.

## Verification model (this environment)
- Local `./gradlew build` is blocked (egress policy denies `maven.neoforged.net`).
- **CI (GitHub Actions) is the build gate** — every push must stay green.
- **You drive `runClient` locally** for the visual/audio passes.

## Milestones
- [x] **M0 — Scaffold.** Rebrand template, empty registry holders, wrapper→8.14.3, CI green.
- [ ] **M1 — Trigger slice.** Vent block + block entity + phase machine + `/geyser`
      debug commands + server→client state sync. No visuals; drive/observe via commands.
- [ ] **M2 — Particles (particle-first).** Steam/spray/mist/bubble/splash particle types
      + client emission scaled by intensity. Move `GeyserStyle` to a hot-reloadable
      datapack registry. CLIENT config for global density/caps. **Review checkpoint:**
      judge the pure-particle look before spending anything on geometry.
- [ ] **M3 — Column decision.** Only if M2 review says the column mass / blue-bubble dome
      needs it: add the minimal BER geometry for those. Otherwise skip.
- [ ] **M4 — Sync polish & partialTick.** One-shot phase events (surge crack, splash)
      via a custom payload; smooth 60fps interpolation.
- [ ] **M5 — Audio.** Layered, phase-synced sounds (priming rumble, surge, roar, steam, tail).
- [ ] **M6 — Styles.** Cone / fountain (blue bubble + violent steam) / hot spring / spouter
      presets as datapack JSON + debug switcher.
- [ ] **M7 — Polish, config & LOD.** Mist drift, wetness, runoff; distance LOD; per-vent
      + global particle caps; optional rainbow/haze behind toggles.
- [ ] **M8 — Watchability pass.** Tune tells, false-starts, blue bubble, violent steam;
      add the creative "Geyser Wand".

## Debug surface (`/geyser`, op-only)
- `start` — trigger the nearest vent (within 8 blocks) into its eruption cycle.
- `reset` — force back to dormant.
- `phase <name>` — jump to a specific phase.
- `intensity <0..1>` — pin intensity (freezes the machine; ideal for tuning a look).
- `auto` — clear the pin, resume the cycle.
- `info` — print phase / intensity / style.
- `style <id>` — swap the active GeyserStyle (more ids once datapack styles land in M2).
