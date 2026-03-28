package com.instantleafdecay.mixin;

import com.instantleafdecay.InstantLeafDecay;
import com.instantleafdecay.config.ModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {
    
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        ModConfig config = InstantLeafDecay.getConfig();
        
        // If mod is disabled, let vanilla behavior run
        if (!config.enabled) {
            return;
        }
        
        // Check if leaf is persistent (player-placed)
        // Persistent leaves should not decay
        if (state.get(LeavesBlock.PERSISTENT)) {
            ci.cancel();
            return;
        }
        
        // Check the distance property
        // Distance 7 means not connected to a log
        // Distance 1-6 means connected to a log at that distance
        int distance = state.get(LeavesBlock.DISTANCE);
        
        if (distance == 7) {
            // Leaf should decay
            if (config.instant) {
                // Instant decay - drop items and remove block immediately
                LeavesBlock.dropStacks(state, world, pos, null, null, null);
                world.removeBlock(pos, false);
                ci.cancel();
            } else {
                // Quick decay (1-3 ticks) - use a small random chance
                // Since random ticks happen every ~68 game ticks on average,
                // we use a high probability to make it happen within 1-3 ticks
                if (random.nextInt(3) == 0) {
                    LeavesBlock.dropStacks(state, world, pos, null, null, null);
                    world.removeBlock(pos, false);
                    ci.cancel();
                }
                // If random check fails, cancel vanilla behavior to prevent
                // the 4-7 second decay timer
                ci.cancel();
            }
        } else {
            // Leaf is connected to a log, should not decay
            ci.cancel();
        }
    }
}
