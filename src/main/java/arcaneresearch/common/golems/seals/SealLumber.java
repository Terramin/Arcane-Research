package arcaneresearch.common.golems.seals;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.api.golems.seals.ISealConfigArea;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.ISealGui;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.BlockUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

public class SealLumber implements ISeal, ISealGui, ISealConfigArea {

    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");
    private int delay;
    private HashMap<Integer, Long> cache = new HashMap<>();
    private Identifier icon;

    public SealLumber() {
        delay = new Random(System.nanoTime()).nextInt(33);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_lumber");
    }

    private static FakePlayer getFakePlayer(Level level, IGolemAPI golem) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        FakePlayer fp = FakePlayerFactory.get(serverLevel, GOLEM_PROFILE);
        if (golem.getGolemEntity() != null) {
            fp.setPos(golem.getGolemEntity().getX(),
                    golem.getGolemEntity().getY(),
                    golem.getGolemEntity().getZ());
        }
        return fp;
    }

    @Override
    public String getKey() {
        return "arcaneresearch:lumber";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % 100 == 0) {
            Iterator<Integer> it = cache.keySet().iterator();
            while (it.hasNext()) {
                Task task = TaskHandler.getTask(level.dimension(), it.next());
                if (task == null) {
                    it.remove();
                }
            }
        }

        delay++;
        BlockPos pos = GolemHelper.getPosInArea(seal, delay);

        if (!cache.containsValue(pos.asLong()) && isWoodLog(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level.dimension(), task);
            cache.put(task.getId(), pos.asLong());
        }
    }

    private boolean isWoodLog(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(BlockTags.LOGS)) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof RotatedPillarBlock && state.getSoundType() == SoundType.WOOD) {
            return true;
        }
        return block.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT).contains("log");
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && isWoodLog(level, task.getPos())) {
            FakePlayer fp = getFakePlayer(level, golem);
            if (fp == null) {
                task.setSuspended(true);
                return true;
            }
            BlockState state = level.getBlockState(task.getPos());
            golem.swingArm();
            if (BlockUtils.breakFurthestBlock(level, task.getPos(), state, fp)) {
                task.setLifespan((short) Math.max(task.getLifespan(), 10));
                golem.addRankXp(1);
                return false;
            }

            cache.remove(task.getId());
        }

        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && isWoodLog(golem.getGolemWorld(), task.getPos())) {
            return true;
        }
        
        return false;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        cache.remove(task.getId());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public Identifier getSealIcon() {
        return icon;
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    public int[] getGuiCategories() {
        return new int[]{2, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.BREAKER, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
    }
}