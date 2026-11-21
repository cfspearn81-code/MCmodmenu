Faster 1.6.0 verification on a real Minecraft 26.3/Fabric client connected to a disposable vanilla localhost server (127.0.0.1:25568), offline OP BridgeLive. Production scanning, rendering, input, collision and movement code runs unchanged. The fixture supplies held keys/focus, observes server position corrections, and sets up the arena through normal OP commands. Resistance V protects this disposable actor from dying during repeated 24-block drops; it is a fixture effect, never applied by the mod. The mod retains normal fall damage.

results.txt records the completed run:

- All 16 bed colors render through a solid stone wall. Seventeen logical beds include an east-facing pair crossing X15/16, so chunk boundaries do not split or duplicate the outline.
- Range 1 hides the distant beds; range 5000 shows the loaded beds. Removing both halves through actual server commands changes the cached logical count from 17 to 16.
- Normal 24-block walk-off: 26 airborne ticks, minimum velocity -1.6481. QuickFall: 3 airborne ticks, minimum velocity -8, same floor landing at Y81.
- Jumping off with QuickFall enabled retains the jump arc (peak Y106.2522, 33 airborne ticks, no -8 acceleration).
- QuickFall lands on bottom slabs at exactly Y81.5, without passing through them.
- Wall Climb continues rising with QuickFall enabled; Free Cam controls leave the body in place.
- All measured fall cases have zero unexpected position corrections. This is local vanilla server validation, not a test of public servers or their custom movement rules.

The three screenshots were visually inspected: bed colors through the wall, BedEsp range/settings, and the scrolled Movement page containing QuickFall. Copies live outside the repository at /workspace/previews/faster-1.6.0/.

To reproduce, start an offline vanilla 26.3 server bound to the address above with OP BridgeLive, and force-load the arena. This fixture builds its bed/fall scenes. It also uses a stone wall at X8,Y81..120,Z3 over a stone floor Y80 (from the Wall Climb fixture); restore that wall if another test changed it. The fixture uses normal OP teleport/fill/setblock/effect commands in this disposable world, modifies test-client settings, and stops after FEATURE ALL CHECKS COMPLETE. Its initial safe spawn is 8.5,81,0.5; use a floor there.

Compile src/ using JDK25 against build/classes/java/client, build/classes/java/main and the Minecraft/Fabric runtime dependencies. Copy fabric.mod.json and check.mixins.json into a clean fixture class directory and jar it. Launch runClient with -Dfabric.addMods=/absolute/path/fixture.jar in JAVA_TOOL_OPTIONS, --username BridgeLive --width 1280 --height 720; SDL_VIDEODRIVER=offscreen and ALSOFT_DRIVERS=null support cloud rendering. Results go to /tmp/faster160-live/results.txt and screenshots/. No check/ fixture classes enter the production JAR.

Standalone tests/QuickFallStateTest.java checks walk-off/landing, stationary support removal, jump descent, brief ledge contact versus sustained climbing, sneaking, correction resets and upward bounces. BedMeshTest verifies combined bed dimensions, all color flag bits, coordinate extremes, shared edges, finite clipped vertices and dense section lookup/removal. BedQuickSettingsTest uses tests/support stubs and an isolated -Dtest.config directory in separate JVMs for write/reload. Existing OreMeshTest and OreProjectionTest pass after the shared rendering/cache helper changes.
