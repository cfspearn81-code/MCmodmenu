# Edge guard regression results

Minecraft 26.3 Fabric client connected to a separate local vanilla dedicated server. Survival mode, 5x5 stone platform over an empty world, bridge key held.

Before fix: diagonal movement without available blocks fell below the platform after 30 ticks (Y=80.2315 from Y=81). This reproduced on the current ui.1 input-only guard.

After fix: all 15 scenarios ran for 120 ticks without falling below the starting platform or breaking the four-tick placement interval. Twelve approach cases deliberately did not aim at a valid placement, so safe stopping was expected. They are not counted as successful bridge progress. The three placement cases actively aimed at the supporting faces and required real forward progress and block consumption.

| Scenario | Travel | Blocks used | Attempts | Minimum Y |
|---|---:|---:|---:|---:|
| straight | 2.70 | 0 | 0 | 81.00 |
| diagonal | 3.78 | 0 | 0 | 81.00 |
| angle22 | 2.88 | 0 | 0 | 81.00 |
| angle67 | 2.91 | 0 | 0 | 81.00 |
| angle135 | 3.81 | 0 | 0 | 81.00 |
| angle225 | 3.81 | 0 | 0 | 81.00 |
| zigzag | 3.31 | 0 | 0 | 81.00 |
| jump | 2.70 | 0 | 0 | 81.00 |
| sprint | 2.66 | 0 | 0 | 81.00 |
| delay | 2.70 | 0 | 0 | 81.00 |
| reject | 2.70 | 0 | 0 | 81.00 |
| empty | 2.70 | 0 | 0 | 81.00 |
| place | 18.77 | 17 | 17 | 81.00 |
| place-delay | 9.13 | 7 | 7 | 81.00 |
| place-jump | 12.25 | 25 | 25 | 81.00 |

Place-delay defers actual vanilla acknowledgements by ten client ticks. Place-jump holds jump while placing. The fixture drives normal player controls and camera aim without substituting production movement or collision code. The test-only client/server mode mismatch used in the reject setup did not generate a placement attempt because its crosshair was intentionally horizontal.

77 independent swept-boundary checks passed, covering 72 angles plus retreat, gap traversal and pending-footing behavior. These are local vanilla tests, not validation of third-party anti-cheat or server teleports. The protection applies while the configured bridge key is held and Auto Bridge is enabled; removing the ground or a fall already underway remains outside the guarantee.
