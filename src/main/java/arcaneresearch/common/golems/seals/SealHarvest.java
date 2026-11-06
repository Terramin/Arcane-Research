package arcaneresearch.common.golems.seals;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.GolemInteractionHelper;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.CropUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SealHarvest implements ISeal, ISealGui, ISealConfigArea, ISealConfigToggles {

    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");

    private static final int TOGGLE_REPLANT = 0;
    private static final int TOGGLE_PROVISION = 1;
    private static final int DELAY_RANGE = 33;
    private static final int CLEANUP_PERIOD = 100;
    private static final int PROCESS_PERIOD = 5;
    private static final short REPLANT_LIFESPAN = 300;

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_harvest");
    private static final int[] GUI_CATEGORIES = {2, 3, 0, 4};
    private static final EnumGolemTrait[] REQUIRED_TAGS = {EnumGolemTrait.DEFT, EnumGolemTrait.SMART};
    private static final EnumGolemTrait[] FORBIDDEN_TAGS = new EnumGolemTrait[0];

    protected SealToggle[] props;
    private int delay;
    private int count = 0;
    private final Map<Long, ReplantInfo> replantTasks = new HashMap<>();

    public SealHarvest() {
        this.delay = ThreadLocalRandom.current().nextInt(DELAY_RANGE);
        this.props = new SealToggle[]{
                new SealToggle(true, "prep", "golem.prop.replant"),
                new SealToggle(false, "ppro", "golem.prop.provision")
        };
    }

    private static FakePlayer getFakePlayer(Level level, IGolemAPI golem) {
        if (golem == null) {
            return null;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        FakePlayer fp = FakePlayerFactory.get(serverLevel, GOLEM_PROFILE);
        var entity = golem.getGolemEntity();
        if (entity != null) {
            fp.setPos(entity.getX(), entity.getY(), entity.getZ());
        }
        return fp;
    }

    @Override
    public String getKey() {
        return "arcaneresearch:harvest";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % CLEANUP_PERIOD == 0) {
            boolean prepOn = props[TOGGLE_REPLANT].getValue();
            AABB area = prepOn ? GolemHelper.getBoundsForArea(seal) : null;
            Iterator<Map.Entry<Long, ReplantInfo>> it = replantTasks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, ReplantInfo> entry = it.next();
                if (!prepOn || !area.contains(Vec3.atCenterOf(BlockPos.of(entry.getKey())))) {
                    Task task = TaskHandler.getTask(level.dimension(), entry.getValue().taskId);
                    if (task != null) {
                        task.setSuspended(true);
                    }
                    it.remove();
                }
            }
        }

        if (delay++ % PROCESS_PERIOD != 0) {
            return;
        }

        BlockPos pos = GolemHelper.getPosInArea(seal, count++);
        if (isGrownCrop(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level.dimension(), task);
            return;
        }

        if (props[TOGGLE_REPLANT].getValue() && level.isEmptyBlock(pos)) {
            ReplantInfo info = replantTasks.get(pos.asLong());
            if (info != null) {
                Task existingTask = TaskHandler.getTask(level.dimension(), info.taskId);
                if (existingTask == null) {
                    Task newTask = new Task(seal.getSealPos(), info.pos);
                    newTask.setPriority(seal.getPriority());
                    TaskHandler.addTask(level.dimension(), newTask);
                    info.taskId = newTask.getId();
                }
            }
        }
    }

    private boolean isGrownCrop(Level level, BlockPos pos) {
        return CropUtils.isGrownCrop(level, pos);
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        BlockPos taskPos = task.getPos();
        long taskPosLong = taskPos.asLong();
        Direction clickFace = task.getSealPos().face;

        if (isGrownCrop(level, taskPos)) {
            if (CropUtils.isClickableCrop(level, taskPos)) {
                GolemInteractionHelper.golemClick(level, golem, taskPos, clickFace,
                        ItemStack.EMPTY, false, true);
            } else {
                GolemInteractionHelper.golemClick(level, golem, taskPos, clickFace,
                        ItemStack.EMPTY, false, true);
                if (isGrownCrop(level, taskPos)) {
                    BlockState state = level.getBlockState(taskPos);
                    FakePlayer fp = getFakePlayer(level, golem);
                    if (fp != null) {
                        BlockUtils.harvestBlock(level, fp, taskPos, true, false, 0, true);
                    } else {
                        level.destroyBlock(taskPos, true);
                    }
                    golem.addRankXp(1);
                    golem.swingArm();
                    if (props[TOGGLE_REPLANT].getValue()) {
                        ItemStack seed = ArcaneresearchApi.getSeed(state.getBlock());
                        if (seed != null && !seed.isEmpty()) {
                            BlockPos belowPos = taskPos.below();
                            BlockState below = level.getBlockState(belowPos);
                            Direction rf = null;
                            if (seed.getItem() instanceof IPlantable plantable
                                    && below.canSustainPlant(level, belowPos, Direction.UP, plantable)) {
                                rf = Direction.DOWN;
                            } else if (!(seed.getItem() instanceof IPlantable)
                                    && state.getBlock() instanceof DirectionalBlock
                                    && state.hasProperty(DirectionalBlock.FACING)) {
                                rf = state.getValue(DirectionalBlock.FACING);
                            }
                            if (rf != null) {
                                Task tt = new Task(task.getSealPos(), taskPos);
                                tt.setPriority(task.getPriority());
                                tt.setLifespan(REPLANT_LIFESPAN);
                                TaskHandler.addTask(level.dimension(), tt);
                                replantTasks.put(taskPosLong, new ReplantInfo(taskPos, rf, tt.getId(),
                                        seed.copy(), below.is(Blocks.FARMLAND)));
                            }
                        }
                    }
                }
            }
        } else {
            ReplantInfo ri = replantTasks.get(taskPosLong);
            if (ri != null
                    && ri.taskId == task.getId()
                    && level.isEmptyBlock(taskPos)
                    && golem.isCarrying(ri.seed)) {
                BlockPos belowPos = taskPos.below();
                BlockState below = level.getBlockState(belowPos);
                if ((below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)) && ri.farmland) {
                    GolemInteractionHelper.useItemOnBlock(level, golem, belowPos,
                            new ItemStack(Items.DIAMOND_HOE), Direction.UP);
                }
                ItemStack seed = ri.seed.copy();
                seed.setCount(1);
                GolemInteractionHelper.golemClick(level, golem,
                        taskPos.relative(ri.face), ri.face.getOpposite(), seed, false, true);
                if (!level.isEmptyBlock(taskPos)) {
                    level.levelEvent(2001, taskPos, Block.getId(level.getBlockState(taskPos)));
                    golem.dropItem(seed);
                    golem.addRankXp(1);
                    golem.swingArm();
                    replantTasks.remove(taskPosLong);
                }
            }
        }

        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ReplantInfo info = replantTasks.get(task.getPos().asLong());
        if (info == null || info.taskId != task.getId()) {
            return true;
        }
        boolean hasSeeds = golem.isCarrying(info.seed);
        if (!hasSeeds && props[TOGGLE_PROVISION].getValue()) {
            Level world = golem.getGolemWorld();
            ISealEntity sealEntity = SealHandler.getSealEntity(world.dimension(), task.getSealPos());
            if (sealEntity != null) {
                GolemHelper.requestProvisioning(world, sealEntity, info.seed);
            }
        }
        return hasSeeds;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
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
    public void readCustomNBT(CompoundTag nbt) {
        replantTasks.clear();
        ListTag list = nbt.getList("replant", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            long loc = tag.getLong("taskloc");
            int faceOrd = tag.getByte("taskface");
            Direction face = (faceOrd >= 0 && faceOrd < Direction.values().length)
                    ? Direction.values()[faceOrd]
                    : Direction.UP;
            boolean farmland = tag.getBoolean("farmland");
            ItemStack seed = ItemStack.of(tag);
            replantTasks.put(loc, new ReplantInfo(BlockPos.of(loc), face, 0, seed, farmland));
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        if (!props[TOGGLE_REPLANT].getValue()) {
            return;
        }
        ListTag list = new ListTag();
        for (Map.Entry<Long, ReplantInfo> entry : replantTasks.entrySet()) {
            ReplantInfo info = entry.getValue();
            CompoundTag tag = new CompoundTag();
            tag.putLong("taskloc", info.pos.asLong());
            tag.putByte("taskface", (byte) info.face.ordinal());
            tag.putBoolean("farmland", info.farmland);
            info.seed.save(tag);
            list.add(tag);
        }
        nbt.put("replant", list);
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

    private static class ReplantInfo {
        Direction face;
        BlockPos pos;
        int taskId;
        ItemStack seed;
        boolean farmland;

        public ReplantInfo(BlockPos pos, Direction face, int taskId, ItemStack seed, boolean farmland) {
            this.pos = pos;
            this.face = face;
            this.taskId = taskId;
            this.seed = seed;
            this.farmland = farmland;
        }
    }
}