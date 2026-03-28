# Instant Leaf Decay

A Fabric mod for Minecraft 1.21.1 that makes leaves decay instantly when they're no longer connected to a log block.

## Features

- **Instant decay**: Leaves decay within 1-2 ticks instead of the vanilla 4-7 second timer
- **Works with all leaf types**: Compatible with oak, birch, spruce, jungle, acacia, dark oak, mangrove, cherry, azalea, and flowering azalea leaves
- **Mod compatibility**: Works with modded leaves that extend vanilla LeavesBlock
- **Player-placed leaves are safe**: Persistent leaves (placed by players) are not affected
- **Preserves vanilla mechanics**: Leaves still drop saplings, sticks, and apples according to their loot tables
- **Zero performance impact**: No overhead when leaves aren't decaying

## Configuration

The mod creates a configuration file at `config/instantleafdecay.json` with the following options:

```json
{
  "enabled": true,
  "instant": true
}
```

- **enabled** (boolean, default: `true`): Master toggle for the entire mod
- **instant** (boolean, default: `true`): If false, leaves decay in 1-3 ticks instead of instantly

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download the mod JAR file
3. Place it in your `.minecraft/mods` folder
4. Launch Minecraft

## Building

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## How it Works

The mod uses a Mixin to intercept the `randomTick` method in `LeavesBlock`. When a leaf block receives a random tick:

1. Check if the mod is enabled
2. Check if the leaf is persistent (player-placed)
3. Check the distance value (7 = not connected to log)
4. If distance is 7 and not persistent, immediately drop items and remove the block
5. Otherwise, prevent vanilla decay logic from running

## License

MIT License - See LICENSE file for details

## Compatibility

- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Java Version**: 21+

## Technical Details

### File Structure

```
instant-leaf-decay/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── LICENSE
├── README.md
└── src/main/
    ├── java/com/instantleafdecay/
    │   ├── InstantLeafDecay.java          # Main mod class
    │   ├── config/
    │   │   └── ModConfig.java             # Configuration handler
    │   └── mixin/
    │       └── LeavesBlockMixin.java      # Mixin for leaf decay logic
    └── resources/
        ├── fabric.mod.json                # Mod metadata
        ├── instantleafdecay.mixins.json   # Mixin configuration
        └── assets/instantleafdecay/
            └── icon.png                   # Mod icon
```

### Why This Mixin Works

The Mixin targets `LeavesBlock.randomTick()` because this is the method Minecraft calls to handle leaf decay. In vanilla Minecraft, leaves use a multi-tick decay system where the DISTANCE property gradually increases over several random ticks. When DISTANCE reaches 7 (max value), the leaf decays after additional random ticks.

By injecting at the HEAD of this method with `cancellable = true`, we can:
1. Read the current DISTANCE value to determine if the leaf should decay
2. Immediately call `dropStacks()` and `removeBlock()` when DISTANCE = 7, bypassing the vanilla timer
3. Cancel the original method execution to prevent vanilla decay logic from interfering
4. Respect the PERSISTENT property to avoid affecting player-placed leaves

This approach preserves all vanilla behavior (loot tables, block updates, etc.) while only modifying the timing of the decay.
