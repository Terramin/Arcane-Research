package arcaneresearch.common.golems.seals;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.CropUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

public class SealHarvest implements ISeal, ISealGui, ISealConfigArea, ISealConfigToggles {

    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");
    protected SealToggle[] props;
    private int delay;
    private int count = 0;
    private HashMap<Long, ReplantInfo> replantTasks = new HashMap<>();
    private Identifier icon;

    public SealHarvest() {
        delay = new Random(System.nanoTime()).nextInt(33);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_harvest");
        props = new SealToggle[]{
                new SealToggle(true, "prep", "golem.prop.replant"),
                new SealToggle(false, "ppro", "golem.prop.provision")
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

    @Override
    public String getKey() {
        return "arcaneresearch:harvest";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % 100 == 0) {
            var area = GolemHelper.getBoundsForArea(seal);
            Iterator<Long> it = replantTasks.keySet().iterator();
            while (it.hasNext()) {
                BlockPos pos = BlockPos.of(it.next());
                if (!area.contains(Vec3.atCenterOf(pos))) {
                    ReplantInfo info = replantTasks.get(pos.asLong());
                    if (info != null) {
                        Task task = TaskHandler.getTask(level.dimension(), info.taskId);
                        if (task != null) {
                            task.setSuspended(true);
                        }
                    }
                    it.remove();
                }
            }
        }

        if (delay++ % 5 != 0) {
            return;
        }

        BlockPos pos = GolemHelper.getPosInArea(seal, count++);

        if (isGrownCrop(level, pos)) {
            Task task = new Task(seal.getSealPos(), pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level.dimension(), task);
        } else if (props[0].getValue() && replantTasks.containsKey(pos.asLong()) && level.isEmptyBlock(pos)) {
            ReplantInfo info = replantTasks.get(pos.asLong());
            Task existingTask = TaskHandler.getTask(level.dimension(), info.taskId);
            if (existingTask == null) {
                Task newTask = new Task(seal.getSealPos(), info.pos);
                newTask.setPriority(seal.getPriority());
                TaskHandler.addTask(level.dimension(), newTask);
                info.taskId = newTask.getId();
            }
        }
    }

    private boolean isGrownCrop(Level level, BlockPos pos) {
        return CropUtils.isGrownCrop(level, pos);
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        if (isGrownCrop(level, task.getPos())) {
            BlockState state = level.getBlockState(task.getPos());

            if (CropUtils.isClickableCrop(level, task.getPos())) {
                arcaneresearch.common.golems.GolemInteractionHelper.golemClick(
                        level, golem, task.getPos(), task.getSealPos().face,
                        ItemStack.EMPTY, false, true);

            } else {
                arcaneresearch.common.golems.GolemInteractionHelper.golemClick(
                        level, golem, task.getPos(), task.getSealPos().face,
                        ItemStack.EMPTY, false, true);

                if (isGrownCrop(level, task.getPos())) {
                    FakePlayer fp = getFakePlayer(level, golem);
                    if (fp != null) {
                        BlockUtils.harvestBlock(level, fp, task.getPos(), true, false, 0, true);
                    } else {
                        level.destroyBlock(task.getPos(), true);
                    }
                    golem.addRankXp(1);
                    golem.swingArm();

                    if (props[0].getValue()) {
                        ItemStack seed = arcaneresearch.api.ArcaneresearchApi.getSeed(state.getBlock());
                        if (seed != null && !seed.isEmpty()) {
                            BlockState below = level.getBlockState(task.getPos().below());
                            Direction rf = null;
                            // NEOFORGE_PORT: forge IPlantable removed; seeds are BlockItems whose
                            // crop block sustains via BlockState.canSustainPlant (now TriState).
                            if (seed.getItem() instanceof net.minecraft.world.item.BlockItem seedBlock
                                    && below.canSustainPlant(level, task.getPos().below(), Direction.UP,
                                            seedBlock.getBlock().defaultBlockState()).toBoolean(true)) {
                                rf = Direction.DOWN;
                            } else if (!(seed.getItem() instanceof net.minecraft.world.item.BlockItem)
                                    && state.getBlock() instanceof net.minecraft.world.level.block.DirectionalBlock
                                    && state.hasProperty(
                                    net.minecraft.world.level.block.DirectionalBlock.FACING)) {
                                rf = state.getValue(
                                        net.minecraft.world.level.block.DirectionalBlock.FACING);
                            }
                            if (rf != null) {
                                Task tt = new Task(task.getSealPos(), task.getPos());
                                tt.setPriority(task.getPriority());
                                tt.setLifespan((short) 300);
                                replantTasks.put(task.getPos().asLong(), new ReplantInfo(task.getPos(), rf,
                                        tt.getId(), seed.copy(), below.is(Blocks.FARMLAND)));
                                TaskHandler.addTask(level.dimension(), tt);
                            }
                        }
                    }
                }
            }
        } else if (replantTasks.containsKey(task.getPos().asLong())
                && replantTasks.get(task.getPos().asLong()).taskId == task.getId()
                && level.isEmptyBlock(task.getPos())
                && golem.isCarrying(replantTasks.get(task.getPos().asLong()).seed)) {
            ReplantInfo ri = replantTasks.get(task.getPos().asLong());
            BlockState below = level.getBlockState(task.getPos().below());
            if ((below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)) && ri.farmland) {
                arcaneresearch.common.golems.GolemInteractionHelper.useItemOnBlock(
                        level, golem, task.getPos().below(),
                        new ItemStack(net.minecraft.world.item.Items.DIAMOND_HOE), Direction.UP);
            }
            ItemStack seed = ri.seed.copy();
            seed.setCount(1);
            arcaneresearch.common.golems.GolemInteractionHelper.golemClick(
                    level, golem, task.getPos().relative(ri.face), ri.face.getOpposite(),
                    seed, false, true);
            if (!level.isEmptyBlock(task.getPos())) {
                level.levelEvent(2001, task.getPos(),
                        net.minecraft.world.level.block.Block.getId(level.getBlockState(task.getPos())));
                golem.dropItem(seed);
                golem.addRankXp(1);
                golem.swingArm();
            }
        }

        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (replantTasks.containsKey(task.getPos().asLong())) {
            ReplantInfo info = replantTasks.get(task.getPos().asLong());
            if (info.taskId == task.getId()) {
                boolean hasSeeds = golem.isCarrying(info.seed);
                if (!hasSeeds && props[1].getValue()) {
                    ISealEntity sealEntity = SealHandler.getSealEntity(golem.getGolemWorld().dimension(), task.getSealPos());
                    if (sealEntity != null) {
                        GolemHelper.requestProvisioning(golem.getGolemWorld(), sealEntity, info.seed);
                    }
                }
                return hasSeeds;
            }
        }
        return true;
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
        return new int[]{2, 3, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.DEFT, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
        // NEOFORGE_PORT: 1:1 from Forge; ItemStack.of/save removed ->
        // ItemStack.CODEC nested under "seed" (was flat-merged into the task tag).
        ListTag list = nbt.getListOrEmpty("replant");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompoundOrEmpty(i);
            long loc = tag.getLongOr("taskloc", 0L);
            byte face = tag.getByteOr("taskface", (byte) 0);
            boolean farmland = tag.getBooleanOr("farmland", false);
            ItemStack seed = ItemStack.EMPTY;
            if (tag.contains("seed")) {
                seed = ItemStack.CODEC.parse(SealHandler.nbtOps(), tag.get("seed"))
                        .result().orElse(ItemStack.EMPTY);
            }
            replantTasks.put(loc, new ReplantInfo(BlockPos.of(loc), Direction.values()[face], 0, seed, farmland));
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
        if (props[0].getValue()) {
            ListTag list = new ListTag();
            for (Long key : replantTasks.keySet()) {
                ReplantInfo info = replantTasks.get(key);
                CompoundTag tag = new CompoundTag();
                tag.putLong("taskloc", info.pos.asLong());
                tag.putByte("taskface", (byte) info.face.ordinal());
                tag.putBoolean("farmland", info.farmland);
                // NEOFORGE_PORT: ItemStack.save removed -> CODEC (see readCustomNBT).
                ItemStack.CODEC.encodeStart(SealHandler.nbtOps(), info.seed)
                        .result().ifPresent(encoded -> tag.put("seed", encoded));
                list.add(tag);
            }
            nbt.put("replant", list);
        }
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