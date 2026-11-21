# Menu visual validation

Minecraft 26.3 with Fabric Loader 0.20.1, SDL offscreen rendering, window 1280x900 and GUI scale 2, connected to a local vanilla server. Actual screenshots for Overview, Movement and Players were captured and visually inspected. Numeric setting panels and controls render with correct layering; category clicks select the corresponding views. Cursor dot/trail are drawn above menu content.

The test confirmed that the SDL native cursor is visible after focus loss, hidden again when the menu regains focus, and restored after closing the custom menu and releasing the mouse. The client completed normally. Nine deterministic cursor tests passed: reset, interpolation, point following, time-based fade, no stationary accumulation, expiration, bounded count, no teleport streak, and reopening.

All original classes from the previous tools build are byte-identical except ClientMenu. Added classes implement menu painting, cursor presentation and bounded trail sampling. No gameplay, ESP, Auto Hit or settings classes were changed. Screenshots are retained at /workspace/previews/menu-1.3.5-ui.1. The fixture is separate from the shipped mod and only opens/clicks menu categories, moves the mouse and captures screenshots; it does not replace rendering implementation.
