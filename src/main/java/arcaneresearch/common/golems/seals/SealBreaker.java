package arcaneresearch.common.golems.seals;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigArea;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.BlockUtils;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SealBreaker extends SealFiltered implements ISealConfigArea, ISealConfigToggles {

    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");

    private static final int CACHE_CLEANUP_PERIOD = 100;
    private static final int BASE_DELAY_RANGE = 42;
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_breaker");
    private static final int[] GUI_CATEGORIES = {2, 1, 3, 0, 4};
    private static final EnumGolemTrait[] REQUIRED_TAGS = {EnumGolemTrait.BREAKER};
    private static final EnumGolemTrait[] FORBIDDEN_TAGS = new EnumGolemTrait[0];

    protected SealToggle[] props;
    private final SealToggle[] filterToggles;
    private int delay;
    private final Map<Integer, Long> cache = new HashMap<>();
    private final LongSet cachedPositions = new LongOpenHashSet();

    public SealBreaker() {
        this.delay = ThreadLocalRandom.current().nextInt(BASE_DELAY_RANGE);
        this.props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta")
        };
        this.filterToggles = new SealToggle[]{this.props[0]};
    }

    private static FakePlayer getFakePlayer(Level level, IGolemAPI golem) {
        if (golem == null) {
            return null;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        FakePlayer fp = FakePlayerFactory.get(serverLevel, GOLEM_PROFILE);
        LivingEntity entity = golem.getGolemEntity();
        if (entity != null) {
            fp.setPos(entity.getX(), entity.getY(), entity.getZ());
        }
        return fp;
    }

    protected boolean isSilkTouch() {
        SealToggle[] toggles = getToggles();
        return toggles.length > 1 && toggles[1].getValue();
    }

    private void removeFromCache(int taskId) {
        Long pos = cache.remove(taskId);
        if (pos != null) {
            cachedPositions.remove(pos.longValue());
        }
    }

    @Override
    public String getKey() {
        return "arcaneresearch:breaker";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % CACHE_CLEANUP_PERIOD == 0) {
            ResourceKey<Level> dim = level.dimension();
            Iterator<Map.Entry<Integer, Long>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Long> entry = it.next();
                Task task = TaskHandler.getTask(dim, entry.getKey());
                if (task == null) {
                    cachedPositions.remove(entry.getValue().longValue());
                    it.remove();
                }
            }
        }
        delay++;
        BlockPos pos = GolemHelper.getPosInArea(seal, delay);
        long posLong = pos.asLong();
        if (!cachedPositions.contains(posLong) && isValidBlock(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            BlockState state = level.getBlockState(pos);
            task.setData((int) (state.getDestroySpeed(level, pos) * 10.0f));
            TaskHandler.addTask(level.dimension(), task);
            cache.put(task.getId(), posLong);
            cachedPositions.add(posLong);
        }
    }

    private boolean isValidBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0f) {
            return false;
        }
        ItemStack fs = BlockUtils.getSilkTouchDrop(state, level, pos);
        if (fs.isEmpty()) {
            fs = new ItemStack(state.getBlock());
        }
        if (fs.isEmpty()) {
            return false;
        }
        ArcaneresearchInvHelper.InvFilter invFil = invFilter(filterToggles);
        boolean blacklist = isBlacklist();
        Iterable<ItemStack> inv = getInv();
        if (inv != null) {
            for (ItemStack ts : inv) {
                if (ts == null || ts.isEmpty()) {
                    continue;
                }
                boolean matches = ArcaneresearchInvHelper.areItemStacksEqualWithFilter(fs, ts, invFil);
                if (blacklist) {
                    if (matches) {
                        return false;
                    }
                } else {
                    if (!matches) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        BlockPos pos = task.getPos();
        BlockState state = level.getBlockState(pos);
        if (cache.containsKey(task.getId()) && isValidBlock(level, pos)) {
            LivingEntity entity = golem.getGolemEntity();
            if (entity == null) {
                removeFromCache(task.getId());
                task.setSuspended(true);
                return true;
            }
            golem.swingArm();
            boolean silky = isSilkTouch();
            int bspd = silky ? 7 : 21;
            if (task.getData() > bspd) {
                float bh = state.getDestroySpeed(level, pos) * 10.0f;
                if (bh > 0.0f) {
                    task.setLifespan((short) Math.max(task.getLifespan(), 10));
                    task.setData(task.getData() - bspd);
                    int progress = (int) (9.0f * (1.0f - task.getData() / bh));
                    progress = Mth.clamp(progress, 0, 9);
                    level.playSound(null, pos,
                            state.getSoundType().getBreakSound(),
                            SoundSource.BLOCKS,
                            (state.getSoundType().getVolume() + 0.7f) / 8.0f,
                            state.getSoundType().getPitch() * 0.5f);
                    level.destroyBlockProgress(entity.getId(), pos, progress);
                    return false;
                }
            }
            level.destroyBlockProgress(entity.getId(), pos, 10);
            FakePlayer fp = getFakePlayer(level, golem);
            if (fp != null) {
                BlockUtils.harvestBlock(level, fp, pos, true, silky, 0, true);
            } else {
                level.destroyBlock(pos, true);
            }
            golem.addRankXp(1);
            removeFromCache(task.getId());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (cache.containsKey(task.getId()) && isValidBlock(golem.getGolemWorld(), task.getPos())) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        removeFromCache(task.getId());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public ResourceLocation getSealIcon() {
        return ICON;
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public int[] getGuiCategories() {
        return GUI_CATEGORIES;
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return REQUIRED_TAGS;
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return FORBIDDEN_TAGS;
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        if (index >= 0 && index < props.length) {
            props[index].setValue(value);
        }
    }
}