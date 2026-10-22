package thaumictinker.necromancy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.tiles.crafting.TilePedestal;

 




 
public class NecromancyTabletBlockEntity extends BlockEntity {

    public static final int INTERVAL = 300;
    public static final int INFUSED_INTERVAL = 1200;
    public static final int ASPECT_COUNT = NecromancyFeature.ASPECT_COUNT;

    public NecromancyTabletBlockEntity(BlockPos pos, BlockState state) {
        super(NecromancyFeature.TABLET_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NecromancyTabletBlockEntity tablet) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % INTERVAL == 0) {
            tablet.attempt(serverLevel, serverLevel.getGameTime());
        }
    }

    public boolean attempt(ServerLevel serverLevel, long time) {
        if (serverLevel.hasNeighborSignal(worldPosition)) {
            return false;
        }
        for (int radius = 1; radius < 6; radius++) {
            List<TilePedestal> pedestals = new ArrayList<>();
            for (int x = worldPosition.getX() - radius; x < worldPosition.getX() + radius; x++) {
                for (int z = worldPosition.getZ() - radius; z < worldPosition.getZ() + radius; z++) {
                    BlockPos pedestalPos = new BlockPos(x, worldPosition.getY(), z);
                    if (serverLevel.getBlockEntity(pedestalPos) instanceof TilePedestal pedestal
                            && !pedestal.isEmpty()
                            && SoulAspectItem.isSoul(pedestal.getItem(0))) {
                        pedestals.add(pedestal);
                    }
                }
            }
            for (TilePedestal first : pedestals) {
                for (TilePedestal second : pedestals) {
                    for (TilePedestal third : pedestals) {
                        if (first == second || second == third || first == third) {
                            continue;
                        }
                        List<Aspect> aspects = List.of(
                                SoulAspectItem.aspectOf(first.getItem(0)),
                                SoulAspectItem.aspectOf(second.getItem(0)),
                                SoulAspectItem.aspectOf(third.getItem(0)));
                        MobAspect recipe = MobAspect.forAspects(aspects);
                        if (recipe == null) {
                            continue;
                        }
                        List<TilePedestal> used = List.of(first, second, third);
                        boolean infused = SoulAspectItem.isInfused(first.getItem(0))
                                && SoulAspectItem.isInfused(second.getItem(0))
                                && SoulAspectItem.isInfused(third.getItem(0));
                        if (infused && time % INFUSED_INTERVAL != 0) {
                            return false;
                        }
                        if (!infused) {
                            effects(serverLevel, used, aspects);
                            for (TilePedestal pedestal : used) {
                                pedestal.removeItem(0, 1);
                            }
                            spawn(serverLevel, recipe);
                        } else {
                            if (matches(first.getItem(0)) && matches(second.getItem(0)) && matches(third.getItem(0))) {
                                effects(serverLevel, used, aspects);
                                spawn(serverLevel, recipe);
                            }
                            for (TilePedestal pedestal : used) {
                                ItemStack marked =pedestal.getItem(0).copy();
                                SoulAspectItem.setLastTablet(marked, worldPosition);
                                pedestal.setItem(0, marked);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean matches(ItemStack stack) {
        BlockPos last = SoulAspectItem.getLastTablet(stack);
        return last == null || last.equals(worldPosition);
    }

    private void spawn(ServerLevel serverLevel, MobAspect recipe) {
        EntityType<?> type = recipe.entityType().get();
        if (type == EntityType.SKELETON && serverLevel.dimension() == Level.NETHER) {
            type = EntityType.WITHER_SKELETON;
        }
        Entity entity = type.create(serverLevel);
        if (entity == null) {
            return;
        }
        entity.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 0.0F, 0.0F);
        if (entity instanceof Mob mob) {
            DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(worldPosition);
            net.minecraftforge.event.ForgeEventFactory.onFinalizeSpawn(mob, serverLevel, difficulty,
                    MobSpawnType.MOB_SUMMONED, null, null);
        }
        serverLevel.addFreshEntity(entity);
        if (entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
    }

    private void effects(ServerLevel serverLevel, List<TilePedestal> pedestals, List<Aspect> aspectList) {
        for (int index = 0; index < pedestals.size(); index++) {
            BlockPos offset = pedestals.get(index).getBlockPos().subtract(worldPosition);
            int aspectIndex = NecromancyFeature.ASPECTS.indexOf(aspectList.get(index));
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), aspectIndex,
                    ((offset.getX() + 8) << 4) | (offset.getZ() + 8));
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id >= 0 && id < ASPECT_COUNT) {
            if (level != null && level.isClientSide()) {
                BlockPos from = worldPosition.offset((type >> 4) - 8, 0, (type & 15) - 8);
                FXDispatcher.INSTANCE.essentiaTrailFx(from, worldPosition, 20,
                        NecromancyFeature.ASPECTS.get(id).getColor(), 1.0F, 0);
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
