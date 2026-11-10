# layered-slope-dynamic-paths

Mob and player footfall/landing erosion of layered and layerable blocks, extracted from Dynamic
Terrain, for NeoForge 1.21.1.

NeoForge 1.21.1, Java 21. Mod ID: `layeredslopedynamicpaths`. MIT licensed.

Depends on [Layered Slopes](https://github.com/brooswit-minecraft/layered-slopes) (blocks only;
no Dynamic Terrain, Dynamic Vehicles or DIV dependency).

## What it does

Every tick, living entities near a player are checked for two independent erosion sources:

- **Walking**: an on-ground entity moving faster than `walkMinSpeed` (0.5 m/s by default) reports its
  mass x ground speed as a bounded, monotonic chance of removing one layer from the block under
  its feet (`entity.getOnPos()`, so partial-height layered contact resolves correctly).
- **Landing**: an entity's PRE-contact fall speed (sampled every tick before movement resolves,
  never the already-zeroed post-landing velocity) is captured the instant it next touches ground,
  and reports mass x impact speed as its own independently-tuned bounded, monotonic chance.

Both sources call the same `LayerErosion.erode`: a full "layerable" block (dirt, stone, grass,
...; see `PathMaterials`) converts to its registered Layered Slopes variant one layer down; an
already-layered block loses exactly one layer, or vanishes to air at its last layer. A successful
erosion of grass always settles it to plain (layered) dirt -- footfall disturbs, it never
preserves a grass top.

Erosion never redistributes material to a neighbouring block (unlike Dynamic Terrain's own
erosion, which this mod does not depend on): a removed layer simply disappears. Server-authoritative,
deterministic-testable via an injected `RandomSource`, and bounded to a per-player radius scan
with explicit de-duplication so an entity visible to more than one nearby player is only
considered once per tick.

Config (`erosionEnabled`, `walkErosion`, `landingErosion`) defaults erosion off, matching Dynamic
Terrain's own terrain-altering defaults, so existing worlds are unaffected until explicitly opted in.

Each probability model's rate/reference/cap is a separate, independently tunable server-config
value -- retuning one model never affects the other:
- Walking (`WalkMath`): `walkBaseChance`, `walkReferenceMassKg`, `walkReferenceSpeed`,
  `walkMinSpeed`, `walkMaxChance`.
- Landing (`LandingMath`): `landingBaseChance`, `landingReferenceMassKg`,
  `landingReferenceImpactSpeed`, `landingMinImpactSpeed`, `landingMaxChance`.

## Migration from Dynamic Terrain

Dynamic Terrain's own entity-movement erosion (`EntityErosion`/`EntityMath`) stays in place for
now; this mod does not remove or replace it. An upgraded world running both Dynamic Terrain and
this mod will double-erode under entity footfall until Dynamic Terrain's own
`EntityErosion`/`EntityMath` are removed in a follow-up Dynamic Terrain change (coordinate with
that ticket before disabling `entityErosion` there). Installing this mod alone (no Dynamic
Terrain) requires only Layered Slopes and behaves identically to the extracted behavior.

## Building

Requires a JDK 21 with `javac` on `JAVA_HOME`.

```sh
./gradlew build
```

Run the GameTest suite (boots a real NeoForge server with this mod and Layered Slopes installed):

```sh
./gradlew runGameTestServer
```

## Releasing

Every PR that touches `src/` must add exactly one fragment under
`changelog.d/` (see `changelog.d/README.md`). Pushing to `main` with new
fragments tags a GitHub release automatically; nothing is committed back by
hand.
