bump: patch

### Fixed
- `LandingErosion.onPreTick`'s blocks/tick-to-m/s conversion is now a named production adapter
  (`LandingErosion.impactSpeedMetersPerSecond`) that the real-runtime regression GameTest calls
  directly, instead of recomputing its own separate copy of the same expression -- deleting or
  breaking the conversion now actually fails the test.
- README: corrected the migration section's false claim that the MINECRAFT-500 Smooth Slopes
  split set a "no automatic config migration" precedent -- that split's own `worldgenSmoothing`
  key *does* auto-migrate. No behavior change: this mod still has no automatic migration for
  `entityErosion`, for the stated reason that a single legacy boolean doesn't map onto this mod's
  two independently-tunable keys.
