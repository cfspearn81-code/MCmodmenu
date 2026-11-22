# Faster

A client-side Fabric mod for Minecraft Java Edition 26.3. Press **P** in-game to open the focused module search. Type a name, click a result to toggle it, or choose a category in the sidebar. The white dot cursor and fading trail remain available for all controls.

[Download the current JAR](https://github.com/cfspearn81-code/MCmodmenu/raw/refs/tags/v1.11.0/downloads/faster-1.11.0-mc26.3.jar)

## Features and controls

The **Search** page opens automatically each time, with an empty query ready for typing. It searches all thirteen module names/descriptions, supports names such as `freecam` or `Free Cam`, and shows a scrollable list. **Ctrl+A** replaces the query, **Ctrl+V** pastes, **Backspace** edits, and **Ctrl+F** focuses search from another category. **Esc** closes the menu. **Enter/Tab** leaves the search field; when the field is unfocused, **P** also closes the menu and movement keys work again. Typing in search and binding a key do not move the player.

- **GuiMove** permits movement while any screen is open in a loaded world, including inventory, containers, chat/search, settings and pause menus. It starts off. Keyboard/mouse movement bindings are read physically while the window is focused. Text fields still receive typing, so the same key can both type and move. With GuiMove on, opening a menu does not pause a single-player world. Existing CraftingMovement settings migrate automatically.
- **Auto Bridge** extends the path beneath you as you walk. Enable it under **Movement**, equip building blocks, and hold its key (**B** by default) while moving. It picks a reachable block face along your movement direction. You can look ahead while bridging. When no valid attachment face is visible from your player or the server rejects a placement, the edge guard stops you on trusted footing. Each new bridge block needs both a separate server block update and reconciliation of the placement prediction. Auto Bridge waits twelve ticks after both signals before stepping onto it, so a confirmation followed by a quick removal cannot advance the bridge. It places at most once every four ticks and waits for the current placement without resending. Releasing the key stops placement. The edge guard remains active until you stop on trusted ground. A server that removes ground after you step onto it can still cause a fall.
- **Auto Hit** starts off. Enable it through **Search** or **Combat** in the P menu. It attacks the live player directly under your crosshair, within normal reach and line of sight, when your held item’s vanilla attack cooldown is fully charged. It pauses in menus, while unfocused, using items, or manually attacking/using. It does not rotate your view or attack everyone on screen. Cooldown timing depends on the held weapon; servers may enforce stricter limits. The toggle is saved.
- **Aim Lock** starts off, with a saved switch in Search and **Combat**. Enable it, then hold **R** (default) to acquire the nearest visible player ahead within **32 blocks** and a **90-degree cone** around your view. The chosen player stays locked while the key remains held: a closer player cannot steal it. The initial range/view-angle limits do not break an existing living lock. Release the key for manual control and a fresh selection on the next press. Target death ends that lock and permits a new eligible target. If no initial eligible player is present, holding can wait for one to enter view. Temporary entity removal pauses the existing lock; a replacement entity with the same UUID resumes it. Once acquired, the same head continues to be tracked through walls. Initial acquisition still requires block line of sight. Temporary invisibility or spectator state pauses aiming without switching players. Menus, P-menu editing, lost focus, Free Cam and Free Look also pause while preserving the held target. Disabling the module, your own death/spectating, world/player changes or changing the binding clears the old lock. Click **Hold: R** beside the module to rebind it; P/Esc are reserved. Other letter keys and function keys work. Head tracking uses interpolated positions and normal player yaw/pitch, with vanilla look packets and no automatic attacks or Auto Hit toggling. Settings save in `config/faster-aimlock.properties`.
- **Xray** outlines coal, iron, gold, redstone, lapis, diamond, emerald, nether quartz and ancient debris through walls with a distinct color for each ore. It starts off. Copper is excluded. Normal/deepslate versions share one toggle; nether gold uses the Gold toggle.
- **BedEsp** outlines beds through walls in their wool color, including all 16 dye colors. It starts off. Find it in Search or **Visuals → Beds**. Each complete bed gets one bed-height outline around both halves, including pairs spanning chunks. Click its range number to type **1–5000 blocks** (default **200**), then press Enter; plus/minus changes it by 25. Range measures from the player to the bed center in all directions. Settings save in `config/faster-bedesp.properties`.
- **Sky Tint** starts off, with a saved switch in Search and Visuals. Choose **Visuals → Sky** and drag the hue, saturation and brightness sliders. It matches the normal Overworld sky, clear background, horizon and distant haze to your selected color, with a matching sunrise/sunset glow. Native fog distances, sun, moon, stars and weather systems remain active. Underwater/lava fog keeps its native color. Nearby terrain textures are not recolored; distant terrain and clouds naturally fade into the selected horizon color. Dimensions without the normal Overworld sky keep their native rendering. Turning it off restores the natural sky. Settings save in `config/faster-sky-tint.properties`.
- **QuickFall** starts off, with a saved switch in Search and Movement. Walking off an edge applies fast downward velocity, up to **8 blocks per tick**, while preserving horizontal motion and using Minecraft’s normal collisions to land on floors and slabs. It leaves ordinary jumps alone and pauses for sneaking, climbing, Free Cam, swimming, flying, gliding and riding. It does not cancel fall damage. Its switch saves in `config/faster-quickfall.properties`.
- **QuickJump** starts off, with a saved switch in Search (type `quickjump`) and Movement. Press your normal jump key, **Space** by default, to move to the normal jump peak on the takeoff tick instead of rising gradually. The default peak is about **1.252 blocks** above your feet. It uses the jump impulse, gravity and air drag already computed by Minecraft, including Jump Boost and reduced jump surfaces, and clips the upward movement against ceilings. Horizontal sprint-jump impulse and ordinary falling remain active. It requires a real grounded jump, so it cannot add midair jumps; holding Space keeps vanilla's grounded repeat/cooldown behavior. Swimming, climbing, riding, flying, gliding, levitation and Free Cam retain normal behavior. Extreme or unsupported jump physics (non-positive gravity, a predicted rise over eight blocks or an unbounded ascent) use the original gradual jump. Settings save in `config/faster-quickjump.properties`.
- **Wall Climb** starts off and has a saved toggle in **Search** (type `wallclimb` or `ladder`) and **Movement**. Walk into a solid block wall to climb using vanilla ladder physics. Release movement to slide slowly; release movement and hold sneak to stay in place. Jump, descending speed, horizontal speed limits and fall-distance handling use the normal ladder code. Back away to leave the wall and resume ordinary falling, or keep moving to step onto a reachable ledge. Floors/ceilings are excluded from wall contact. It pauses during Free Cam, flying, gliding, riding and swimming. Auto Bridge pauses its placement and edge guard while attached to the wall so it does not fight ladder movement. Real ladders and vines retain their normal behavior. The switch saves separately in `config/faster-wallclimb.properties`.
- **Free Cam** detaches the camera from the player. Click its switch in Search/Visuals or press **F6** to toggle it on/off. Click **Toggle: F6** beside the module and press a keyboard key to rebind it; P/Esc are reserved. Camera mode starts off in every session, and only the keybinding persists. Use your configured movement keys to fly, Space/Shift (your jump/sneak bindings) for up/down, and sprint to fly three times faster. Mouse sensitivity and inversion use the normal game settings. The camera passes through blocks, while the real player retains normal physics and server corrections. Free Cam stops camera control in menus and when unfocused; it returns to normal on death, disconnect, or world/player changes. It pauses Auto Bridge/Auto Hit and world attack/use actions while active, and preserves the selected first/third-person perspective on return. It only displays terrain already sent to the client.
- **Free Look** is enabled by default. Hold **Left Alt** and move the mouse to orbit your player in third person while walking normally. The camera passes through blocks and follows your player at the normal third-person distance. Your movement and aim keep their original direction. Release the key to return to your selected perspective. Find the switch and the **Hold** key control under **Visuals** or search for `free look`. Your settings save in `config/faster-freelook.properties`. Free Cam takes priority when active, and Aim Lock pauses during the orbit.
- **Player ESP** draws wireframe boxes and tracers matching each player’s formatted nametag/team color, white player names, and green distances in M (meters). Off-screen players get direction indicators. It starts on.

The **Visuals** tab groups Xray, Player ESP, BedEsp, Free Cam, Free Look and Sky Tint. Scroll in **All** to reach every module. The **ESP**, **Xray**, **Beds** and **Sky** tabs expose their settings. All five **Movement** modules fit on one page.

Choose **Visuals → ESP** to edit the ESP range and name limit.

- The default range is **500 M** (one block equals one meter). ESP displays players whose positions the server currently sends to the client.
- The default name limit is **10**. When more than 10 players are displayed, names hide and boxes, tracers, and distances continue to render. Raising the limit changes that cutoff. Setting it to zero hides names whenever any player is displayed.
- Range changes use 25-block steps. Name-limit changes use one-player steps. Click either number to type an exact whole number, then press Enter.

Choose **Visuals → Xray** for its switch, radius and nine ore filters. Click the radius number, type any whole number from **1–5000**, and press Enter. The default is **200 blocks**, measured as a radius in all directions from the player to the ore block center. Plus/minus changes it by 25 blocks. Each ore tile switches that type on/off, and its dot shows the outline color. Netherite means **ancient debris**, the mined source of netherite.

Xray reads only chunk data already loaded by the client; a larger radius does not load or request distant terrain, or reveal ores hidden from the client by the server. It scans unchanged sections only once, updates the cache from normal block-change/chunk events, and discards unloaded chunks and old dimensions. New ores appear as the initial scan progresses. Scan work is generated incrementally from loaded chunks, with up to 32 section checks and a 0.35 ms soft budget per tick (at most four populated sections); this avoids a full-world queue allocation when enabling a large radius. All-disabled filters suspend scanning and drawing. The overlay uses a single batched GUI element, compact primitive ore positions, allocation-free per-edge projection, and shared-edge deduplication instead of a separate transformed GUI rectangle for every line. Candidate selection is cached and refreshed at most ten times per second as the view/cache changes, while the camera projection remains smooth each frame. Normal mining/placement updates bypass that selection delay. In exceptionally dense fields, at most 4096 on-screen boxes are drawn per frame, with nearer sections first. Xray settings persist separately in `config/faster-movement-xray.properties`.

BedEsp uses the same loaded-chunk, palette-filtered, event-driven scan and compact section cache as Xray, with the same bounded per-tick scan work. Unchanged sections are not repeatedly scanned. Both bed halves share one outline; edges are deduplicated and rendered in one batched overlay, with no per-edge allocations. Selection refreshes at most ten times per second as the view changes; block updates invalidate it immediately. At most 1024 visible beds are drawn, with nearer sections first. Raising the radius does not load/request chunks or reveal beds the server has not sent. Turning the module off releases its scan results; unloaded chunks and old worlds are discarded.

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

The installable output is `build/libs/faster-1.11.0-mc26.3.jar`. The sources JAR in that directory is for development.

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
| `BedEsp.java`, `BedGeometry.java`, `BedMesh.java`, `BedOutlineState.java` | Incremental bed scan, dye metadata, combined bed geometry and one overlay |
| `TintSettings.java`, `TintControls.java`, `GuiColorMesh.java`, `ColorMath.java` | Saved sky color, drag controls and batched slider gradients |
| `mixin/SkyTintMixin.java` | Selected Overworld sky color in the extracted render state |
| `AimLock.java`, `AimAngles.java`, `AimLockSettings.java`, `mixin/AimLockCameraMixin.java` | Nearest visible head selection, frame-aligned view angles and saved hold binding |
| `QuickJump.java`, `QuickJumpPhysics.java`, `QuickJumpSettings.java`, `mixin/QuickJumpMixin.java` | Saved toggle, bounded vanilla peak calculation and collision-resolved takeoff |
| `QuickFall.java`, `QuickFallState.java`, `mixin/QuickFall*Mixin.java` | Walk-off detection, collision-resolved downward velocity and correction reset |
| `WallClimb.java`, `WallClimbSettings.java`, `mixin/WallClimbMixin.java` | Cached block-wall contact, saved switch and vanilla ladder movement |
| `FreeCam.java`, `FreeCamMotion.java`, `FreeCamSettings.java` | Detached camera state, normalized flight, lifecycle and saved toggle key |
| `mixin/FreeCam*Mixin.java` | Camera/frustum alignment, mouse look, detached hands and interaction guards |
| `OreType.java`, `XraySettings.java`, `Xray.java` | Ore colors/variants, saved filters/radius, loaded-chunk scan and outlines |
| `mixin/XrayChunkMixin.java` | Tracks client chunks and block changes without periodic rescanning |
| `OreSection.java`, `OreMesh.java`, `OreProjection.java`, `OreOutlineState.java` | Compact ore cache, shared edges, frustum rejection and one batched overlay |
| `mixin/GuiRenderStateAccessor.java` | Submits the batched outline element through the normal GUI renderer |
| `PlayerDisplaySettings.java` | Defaults, validation, loading, and saving |
| `PlayerDisplayRenderer.java` | Player selection, projection, clipped boxes and tracers, labels, and name cutoff |
| `mixin/LocalPlayerMixin.java` | Runs Auto Bridge before each local player tick |
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


Version 1.4.0 restored the 1.3.5 placement behavior and added a movement-boundary guard. It introduced search and Free Cam alongside Auto Hit, nametag-colored ESP and optimized Xray. Uncolored players use the normal white nametag fallback.

The P menu uses a dark indigo/violet panel theme with rounded cards, animated switches, and a white dot cursor with a short fading trail. The normal cursor is restored when the menu closes or the window loses focus. Controls and existing feature behavior remain from the 1.3.5-based tools build.

While Auto Bridge is enabled and its hold key is down (or a pending/rejected placement keeps its safety latch active), a final check in vanilla player movement catches residual horizontal momentum that the input-only guard could miss. Support is checked with a 0.15-block inset of the real footprint, reserving an edge margin even at diagonal corners. An unsafe movement is shortened and blocked horizontal momentum is cleared. All outstanding vanilla predictions, solid predictions lacking a separate server block update, and newly confirmed footing during its twelve-tick stability check do not count as support, and short steps back from the safety margin are allowed. This does not force crouching or add rotation packets. Server teleports, removing footing under the player, and a fall already underway remain outside the guarantee.

ESP tracer lines and off-screen markers use the same resolved nametag/team color as the player box. GuiMove also keeps single-player menus from pausing while enabled and focused; world-loading overlays and unfocused windows retain normal behavior.

The Xray performance regression, dense-ore measurements and validation limits are documented in `tests/xray-performance/README.md`. These are cloud test measurements, not a promise of a particular FPS on every PC.

Version 1.4.0 validation is documented in `tests/faster140-live/README.md`, with the local real-client checks and movement packet audit. The installable artifact uses the new `faster-<version>-mc<game>.jar` name; the internal mod ID stays compatible with previous builds. Replace the older mod JAR when installing.

Wall Climb validation for 1.5.0 is documented in `tests/wallclimb-live/README.md`. The local client/server comparison matched a real ladder through ascending, sliding and sneak-holding, with no unexpected server corrections.

BedEsp and QuickFall validation for 1.6.0 is documented in `tests/faster160-live/README.md`. The dense-bed CPU/batching measurement and typed-control checks are in `tests/bedesp-performance/README.md`.

Version 1.6.1 fixes the reported `Can only blur once per frame` crash when a normal screen opens while the P menu is active. Screen/overlay transitions dismiss the custom menu immediately, with a render-time check for changes between game ticks. The native screen retains mouse control and its normal background blur. Real-client regression checks are in `tests/faster161-live/README.md`.

QuickJump validation for 1.7.0 is in `tests/faster170-live/README.md`, including default/boosted jump references, immediate peak timing, ceilings, a one-block ledge, held jumping and interactions with other movement modules.

Version 1.8.1 removes Player Tint and fixes the blue lower-sky band by applying the selected Sky Tint color to the clear background/horizon fog as well as the sky disc. Normal fog distances and underwater/lava colors are retained. The horizontal-view regression, four-direction screenshots and toggle restoration checks are documented in `tests/faster181-live/README.md`. Historical Player Tint tests under `tests/faster180-live` target the older 1.8.0 tag. Sky Tint keeps the same config filename and HSV values so existing selections remain compatible.

Auto Bridge ghost-footing validation for 1.8.2 is in `tests/faster182-live/README.md`: accepted/delayed bridge progress, genuine server-rejected predictions, key release during a delayed rejection, post-ack server rollback and secondary/manual predictions. These are local vanilla server tests. Protection stops at known support; server teleports, removal of already trusted ground beneath the player, or a fall already underway remain outside the guarantee.

Aim Lock validation for 1.9.0 is in `tests/faster190-live/README.md`, with angle/key persistence checks, real-client target switching/occlusion/hold-release tests, and a vanilla rotation-packet audit.

Auto Bridge server-confirmation validation for 1.9.1 is in `tests/faster191-live/README.md`, covering actual rejected placements, delayed confirmations, later rollback, an injected ACK-only visible ghost, and acknowledged secondary ghosts while jumping diagonally. These are controlled local tests, not Hypixel anti-cheat certification.

Adaptive Auto Bridge validation for 1.9.2 is in `tests/faster192-live/README.md`: faster clean/delayed bridge progress, existing ghost/rejection/diagonal-jump regressions, and a real rejection after fast checks were earned. Recent footing remains observed after release; this still cannot guarantee survival when a server removes ground already underneath you.

Aim Lock target-retention validation for 1.9.3 is in `tests/faster193-live/README.md`, including closer competitors, acquisition direction/range, occlusion/invisibility, death, key release and menu/focus/Free Cam pauses.

Aim Lock wall-tracking validation for 1.9.4 is in `tests/faster194-live/README.md`, including continuous head alignment through a real wall while the target moves and visible-only initial acquisition.

[Spawn NPC](spawnnpc/README.md) is a separate optional test-dummy mod with `/spawnnpc` and diamond-block removal. It can be installed independently or alongside Faster.
