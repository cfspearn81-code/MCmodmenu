BedEsp performance and menu-control verification for Faster 1.6.0, on a real Fabric 26.3 client with Mesa/llvmpipe offscreen rendering at 1280×720, GUI scale 2. It connects to the disposable vanilla server used by tests/faster160-live and uses the production scanner/renderer unchanged.

The test injects 2688 complete beds into already-loaded client chunk data: X=-48..47, Y81, Z=-10..45, two blocks per south-facing bed. All 16 dye colors repeat. The player stands at 0.5,81,-20.5; pitch is 8 degrees and yaw oscillates ±8 degrees. Radius is 5000. Unlike the functional test, this dense stress scene is client-only and is discarded with the client; it does not claim server placement/physics validation. Server teleports are only fixture setup. Normal production block-change hooks update the cache.

After warmup, 120 sampled frames selected the maximum 1024 visible logical beds. Every frame used exactly one GUI element. dense-moving.json records:

- Mean overlay extraction/selection: 0.2095 ms; p95 1.4189 ms.
- Mean vertex construction: 0.9062 ms (a separate rendering stage).
- Mean cached BedEsp tick: 0.0024 ms; p95 0.0040 ms.
- Mean extraction allocation: 43254 bytes, including periodic mesh rebuilds while moving the view. Projection/emission has no per-edge object allocation.

Total frame intervals also contain Minecraft's terrain/bed models and software GPU work; these timings are measurements on this cloud machine, not an FPS guarantee for another PC. Scan timing is sampled after warmup, not a measurement of the initial populated-section scan. Initial scan work is bounded separately in the implementation by a soft 0.35 ms budget, at most 32 section checks and four populated sections per tick. A large radius only visits loaded chunks, and steady-state scans do not repeatedly walk unchanged sections.

The fixture also opens the actual menu, types 1 and 5000 into the BedEsp range box, checks that 5001 preserves the previous value and shows a validation error, clicks BedEsp both ways, scrolls Movement and clicks QuickFall both ways. All pass. It waits for the join/loading screen to finish before sending keyboard input, as production menu input intentionally pauses while another screen is open.

To reproduce, compile src/ against the production client classes plus the Minecraft/Fabric runtime dependencies using JDK25. Copy fabric.mod.json/profile.mixins.json into a CLEAN fixture classes directory and jar it. Do not include copied production helper classes. Launch runClient with -Dfabric.addMods=/absolute/path/fixture.jar -Dbench.motion=true in JAVA_TOOL_OPTIONS and --username BridgeLive --width 1280 --height 720. SDL_VIDEODRIVER=offscreen and ALSOFT_DRIVERS=null support cloud rendering. Use the disposable offline OP server at 127.0.0.1:25568 with the floor and force-loaded chunks from the functional fixture. The fixture changes test-client settings, disables other overlays and stops after writing /tmp/bedesp-perf/result.json. It resets BedEsp to off/range200 on completion. None of its bench/ classes are in the release JAR.
