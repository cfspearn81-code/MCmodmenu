# Changelog

## 1.11.0

- Auto Bridge finds a reachable attachment face along the player’s movement path, including diagonal steps, without turning the player’s view.
- Require a separate server block update and prediction reconciliation for every placement. Use a twelve-tick stability check for each block to catch removals that arrived after the former one-tick check.
- Keep the edge guard active on key release until the player is at rest on trusted ground. Clip approaching steps while preserving enough clearance for legal side-face placement.

## 1.10.1

- Remove Speed and its walking and sprinting multiplier. All five remaining Movement modules fit on one page.

## 1.10.0

- Add Free Look to Visuals and Search. Hold Left Alt by default to orbit the player with normal mouse sensitivity and inversion while maintaining the player’s movement direction.
- Free Look uses the regular third-person camera distance and passes through blocks. Release the hold key to return to your selected perspective. The module switch and binding are saved.
- Free Cam takes priority. Aim Lock pauses during the orbit and resumes when the key is released.

## 1.9.4

- Continue Aim Lock head tracking through walls once a target is acquired. Preserve sticky target identity, release/death handling, 32-block/forward initial selection, and initial block line of sight.
- Keep other pause conditions and all other modules unchanged. Add live regressions for moving heads behind a solid wall and visible-only initial acquisition.

## 1.9.3

- Keep the same Aim Lock target while its key is held instead of selecting a new nearest player every tick. A closer competitor cannot steal a living lock; release clears it, and target death permits a new selection.
- Limit initial acquisition to visible players within 32 blocks and a 90-degree cone ahead. Those limits only apply to acquisition, preserving an already chosen living target if it moves beyond them.
- Pause aiming through walls, invisibility, spectator state, menus, focus loss and Free Cam without forgetting the held target. Temporary entity removal also pauses; a replacement with the same UUID resumes that player. Disabling, binding changes, local death/spectating and world/player changes reset the lock.
- Update Combat help and add real-client target-retention regressions. Preserve Auto Bridge's faster adaptive checks and all other modules.

## 1.9.2

- Replace the repeated latency-scaled 10–40 tick Auto Bridge wait with adaptive confirmation: begin at ten ticks, then use one tick after three reconciled server-confirmed placements remain stable for twenty additional ticks. Still require separate authoritative block-state feedback and native prediction reconciliation for every block.
- Monitor approved footing for 100 ticks instead of discarding its history as soon as it is usable. A server update revoking recent solid confirmation immediately invalidates trust, restores conservative checks, retains the safety latch and applies existing rejection backoff. Duplicate air feedback cannot multiply rejection events.
- After failure, require 100 recovery ticks and three fresh sustained confirmations before returning to fast checks. Old observations cannot be counted again, and confidence resets on world/player changes. Preserve placement packet budgets, missing-confirmation stops and diagonal/jump edge protection.
- Add adaptive-policy unit coverage and local real-client regressions including a server rejection after fast mode is earned. Normal local bridge progress improves from about 13 to 23 blocks in the same measured interval; this is a controlled comparison, not a public-server speed guarantee.

## 1.9.1

- Require an authoritative server block-state update in addition to vanilla reconciliation before trusting new Auto Bridge footing. Acknowledgement or elapsed time alone cannot approve a visible ghost. Apply the same quarantine to manual/secondary predicted solid blocks.
- Wait at least ten ticks after acknowledgement and server block confirmation, increasing the stability interval for measured latency up to forty ticks. Server rejection revokes confirmation; missing confirmation keeps the edge blocked without additional interaction packets. Show “Unconfirmed: stopped safely” after prolonged waiting.
- Keep diagonal/jump swept-edge checks, release-during-movement protection, placement budgets and rejection backoff. Clear quarantine metadata after safe reconciliation, chunk unload or world/player changes.
- Add authoritative-footing unit coverage and local client/server regressions for rejected, delayed, ACK-only ghost and late-rollback scenarios.

## 1.9.0

- Add Aim Lock in Combat/Search with a saved default-off switch and configurable hold key (R by default). Aim the real player view at the nearest unobstructed player head while held and stop on release.
- Track changing/crouching heads, use shortest yaw turns and skip self, invisible-to-viewer, dead/removed and spectator targets. Pause in menus, while unfocused, during Free Cam and on death/spectating/world changes.
- Cache nearest selection per player tick and align interpolated head tracking before camera update. Use only normal vanilla rotation packets; no automatic attacks are added. Preserve existing Bridge/Free Cam binding behavior and all other modules.

## 1.8.2

- Exclude every outstanding vanilla block prediction from Auto Bridge footing checks, including manual and secondary positions beyond its expected placement target.
- Use the normal BlockPlaceContext to identify replaceable-face placement targets. Wait for prediction reconciliation and four additional stable ticks before trusting newly placed footing; treat early server rollback as a rejection.
- Keep the edge guard active through a pending interaction and rejected placement even if the hold key is released while movement continues. Release the bridge key and stop moving to reset the rejection safety latch, or disable the module.
- Preserve the four-tick attempt budget, no-resend waiting and rejected-placement backoff; count success only after footing stabilizes. No custom movement/rotation packets or server-position overrides.

## 1.8.1

- Fix the blue lower-sky/horizon band by matching the clear background and distant fog color to Sky Tint. Keep native fog distances and fluid/dimension-specific fog, and restore native colors immediately when disabled.
- Remove Player Tint completely: menu/search entry, hand/body render hooks, custom line pipeline, model accessors, wheel and player-color settings access. Old player-tint config files are left unused.
- Fit all five remaining Visuals switches on one page, retaining Sky hue/saturation/brightness controls and existing saved sky settings.

## 1.8.0

- Add Player Tint in Visuals/Search: Xray-style wireframe edges on visible first-person hands and the local third-person body, following animated model parts. Add a full-spectrum hue/saturation color wheel, brightness bar, swatch and hex readout.
- Add Sky Tint with hue, saturation and brightness sliders for the Overworld sky and matching sunrise/sunset glow. Preserve native sun/moon/stars, terrain and dimensions without a normal sky.
- Save each toggle/color independently. Preview drags in memory and save on release, menu close or screen transition. Batch the wheel/gradients and snapshot player edges once per submission.
- Add Tint/Sky settings tabs and a scrollable six-module Visuals overview while retaining the blur crash fix and existing tools.

## 1.7.0

- Add QuickJump with a saved default-off toggle in Search and Movement. Compress a grounded jump to its normal peak on the takeoff tick, using actual jump impulse, gravity and air drag.
- Use normal collision sweeps to stop at ceilings; preserve horizontal sprint impulse, normal falling and grounded jump cooldown/repeats. Keep swimming, climbing, flight, riding, levitation and Free Cam unchanged. Unsupported/extreme physics retain their original gradual ascent.
- Scroll Movement to reach the sixth movement module; preserve the Visuals grouping and screen-transition blur fix.

## 1.6.1

- Fix `Can only blur once per frame` when pause/inventory screens open while the P menu is active. Dismiss on screen/overlay transitions and check again before rendering; leave the new screen in control of the mouse.
- Group Xray, Player ESP, BedEsp and Free Cam under Visuals. Keep their switches together and provide Player ESP/Xray/BedEsp settings sub-tabs, retaining ranges, ore filters and the Free Cam keybinding.
- Remove the old separate visual categories and move Free Cam out of Movement, allowing all five Movement modules to fit without scrolling.

## 1.6.0

- Add BedEsp with all 16 bed colors, one combined bed-height outline per bed, and a saved 1–5000 block range (default 200).
- Reuse optimized incremental section scans, block/chunk events, compact metadata, shared-edge deduplication and a single batched overlay; cap drawing at 1024 visible beds.
- Add QuickFall with a saved default-off switch: walking off an edge accelerates downward using normal block collisions. Preserve jumps, climbing, Free Cam and normal fall damage. Reset on server corrections and world/player changes.
- Add a BedEsp settings page and scrollable Movement tools so QuickFall remains accessible.

## 1.5.0

- Add Wall Climb in Search and Movement, with a saved default-off switch.
- Treat contacted block walls as climbable and use vanilla ladder physics for ascending, controlled sliding and sneak-holding. No custom jump impulses or packet loop.
- Allow stepping onto a ledge and backing away into ordinary falling; exclude floors/ceilings and pause synthetic wall climbing during Free Cam, flight, riding and swimming.
- Pause Auto Bridge while Wall Climb is attached to a wall, so its safety guard does not block the climb.
- Keep real ladders/vines intact and clear wall-contact cache references on world/player changes or disable.

## 1.4.1

- Display Player ESP distances and its range setting in `M` (meters), including off-screen labels, using the same numeric distances.

## 1.4.0

- Rename the mod display name to Faster and the installable file to `faster-1.4.0-mc26.3.jar`.
- Open the P menu to a focused module search. Search by name/description, scroll through all seven modules, use Ctrl+A/Ctrl+V to edit, and Esc to close. Typing does not move the player.
- Add Free Cam, controlled by a menu switch or a configurable toggle key (F6 by default). Movement keys fly the camera, Space/Shift move vertically, and sprint flies faster. Mouse look moves the camera independently; the real player keeps normal physics and server updates. Camera control pauses in menus and when unfocused; death/disconnection/world changes return to normal view.
- Pause building/combat actions while Free Cam is active, preserve the original camera perspective, and make cached Xray selection follow a translating detached camera.
- Preserve existing settings and the optimized Xray/1.3.5-based movement behavior. Free Cam's binding saves in `faster-freecam.properties`; active camera mode does not persist across sessions.

## 1.3.5-xray.2

- Optimize Xray with a batched renderer, shared-edge deduplication, compact event-driven caches and bounded incremental scan work.
