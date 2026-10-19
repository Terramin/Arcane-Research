package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
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

import java.util.List;
import java.util.Random;

public class SealButcher implements ISeal, ISealGui, ISealConfigArea {

    private int delay;
    private boolean wait = false;
    private Identifier icon;

    public SealButcher() {
        delay = new Random(System.nanoTime()).nextInt(200);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_butcher");
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
        return "arcaneresearch:butcher";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % 200 != 0 || wait) {
            return;
        }

        AABB area = GolemHelper.getBoundsForArea(seal);
        List<LivingEntity> entities = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), area, e -> true);

        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (isValidTarget(target)) {
                    int count = 0;
                    for (LivingEntity other : entities) {
                        if (target.getClass().isInstance(other)) {
                            count++;
                            if (count > 2) break;
                        }
                    }

                    if (count > 2) {
                        Task task = new Task(seal.getSealPos(), target);
                        task.setPriority(seal.getPriority());
                        task.setLifespan((short) 10);
                        TaskHandler.addTask(level.dimension(), task);
                        wait = true;
                        break;
                    }
                }
            }
        }
    }

    private boolean isValidTarget(LivingEntity target) {
        if (!isAnimalLike(target)) {
            return false;
        }

        if (target instanceof Enemy) {
            return false;
        }

        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }

        if (target instanceof net.minecraft.world.entity.animal.golem.AbstractGolem) {
            return false;
        }

        if (target instanceof Animal animal && animal.isBaby()) {
            return false;
        }

        return true;
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
        wait = false;
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        task.setSuspended(true);
        wait = false;
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        return true;
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
        return new EnumGolemTrait[]{EnumGolemTrait.FIGHTER, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        wait = false;
    }

    @Override
    public void readCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt) {
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
        wait = false;
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }
}