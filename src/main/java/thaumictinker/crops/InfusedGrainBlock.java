package thaumictinker.crops;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import thaumictinker.util.AspectCombination;

 

 
public class InfusedGrainBlock extends CropBlock implements EntityBlock {

    public static final EnumProperty<CropKind> KIND = EnumProperty.create("kind", CropKind.class);
    public static final int BREEDING_CHANCE = 10;

    public InfusedGrainBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(KIND, CropKind.AER));
    }

    public BlockState stateFor(Aspect aspect) {
        return defaultBlockState().setValue(KIND, CropKind.of(aspect));
    }

    public BlockState withAspect(BlockState state, Aspect aspect) {
        return state.setValue(KIND, CropKind.of(aspect));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(KIND);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusedGrainBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != CropsFeature.INFUSED_GRAIN_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) ->
                InfusedGrainBlockEntity.tick(tickLevel, pos, tickState, (InfusedGrainBlockEntity) entity);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof InfusedFarmlandBlock;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        int age = Math.min(getMaxAge(), getAge(state) + getBonemealAgeIncrease(level));
        level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return !isMaxAge(state);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return CropsFeature.INFUSED_SEEDS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof InfusedGrainBlockEntity grain) {
            ItemStack stack = InfusedSeedsItem.stackFor(grain.getAspect());
            InfusedSeedsItem.setTendencies(stack, grain.getTendencies());
            return stack;
        }
        return new ItemStack(CropsFeature.INFUSED_SEEDS);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ServerLevel level = params.getLevel();
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        if (!(entity instanceof InfusedGrainBlockEntity) && origin != null) {
            entity = level.getBlockEntity(BlockPos.containing(origin));
        }
        if (!(entity instanceof InfusedGrainBlockEntity grain)) {
            return List.of();
        }
        return drops(level, grain, isMaxAge(state), level.getRandom());
    }

    public static List<ItemStack> drops(ServerLevel level, InfusedGrainBlockEntity grain, boolean mature, RandomSource random) {
        List<ItemStack> result = new ArrayList<>();
        ItemStack seeds = InfusedSeedsItem.stackFor(droppedAspect(level, grain, mature, random));
        InfusedSeedsItem.setTendencies(seeds, grain.getTendencies());
        int entropy = grain.tendency(Aspect.ENTROPY);
        while (random.nextInt(10000) < entropy * entropy) {
            seeds.grow(1);
        }
        result.add(seeds);
        if (mature) {
            fertilizeSoil(level, grain, random);
            int order = grain.tendency(Aspect.ORDER);
            do {
                ItemStack loot = CropLootManager.getLootForAspect(grain.getAspect(), random);
                if (!loot.isEmpty()) {
                    result.add(loot);
                }
            } while (random.nextInt(75) < order);
        }
        return result;
    }

    public static Aspect droppedAspect(ServerLevel level, InfusedGrainBlockEntity grain, boolean mature, RandomSource random) {
        Aspect current = grain.getAspect();
        if (mature && level.getBlockEntity(grain.getBlockPos().below()) instanceof InfusedFarmlandBlockEntity farmland) {
            return mutate(current, farmland.getAspects(), grain.tendency(Aspect.FIRE), random);
        }
        return current;
    }

    public static Aspect mutate(Aspect current, AspectList residue, int fireTendency, RandomSource random) {
        for (Aspect aspect : residue.getAspects()) {
            int amount = residue.getAmount(aspect);
            if (random.nextInt(BREEDING_CHANCE) < (fireTendency + 1) * amount * amount) {
                Aspect combined = AspectCombination.combine(aspect, current);
                if (combined != null) {
                    return combined;
                }
            }
        }
        return current;
    }

    public static void fertilizeSoil(ServerLevel level, InfusedGrainBlockEntity grain, RandomSource random) {
        if (!(level.getBlockEntity(grain.getBlockPos().below()) instanceof InfusedFarmlandBlockEntity farmland)) {
            return;
        }
        int earth = grain.tendency(Aspect.EARTH);
        do {
            farmland.getAspects().add(grain.getAspect(), 1);
            farmland.reduceSaturatedAspects(random);
            farmland.setChanged();
        } while (random.nextInt(55) < earth);
    }
}
