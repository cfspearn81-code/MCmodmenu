# Spawn NPC

A **separate client mod** for Minecraft Java **26.3**, Fabric Loader **0.20.1+**, and Java **25**. It works by itself or alongside Faster; it does not need Fabric API or a server plugin.

[Download Spawn NPC 1.0.0](https://github.com/cfspearn81-code/MCmodmenu/raw/refs/tags/spawnnpc-v1.0.0/spawnnpc/downloads/spawnnpc-1.0.0-mc26.3.jar)

Put the JAR in your instance's `mods` folder. Keep Faster installed as well if you want its modules; this JAR does not replace it.

- Type **`/spawnnpc`** in chat and press Enter. A stationary player-shaped dummy appears on clear floor nearby, usually about 2.5 blocks ahead.
- The dummy uses your skin, visible skin layers, main arm and a copy of your currently equipped armor/held items. Its name ends in `_NPC` plus a number. Its player entity type lets client player-targeting/rendering modules recognize it.
- Ordinary left-click hits show a hurt animation and reset your normal attack cooldown, without killing or moving the dummy.
- To despawn one, hold a **diamond block in your main hand** and **left-click/hit that NPC** within normal reach. The block is not consumed. An offhand diamond block alone does not remove it.
- Repeating `/spawnnpc` creates separate dummies. Spawn placement avoids solid blocks and existing dummies; face another open area if no space is available. There is a limit of 64 active dummies.
- Dummies are local to your client: other players and the server do not see them. Local attacks/interactions are handled locally. This supports visual/aim/hit-input testing, not server damage or anti-cheat testing.
- Dummies clear when you change dimensions/worlds, disconnect or respawn. They are not saved with the world.

Commands other than the exact `/spawnnpc` command still go to the server normally. Extra arguments show usage instead of spawning a dummy.

From the parent repository root, build only this mod with:

```sh
./gradlew -p spawnnpc build
```

The independent installable artifact is `spawnnpc/build/libs/spawnnpc-1.0.0-mc26.3.jar`. The source project has its own Gradle settings, source roots, mod ID and mixins, with no Faster runtime dependency. The parent Gradle wrapper is only a build convenience. Tests live in `tests/live/` and are excluded from the release JAR.
