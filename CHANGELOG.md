# Changelog

## 1.4.0

- Rename the mod display name to Faster and the installable file to `faster-1.4.0-mc26.3.jar`.
- Open the P menu to a focused module search. Search by name/description, scroll through all seven modules, use Ctrl+A/Ctrl+V to edit, and Esc to close. Typing does not move the player.
- Add Free Cam, controlled by a menu switch or a configurable toggle key (F6 by default). Movement keys fly the camera, Space/Shift move vertically, and sprint flies faster. Mouse look moves the camera independently; the real player keeps normal physics and server updates. Camera control pauses in menus and when unfocused; death/disconnection/world changes return to normal view.
- Pause building/combat actions while Free Cam is active, preserve the original camera perspective, and make cached Xray selection follow a translating detached camera.
- Preserve existing settings and the optimized Xray/1.3.5-based movement behavior. Free Cam's binding saves in `faster-freecam.properties`; active camera mode does not persist across sessions.

## 1.3.5-xray.2

- Optimize Xray with a batched renderer, shared-edge deduplication, compact event-driven caches and bounded incremental scan work.
