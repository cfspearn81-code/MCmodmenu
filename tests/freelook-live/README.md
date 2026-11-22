# Free Look live regression

The fixture runs the Faster client against a disposable vanilla Minecraft 26.3 server on `127.0.0.1:25568` as the offline operator `BridgeLive`. Test input supplies physical key state and feeds deltas through Minecraft's native mouse turn path. It uses the actual gameplay tick, camera renderer, client movement and outgoing movement packets.

Coverage includes the Search and Visuals cards, Sky Tint access after scrolling, hold key rebinding, four-block orbit through a stone wall, independent camera yaw and pitch, forward walking along the original body heading, release from first- and third-person views, Free Cam priority, Aim Lock suspension, disable, menu and focus loss. A temporary local player entity supplies the Aim Lock target.

Build the client on JDK 25 and compile `src/` with the built client classes and Minecraft/Fabric runtime classpath. Copy the two fixture JSON files into the fixture classes directory and package a fixture JAR. Launch `runClient -x downloadAssets` with `-Dfabric.addMods=/absolute/path/to/fixture.jar` in `JAVA_TOOL_OPTIONS` and `--username BridgeLive --width 854 --height 480` as the client arguments. Set `SDL_VIDEODRIVER=offscreen` and `ALSOFT_DRIVERS=null` to run without a desktop display.

The fixture prepares its test area with operator commands and uses a disposable client configuration. The run writes results and screenshots to `/tmp/freelook-live/`. The Faster 1.10.0 run passed all 11 checks and observed 20 movement packets during the orbit, with player rotation preserved. See `results.txt` and `run-summary.txt`. The client stops after it prints `FREELOOK ALL CHECKS COMPLETE`. The fixture is outside the production source sets.
