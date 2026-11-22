# Changelog

## 1.1.0

- Add `/spawnnpc kb`, which enables hit knockback for that individual NPC. Keep `/spawnnpc` stationary and allow both types at once.
- Use native knockback away from the attacker and native player travel for gravity, drag and terrain collisions. Continue hurt animation/cooldown and damage-free testing. Preserve diamond-block removal for either type, including during flight.
- Retain local-only commands/interaction handling, skin/equipment copying, cleanup, ownership guards and normal server-entity behavior. Add real-client regressions for idle/normal behavior, mixed modes, launch/landing, repeat and diagonal hits, wall collision and moving-target compatibility with Faster.

## 1.0.0

- Initial standalone client mod: `/spawnnpc` creates a stationary copy of the player's skin and equipped items. Mainhand diamond-block left clicks remove owned dummies; ordinary hits give local hurt feedback. Include multi-NPC placement, world/player cleanup and packet isolation.
