# Faster Movement

A client-side Fabric mod for Minecraft Java Edition 26.3. Press **P** in-game to open its menu. Movement keys work while the menu is open, and the cursor is available for clicking its controls.

[Download the current JAR](https://faster-movement-mc263.cfspearn81.chatgpt.site)

## Features and controls

The **All tools** tab contains the three switches.

- **Speed** changes walking and sprinting speed. It starts off at launch.
- **CraftingMovement** permits movement while a crafting table is open. It starts off. Movement input is suspended while the recipe search field captures typing.
- **Player ESP** draws green wireframe boxes, red tracers, white player names, and green block distances. Off-screen players get direction indicators. It starts on.

Choose **Movement** to edit the speed multiplier. The default **1.5x** is a 50% increase over normal speed. Use the plus and minus controls or click the number, type a multiplier of at least 1, and press Enter.

Choose **Players** to edit the ESP range and name limit.

- The default range is **500 blocks**. ESP displays players whose positions the server currently sends to the client.
- The default name limit is **10**. When more than 10 players are displayed, names hide and boxes, tracers, and distances continue to render. Raising the limit changes that cutoff. Setting it to zero hides names whenever any player is displayed.
- Range changes use 25-block steps. Name-limit changes use one-player steps. Click either number to type an exact whole number, then press Enter.

Press **P**, **Esc**, or the close control to close the menu. Settings are saved to `config/faster-movement.properties` in the Minecraft instance.

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

The installable output is `build/libs/faster-movement-1.3.0-mc26.3.jar`. The sources JAR in that directory is for development.

Install Fabric Loader **0.20.1 or later** for Minecraft **26.3**, then put the installable JAR in the instance's `mods` folder.

To launch the development client

```sh
./gradlew runClient
```

## Continuing development

Java sources live in `src/client/java/net/fastersprint`. The mod metadata and mixin registration live in `src/main/resources`.

| File | Responsibility |
| --- | --- |
| `ClientMenu.java` | P menu, category layout, feature switches, and numeric controls |
| `PlayerDisplaySettings.java` | Defaults, validation, loading, and saving |
| `PlayerDisplayRenderer.java` | Player selection, projection, clipped boxes and tracers, labels, and name cutoff |
| `mixin/LocalPlayerMixin.java` | Speed attribute modifier and its configured amount |
| `mixin/CraftingMovementInputMixin.java` | Configured movement keys while a crafting table is open |
| `mixin/PlayerDisplayNameMixin.java` | Suppresses overlapping vanilla names while ESP renders its own labels |
| `mixin/PlayerDisplayHudMixin.java` | ESP and menu rendering |
| `mixin/PlayerDisplayInputMixin.java` | P and numeric editing while allowing movement keys |
| `mixin/ClientMenuMouseMixin.java` | Menu clicks and mouse capture |
| `mixin/ClientMenuLifecycleMixin.java` | Closes menu state when gameplay changes to another screen |

Add a feature's menu switch through `ClientMenu.Tool`, connect its enabled state and behavior, and register any additional mixins in `fastersprint.mixins.json`. Keep settings validation and persistence in `PlayerDisplaySettings`.

The project uses Minecraft's unobfuscated 26.3 class names. Dependencies and the release version are pinned in `gradle.properties` and `build.gradle`. Change `mod_version` in `gradle.properties` for a new release. The build expands that version into `fabric.mod.json`.

## Verification

Version 1.3.0 was tested with real 26.3 Fabric clients connected to an isolated vanilla 26.3 server.

- Walking continued while opening the menu, clicking switches, and closing it.
- A 2.0x multiplier produced approximately 8.63 blocks per second walking, compared with 4.32 with Speed off.
- Green boxes, red tracers, on-screen labels, and off-screen/behind-camera indicators rendered with a real second player.
- A client-only test fixture verified names at exactly 10 displayed players and distance-only labels at 11. Raising the limit to 11 restored the names.
- CraftingMovement was tested with a real crafting table, physical movement keys, sprint, jump, and key release. Ordinary inventory behavior was checked separately.
- Saved settings were verified in a fresh process.

Test fixture code and downloaded Minecraft binaries are outside this repository.

