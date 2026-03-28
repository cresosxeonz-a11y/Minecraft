# Instant Leaf Decay - Implementation Explanation

## Mixin Target and Changes

### Why `LeavesBlock.randomTick()`?

The Mixin targets the `randomTick()` method in Minecraft's `LeavesBlock` class because this is the core method responsible for leaf decay mechanics in vanilla Minecraft. Here's why this is the perfect injection point:

1. **Central Decay Logic**: All leaf decay processing happens through random ticks. Minecraft's tick scheduler randomly selects blocks in loaded chunks and calls their `randomTick()` method approximately every 68 game ticks (3.4 seconds) on average.

2. **Natural vs Player-Placed Detection**: The `randomTick()` method has access to the full `BlockState`, which includes the `PERSISTENT` property. This boolean property is `true` for player-placed leaves and `false` for naturally-generated leaves, allowing us to preserve player-placed leaves exactly as specified.

3. **Distance Property Access**: Vanilla Minecraft uses the `DISTANCE` property (values 1-7) to track how far leaves are from the nearest log block. When a log is broken, nearby leaves have their distance recalculated over several ticks. When `DISTANCE` reaches 7, it means the leaf is no longer connected to any log within 6 blocks and should decay.

### What the Mixin Changes

The injection uses `@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)`, which means:

- **Injection Point**: `@At("HEAD")` - executes our code before any vanilla logic runs
- **Cancellable**: Allows us to prevent the original method from executing by calling `ci.cancel()`

**The modified behavior**:

1. **Check if enabled**: If `config.enabled` is false, the mixin does nothing and vanilla behavior proceeds normally.

2. **Respect persistent leaves**: If `PERSISTENT` is true (player-placed), we cancel the vanilla tick immediately, preventing any decay. This is crucial because vanilla would still process these leaves unnecessarily.

3. **Instant decay for disconnected leaves**: If `DISTANCE == 7` (not connected to a log) and the leaf is not persistent:
   - **Instant mode**: Immediately call `dropStacks()` to generate loot (saplings, sticks, apples) and `removeBlock()` to remove the block
   - **Quick mode**: Use a random chance to decay within 1-3 ticks instead of the vanilla 4-7 seconds
   - Cancel vanilla execution to prevent the default timer-based decay

4. **Preserve connected leaves**: If `DISTANCE < 7`, the leaf is still connected to a log, so we cancel the vanilla tick to prevent unnecessary processing.

### Why This Approach Works

- **Preserves Loot Tables**: By calling `LeavesBlock.dropStacks()`, all vanilla loot table logic is preserved. Leaves will still drop saplings, sticks, and apples at their normal rates.

- **Works with All Leaf Types**: Because we're hooking into the base `LeavesBlock` class, this automatically works with all vanilla leaf types (oak, birch, spruce, jungle, acacia, dark oak, mangrove, cherry, azalea, flowering azalea) and any modded leaves that extend `LeavesBlock`.

- **Zero Performance Impact**: By canceling the random tick early for leaves that don't need to decay, we actually reduce server load compared to vanilla, which would process all leaves every random tick.

- **No Block Replacement Hacks**: We're not replacing blocks or bypassing vanilla systems—we're just accelerating the existing decay mechanic, so light updates, block updates to neighbors, and all other vanilla behavior remain intact.

### Technical Flow

```
Random Tick on Leaf Block
    ↓
Mixin intercepts at HEAD
    ↓
Is mod enabled? → NO → Let vanilla run
    ↓ YES
Is leaf persistent? → YES → Cancel (no decay)
    ↓ NO
Is distance == 7? → NO → Cancel (still connected)
    ↓ YES
Drop items via dropStacks()
    ↓
Remove block via removeBlock()
    ↓
Cancel vanilla method
```

This approach is elegant, performant, and maintains full compatibility with vanilla mechanics while achieving instant leaf decay.
