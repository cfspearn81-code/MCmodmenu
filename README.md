# Faster

A client-side Fabric mod for Minecraft Java Edition 26.3. Press **P** in-game to open the focused module search. Type a name, click a result to toggle it, or choose a category in the sidebar. The white dot cursor and fading trail remain available for all controls.

[Download the current JAR](https://github.com/cfspearn81-code/MCmodmenu/raw/refs/tags/v1.4.1/downloads/faster-1.4.1-mc26.3.jar)

## Features and controls

The **Search** page opens automatically each time, with an empty query ready for typing. It searches all seven module names/descriptions, supports names such as `freecam` or `Free Cam`, and shows a scrollable list. **Ctrl+A** replaces the query, **Ctrl+V** pastes, **Backspace** edits, and **Ctrl+F** focuses search from another category. **Esc** closes the menu. **Enter/Tab** leaves the search field; when the field is unfocused, **P** also closes the menu and movement keys work again. Typing in search and binding a key do not move the player.

- **Speed** changes walking and sprinting speed. It starts off at launch.
- **GuiMove** permits movement while any screen is open in a loaded world, including inventory, containers, chat/search, settings and pause menus. It starts off. Keyboard/mouse movement bindings are read physically while the window is focused. Text fields still receive typing, so the same key can both type and move. With GuiMove on, opening a menu does not pause a single-player world. Existing CraftingMovement settings migrate automatically.
- **Auto Bridge** uses the restored 1.3.5 implementation: standing movement with manual crosshair targeting, a configurable hold key (B by default), the four-tick placement budget, and the enhanced edge protection. Its switch and hold key are in the P menu.
- **Auto Hit** starts off. Enable it through **Search** or **Combat** in the P menu. It attacks the live player directly under your crosshair, within normal reach and line of sight, when your held item’s vanilla attack cooldown is fully charged. It pauses in menus, while unfocused, using items, or manually attacking/using. It does not rotate your view or attack everyone on screen. Cooldown timing depends on the held weapon; servers may enforce stricter limits. The toggle is saved.
- **Xray** outlines coal, iron, gold, redstone, lapis, diamond, emerald, nether quartz and ancient debris through walls with a distinct color for each ore. It starts off. Copper is excluded. Normal/deepslate versions share one toggle; nether gold uses the Gold toggle.
- **Free Cam** detaches the camera from the player. Click its switch in Search/Movement or press **F6** to toggle it on/off. Click **Toggle: F6** beside the module and press a keyboard key to rebind it; P/Esc are reserved. Camera mode starts off in every session, and only the keybinding persists. Use your configured movement keys to fly, Space/Shift (your jump/sneak bindings) for up/down, and sprint to fly three times faster. Mouse sensitivity and inversion use the normal game settings. The camera passes through blocks, while the real player retains normal physics and server corrections. Free Cam stops camera control in menus and when unfocused; it returns to normal on death, disconnect, or world/player changes. It pauses Auto Bridge/Auto Hit and world attack/use actions while active, and preserves the selected first/third-person perspective on return. It only displays terrain already sent to the client.
- **Player ESP** draws wireframe boxes and tracers matching each player’s formatted nametag/team color, white player names, and green distances in M (meters). Off-screen players get direction indicators. It starts on.

Choose **Movement** to edit the speed multiplier. The default **1.5x** is a 50% increase over normal speed. Use the plus and minus controls or click the number, type a multiplier of at least 1, and press Enter.

Choose **Players** to edit the ESP range and name limit.

- The default range is **500 M** (one block equals one meter). ESP displays players whose positions the server currently sends to the client.
- The default name limit is **10**. When more than 10 players are displayed, names hide and boxes, tracers, and distances continue to render. Raising the limit changes that cutoff. Setting it to zero hides names whenever any player is displayed.
- Range changes use 25-block steps. Name-limit changes use one-player steps. Click either number to type an exact whole number, then press Enter.

Choose **Xray** for its switch, radius and nine ore filters. Click the radius number, type any whole number from **1–5000**, and press Enter. The default is **200 blocks**, measured as a radius in all directions from the player to the ore block center. Plus/minus changes it by 25 blocks. Each ore tile switches that type on/off, and its dot shows the outline color. Netherite means **ancient debris**, the mined source of netherite.

Xray reads only chunk data already loaded by the client; a larger radius does not load or request distant terrain, or reveal ores hidden from the client by the server. It scans unchanged sections only once, updates the cache from normal block-change/chunk events, and discards unloaded chunks and old dimensions. New ores appear as the initial scan progresses. Scan work is generated incrementally from loaded chunks, with up to 32 section checks and a 0.35 ms soft budget per tick (at most four populated sections); this avoids a full-world queue allocation when enabling a large radius. All-disabled filters suspend scanning and drawing. The overlay uses a single batched GUI element, compact primitive ore positions, allocation-free per-edge projection, and shared-edge deduplication instead of a separate transformed GUI rectangle for every line. Candidate selection is cached and refreshed at most ten times per second as the view/cache changes, while the camera projection remains smooth each frame. Normal mining/placement updates bypass that selection delay. In exceptionally dense fields, at most 4096 on-screen boxes are drawn per frame, with nearer sections first. Xray settings persist separately in `config/faster-movement-xray.properties`.

Press **Esc** or the close control to close the menu; P also closes it when search is unfocused. Existing settings remain in `config/faster-movement.properties` and `config/faster-movement-xray.properties` so the rename preserves preferences. The new camera binding saves in `config/faster-freecam.properties`.

The mod is installed in the client's `mods` folder. Multiplayer movement is subject to the server's movement rules.

## Build

Install **JDK 25** and set `JAVA_HOME` to that JDK. The included Gradle wrapper downloads its pinned Gradle distribution and build dependencies.

Linux and macOS

```sh
./gradlew clean build
```

Windows PowerShell

```powershell
.\gradlew.bat clean build
```

The installable output is `build/libs/faster-1.4.1-mc26.3.jar`. The sources JAR in that directory is for development.

Install Fabric Loader **0.20.1 or later** for Minecraft **26.3**, then put the installable JAR in the instance's `mods` folder.

To launch the development client

```sh
./gradlew runClient
```

## Continuing development

Java sources live in `src/client/java/net/fastersprint`. The mod metadata and mixin registration live in `src/main/resources`.

| File | Responsibility |
| --- | --- |
| `ClientMenu.java`, `ModuleSearch.java` | Focused module search, category layout, switches, bindings and numeric controls |
| `FreeCam.java`, `FreeCamMotion.java`, `FreeCamSettings.java` | Detached camera state, normalized flight, lifecycle and saved toggle key |
| `mixin/FreeCam*Mixin.java` | Camera/frustum alignment, mouse look, detached hands and interaction guards |
| `OreType.java`, `XraySettings.java`, `Xray.java` | Ore colors/variants, saved filters/radius, loaded-chunk scan and outlines |
| `mixin/XrayChunkMixin.java` | Tracks client chunks and block changes without periodic rescanning |
| `OreSection.java`, `OreMesh.java`, `OreProjection.java`, `OreOutlineState.java` | Compact ore cache, shared edges, frustum rejection and one batched overlay |
| `mixin/GuiRenderStateAccessor.java` | Submits the batched outline element through the normal GUI renderer |
| `PlayerDisplaySettings.java` | Defaults, validation, loading, and saving |
| `PlayerDisplayRenderer.java` | Player selection, projection, clipped boxes and tracers, labels, and name cutoff |
| `mixin/LocalPlayerMixin.java` | Speed attribute modifier and its configured amount |
| `mixin/GuiMoveInputMixin.java` | Configured movement keys while any in-world screen is open |
| `mixin/PlayerDisplayNameMixin.java` | Suppresses overlapping vanilla names while ESP renders its own labels |
| `mixin/PlayerDisplayHudMixin.java` | ESP and menu rendering |
| `mixin/PlayerDisplayInputMixin.java` | P and numeric editing while allowing movement keys |
| `mixin/ClientMenuMouseMixin.java` | Menu clicks and mouse capture |
| `mixin/ClientMenuLifecycleMixin.java` | Closes menu state when gameplay changes to another screen |

Add a feature's menu switch through `ClientMenu.Tool`, connect its enabled state and behavior, and register any additional mixins in `fastersprint.mixins.json`. Keep settings validation and persistence in `PlayerDisplaySettings` or a dedicated settings class such as `XraySettings`.

The project uses Minecraft's unobfuscated 26.3 class names. Dependencies and the release version are pinned in `gradle.properties` and `build.gradle`. Change `mod_version` in `gradle.properties` for a new release. The build expands that version into `fabric.mod.json`.

## Verification

Version 1.3.0 was tested with real 26.3 Fabric clients connected to an isolated vanilla 26.3 server.

- Walking continued while opening the menu, clicking switches, and closing it.
- A 2.0x multiplier produced approximately 8.63 blocks per second walking, compared with 4.32 with Speed off.
- Green boxes, red tracers, on-screen labels, and off-screen/behind-camera indicators rendered with a real second player.
- A client-only test fixture verified names at exactly 10 displayed players and distance-only labels at 11. Raising the limit to 11 restored the names.
- CraftingMovement was tested with a real crafting table, physical movement keys, sprint, jump, and key release. Ordinary inventory behavior was checked separately.
- Saved settings were verified in a fresh process.

Current test fixture sources are under `tests/`, outside production source roots. Downloaded Minecraft binaries are outside this repository.


This build is based on 1.3.5. The later bridge changes from 1.3.6–1.3.8 are excluded. Version 1.4.0 adds the search and Free Cam on top of Auto Hit, nametag-colored ESP and optimized Xray; the 1.3.5 placement targeting/timing are retained with an added movement-boundary guard. Uncolored players use the normal white nametag fallback.

The P menu uses a dark indigo/violet panel theme with rounded cards, animated switches, and a white dot cursor with a short fading trail. The normal cursor is restored when the menu closes or the window loses focus. Controls and existing feature behavior remain from the 1.3.5-based tools build.

While Auto Bridge is enabled and its hold key is down, a final check in vanilla player movement catches residual horizontal momentum that the input-only guard could miss. Support is checked with a 0.10-block inset of the real footprint, reserving an edge margin even at diagonal corners. An unsafe movement is shortened and blocked horizontal momentum is cleared. Pending placements do not count as support, and short steps back from the safety margin are allowed. This does not force crouching or add rotation packets. Server teleports, removing footing under the player, and a fall already underway remain outside the guarantee.

ESP tracer lines and off-screen markers use the same resolved nametag/team color as the player box. GuiMove also keeps single-player menus from pausing while enabled and focused; world-loading overlays and unfocused windows retain normal behavior.

The Xray performance regression, dense-ore measurements and validation limits are documented in `tests/xray-performance/README.md`. These are cloud test measurements, not a promise of a particular FPS on every PC.

Version 1.4.0 validation is documented in `tests/faster140-live/README.md`, with the local real-client checks and movement packet audit. The installable artifact uses the new `faster-<version>-mc<game>.jar` name; the internal mod ID stays compatible with previous builds. Replace the older mod JAR when installing.
