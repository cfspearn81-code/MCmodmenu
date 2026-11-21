# Changelog

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
