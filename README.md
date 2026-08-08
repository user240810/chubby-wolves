# Chubby Wolves

A Fabric mod: tamed wolves render bigger/rounder ("chubby") and wear a floating hat.

## Why this looks the way it does

You're targeting Minecraft **1.21.11**, which is about as bleeding-edge as it gets —
it's the very last *obfuscated* release before Mojang switches to shipping
unobfuscated code, and it also landed a real rewrite of how mob rendering works
internally (mob feature layers now "submit" to a render-command collector instead
of drawing directly). That rewrite is genuinely new and thinly documented, so I
made two deliberate choices to keep this reliable:

1. **Chubby effect** — a mixin into `WolfRenderer#extractRenderState`, which
   multiplies `WolfRenderState.scale`. I checked this method/field by name against
   the real 1.21.11 Mojang mappings, so it should be accurate.
2. **Hat** — rather than write a custom render layer against the brand-new
   rendering internals (high risk of being subtly wrong in ways I can't verify
   without a compiler), the hat is a plain `ItemDisplay` entity that the mod
   spawns and glues above each tamed wolf's head every tick. Display entities
   are ordinary, stable, well-documented game objects — completely unaffected
   by the renderer rewrite.

## Before you build

Dependency versions for 1.21.11 are moving fast. Open **`gradle.properties`** and:

1. Go to <https://fabricmc.net/develop/>, select Minecraft `1.21.11`.
2. Copy the exact `loader_version`, `loom_version`, and `fabric_version` shown
   there into `gradle.properties` (I've left placeholders / best guesses).

Then:

```bash
./gradlew build
```

The built jar lands in `build/libs/`. Drop it in your `.minecraft/mods` folder
alongside a matching **Fabric API** jar and the **Fabric Loader** for 1.21.11.

## If it doesn't compile out of the box

Given how new 1.21.11 is, there's a real chance a class or method got renamed
in a patch after my research cutoff. If that happens:

- The error will be a plain "cannot find symbol" pointing at the exact line.
- In IntelliJ/VS Code, right-click the nearby class (e.g. `WolfRenderer`,
  `WolfRenderState`, `Display.ItemDisplay`) → "Go to Declaration" to see its
  current real method/field names, then adjust.
- `./gradlew genSources` will decompile Minecraft with the mappings you're
  using, which makes browsing the real source much easier.

These are usually one-line fixes, not structural problems — the *design* of
the mod (mixin into `extractRenderState`, display-entity hat) should hold up
even if an exact name shifts slightly.

## Customizing

- **Hat item** — change `hatItem()` in `ChubbyWolves.java` (e.g.
  `Items.LEATHER_HORSE_ARMOR`, `Items.PLAYER_HEAD`, or your own custom item).
- **Hat size / height** — `HAT_SCALE` and `HAT_HEIGHT_OFFSET` in the same file.
- **How chubby** — `CHUBBY_SCALE` in `WolfRendererMixin.java` (1.0 = normal,
  the default 1.45 is a fairly round wolf).

## Known limitations

- Hats are tracked in memory only, so they respawn automatically a moment
  after a server/world restart rather than persisting across saves.
- The chubby effect is purely visual — hitboxes stay wolf-sized, so a chubby
  wolf won't get stuck in doorways any more than a normal one would.
