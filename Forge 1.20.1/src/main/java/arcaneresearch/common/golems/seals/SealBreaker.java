package arcaneresearch.common.golems.seals;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
public class SealBreaker extends SealFiltered implements ISealConfigArea, ISealConfigToggles {
    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");
    protected SealToggle[] props;
    private int delay;
    private HashMap<Integer, Long> cache = new HashMap<>();
    private ResourceLocation icon;
    public SealBreaker() {
        delay = new Random(System.nanoTime()).nextInt(42);
        icon = new ResourceLocation(Arcaneresearch.MODID, "item/seals/seal_breaker");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta")
        };
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
    protected boolean isSilkTouch() {
        SealToggle[] toggles = getToggles();
        return toggles.length > 1 && toggles[1].getValue();
    }
    @Override
    public String getKey() {
        return "arcaneresearch:breaker";
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
        if (!cache.containsValue(pos.asLong()) && isValidBlock(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            BlockState state = level.getBlockState(pos);
            task.setData((int) (state.getDestroySpeed(level, pos) * 10.0f));
            TaskHandler.addTask(level.dimension(), task);
            cache.put(task.getId(), pos.asLong());
        }
    }
    private boolean isValidBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (level.isEmptyBlock(pos) || state.getDestroySpeed(level, pos) < 0.0f) {
            return false;
        }
        ItemStack fs = BlockUtils.getSilkTouchDrop(state, level, pos);
        if (fs.isEmpty()) {
            fs = new ItemStack(state.getBlock());
        }
        SealToggle[] toggles = getToggles();
        ArcaneresearchInvHelper.InvFilter invFil = invFilter(new SealToggle[]{toggles[0]});
        for (ItemStack ts : getInv()) {
            if (ts != null && !ts.isEmpty()) {
                boolean matches = ArcaneresearchInvHelper.areItemStacksEqualWithFilter(fs, ts, invFil);
                if (isBlacklist()) {
                    if (matches) {
                        return false;
                    }
                    continue;
                }
                if (!matches) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        BlockState state = level.getBlockState(task.getPos());
        if (cache.containsKey(task.getId()) && isValidBlock(level, task.getPos())) {
            golem.swingArm();
            boolean silky = isSilkTouch();
            int bspd = silky ? 7 : 21;
            if (task.getData() > bspd) {
                float bh = state.getDestroySpeed(level, task.getPos()) * 10.0f;
                task.setLifespan((short) Math.max(task.getLifespan(), 10));
                task.setData(task.getData() - bspd);
                int progress = (int) (9.0f * (1.0f - task.getData() / bh));
                level.playSound(null, task.getPos(),
                        state.getSoundType().getBreakSound(),
                        SoundSource.BLOCKS,
                        (state.getSoundType().getVolume() + 0.7f) / 8.0f,
                        state.getSoundType().getPitch() * 0.5f);
                level.destroyBlockProgress(golem.getGolemEntity().getId(), task.getPos(), progress);
                return false;
            }
            level.destroyBlockProgress(golem.getGolemEntity().getId(), task.getPos(), 10);
            FakePlayer fp = getFakePlayer(level, golem);
            if (fp != null) {
                BlockUtils.harvestBlock(level, fp, task.getPos(), true, silky, 0, true);
            } else {
                level.destroyBlock(task.getPos(), true);
            }
            golem.addRankXp(1);
            cache.remove(task.getId());
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
        cache.remove(task.getId());
    }
    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.isEmptyBlock(pos);
    }
    @Override
    public ResourceLocation getSealIcon() {
        return icon;
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
        return new int[]{2, 1, 3, 0, 4};
    }
    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.BREAKER};
    }
    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
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
