bump: minor

### Added
- New mod: mob/player footfall and landing erosion of layered and layerable blocks, extracted
  from Dynamic Terrain's entity-movement erosion. Requires Layered Slopes only; no Dynamic
  Terrain, Dynamic Vehicles or DIV dependency.
- Independently tunable, bounded, monotonic walking (`WalkMath`) and landing (`LandingMath`)
  probability models; landing captures pre-contact impact velocity, never post-landing zero.
- `erosionEnabled`/`walkErosion`/`landingErosion` config, defaulting off.
