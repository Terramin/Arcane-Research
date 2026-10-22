package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;

import java.util.List;
import java.util.Random;

public class SealGuard implements ISeal, ISealGui, ISealConfigArea, ISealConfigToggles {

    protected SealToggle[] props;
    private int delay;
    private Identifier icon;

    public SealGuard() {
        delay = new Random(System.nanoTime()).nextInt(22);
        props = new SealToggle[]{
                new SealToggle(true, "pmob", "golem.prop.mob"),
                new SealToggle(false, "panimal", "golem.prop.animal"),
                new SealToggle(false, "pplayer", "golem.prop.player")
        };
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_guard");
    }

    private static boolean isAnimalLike(LivingEntity target) {
        if (target instanceof Animal) {
            return true;
        }
        net.minecraft.world.entity.MobCategory cat = target.getType().getCategory();
        return cat == net.minecraft.world.entity.MobCategory.CREATURE
                || cat == net.minecraft.world.entity.MobCategory.AMBIENT
                || cat == net.minecraft.world.entity.MobCategory.WATER_AMBIENT
                || cat == net.minecraft.world.entity.MobCategory.WATER_CREATURE
                || cat == net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE;
    }

    @Override
    public String getKey() {
        return "arcaneresearch:guard";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }

        AABB area = GolemHelper.getBoundsForArea(seal);
        
        
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);

        if (!targets.isEmpty()) {
            for (LivingEntity target : targets) {
                if (isValidTarget(target)) {
                    Task task = new Task(seal.getSealPos(), target);
                    task.setPriority(seal.getPriority());
                    task.setLifespan((short) 10);
                    TaskHandler.addTask(level.dimension(), task);
                }
            }
        }
    }

    private boolean isValidTarget(LivingEntity target) {
        boolean valid = false;

        if (props[0].getValue() && (target instanceof Enemy || target instanceof Monster)) {
            valid = true;
        }

        if (props[1].getValue() && isAnimalLike(target)) {
            valid = true;
        }

        if (props[2].getValue() && target instanceof Player) {
            
            if (target.level() instanceof ServerLevel serverLevel && serverLevel.isPvpAllowed()) {
                valid = true;
            }
        }

        return valid;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
        if (task.getEntity() != null &&
                task.getEntity() instanceof LivingEntity target &&
                isValidTarget(target)) {

            if (golem instanceof Mob mob) {
                mob.setTarget(target);
            }
            golem.addRankXp(1);
        }
        task.setSuspended(true);
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return task.getEntity() == null || !golem.getGolemEntity().isAlliedTo(task.getEntity());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public Identifier getSealIcon() {
        return icon;
    }

    public int[] getGuiCategories() {
        return new int[]{2, 0, 4};
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.FIGHTER};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
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