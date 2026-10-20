package arcaneresearch.common.lib.events;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.blocks.misc.BlockEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

// NEOFORGE_PORT (26.3): sap/shock effect blocks are .air(), and 26.3 Entity.checkInsideBlocks
// skips air, so BlockEffect.entityInside never ran (Eldritch Warden sap field, curse focus sap).
// Re-create the 1.20.1 checkInsideBlocks pass (entity box deflated by 1e-7, every tick the entity
// moves physically) for these blocks only.
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class EffectBlockEvents {
    private EffectBlockEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide() || entity.noPhysics || !entity.isAlive()) {
            return;
        }
        AABB box = entity.getBoundingBox();
        BlockPos min = BlockPos.containing(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
        BlockPos max = BlockPos.containing(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
        if (!level.hasChunksAt(min, max)) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BlockEffect effect) {
                effect.applyInside(level, pos.immutable(), entity);
                if (!entity.isAlive()) {
                    return;
                }
            }
        }
    }
}
