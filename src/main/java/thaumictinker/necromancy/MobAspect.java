package thaumictinker.necromancy;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.entities.monster.EntityBrainyZombie;
import arcaneresearch.common.entities.monster.EntityFireBat;
import arcaneresearch.common.entities.monster.EntityWisp;
import arcaneresearch.init.ModEntities;

 



 
public record MobAspect(String name, Class<? extends Entity> type, Supplier<EntityType<?>> entityType, List<Aspect> aspects) {
    public static final List<MobAspect> ALL = List.of(
            entry("SnowMan", SnowGolem.class, () -> net.minecraft.world.entity.EntityType.SNOW_GOLEM, Aspect.WATER, Aspect.WATER, Aspect.MAN),
            entry("Bat", Bat.class, () -> net.minecraft.world.entity.EntityType.BAT, Aspect.AIR, Aspect.AIR, Aspect.FLIGHT),
            entry("Blaze", Blaze.class, () -> net.minecraft.world.entity.EntityType.BLAZE, Aspect.FIRE, Aspect.FIRE, Aspect.FIRE),
            entry("BrainyZombie", EntityBrainyZombie.class, ModEntities.BRAINY_ZOMBIE::get, Aspect.MAGIC, Aspect.UNDEAD, Aspect.LIFE),
            entry("Firebat", EntityFireBat.class, ModEntities.FIRE_BAT::get, Aspect.FLIGHT, Aspect.FIRE, Aspect.MAGIC),
            entry("CaveSpider", CaveSpider.class, () -> net.minecraft.world.entity.EntityType.CAVE_SPIDER, Aspect.BEAST, Aspect.FLUX, Aspect.FLUX),
            entry("Chicken", Chicken.class, () -> net.minecraft.world.entity.EntityType.CHICKEN, Aspect.PLANT, Aspect.FLIGHT, Aspect.BEAST),
            entry("Cow", Cow.class, () -> net.minecraft.world.entity.EntityType.COW, Aspect.BEAST, Aspect.EARTH, Aspect.BEAST),
            entry("Creeper", Creeper.class, () -> net.minecraft.world.entity.EntityType.CREEPER, Aspect.MAGIC, Aspect.BEAST, Aspect.ELDRITCH),
            entry("Enderman", EnderMan.class, () -> net.minecraft.world.entity.EntityType.ENDERMAN, Aspect.ELDRITCH, Aspect.ELDRITCH, Aspect.MAN),
            entry("Ghast", Ghast.class, () -> net.minecraft.world.entity.EntityType.GHAST, Aspect.FIRE, Aspect.FLIGHT, Aspect.FLIGHT),
            entry("EntityHorse", AbstractHorse.class, () -> net.minecraft.world.entity.EntityType.HORSE, Aspect.BEAST, Aspect.BEAST, Aspect.MOTION),
            entry("VillagerGolem", IronGolem.class, () -> net.minecraft.world.entity.EntityType.IRON_GOLEM, Aspect.METAL, Aspect.METAL, Aspect.MAN),
            entry("LavaSlime", MagmaCube.class, () -> net.minecraft.world.entity.EntityType.MAGMA_CUBE, Aspect.FIRE, Aspect.COLD, Aspect.COLD),
            entry("MushroomCow", MushroomCow.class, () -> net.minecraft.world.entity.EntityType.MOOSHROOM, Aspect.BEAST, Aspect.EARTH, Aspect.PLANT),
            entry("Ozelot", Ocelot.class, () -> net.minecraft.world.entity.EntityType.OCELOT, Aspect.BEAST, Aspect.EARTH, Aspect.ELDRITCH),
            entry("Pig", Pig.class, () -> net.minecraft.world.entity.EntityType.PIG, Aspect.BEAST, Aspect.EARTH, Aspect.MOTION),
            entry("PigZombie", ZombifiedPiglin.class, () -> net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN, Aspect.UNDEAD, Aspect.LIFE, Aspect.FIRE),
            entry("Sheep", Sheep.class, () -> net.minecraft.world.entity.EntityType.SHEEP, Aspect.EARTH, Aspect.DARKNESS, Aspect.BEAST),
            entry("Silverfish", Silverfish.class, () -> net.minecraft.world.entity.EntityType.SILVERFISH, Aspect.METAL, Aspect.METAL, Aspect.EARTH),
            entry("Skeleton", AbstractSkeleton.class, () -> net.minecraft.world.entity.EntityType.SKELETON, Aspect.UNDEAD, Aspect.MAN, Aspect.UNDEAD),
            entry("Slime", Slime.class, () -> net.minecraft.world.entity.EntityType.SLIME, Aspect.COLD, Aspect.COLD, Aspect.BEAST),
            entry("Spider", Spider.class, () -> net.minecraft.world.entity.EntityType.SPIDER, Aspect.BEAST, Aspect.UNDEAD, Aspect.UNDEAD),
            entry("Squid", Squid.class, () -> net.minecraft.world.entity.EntityType.SQUID, Aspect.WATER, Aspect.WATER, Aspect.WATER),
            entry("Villager", Villager.class, () -> net.minecraft.world.entity.EntityType.VILLAGER, Aspect.MAN, Aspect.MAN, Aspect.MAN),
            entry("Wisp", EntityWisp.class, ModEntities.WISP::get, Aspect.AIR, Aspect.MAGIC, Aspect.MAGIC),
            entry("Witch", Witch.class, () -> net.minecraft.world.entity.EntityType.WITCH, Aspect.MAGIC, Aspect.UNDEAD, Aspect.ELDRITCH),
            entry("Wolf", Wolf.class, () -> net.minecraft.world.entity.EntityType.WOLF, Aspect.BEAST, Aspect.BEAST, Aspect.BEAST),
            entry("Zombie", Zombie.class, () -> net.minecraft.world.entity.EntityType.ZOMBIE, Aspect.LIFE, Aspect.LIFE, Aspect.UNDEAD)
    );

    private static MobAspect entry(String name, Class<? extends Entity> type, Supplier<EntityType<?>> entityType, Aspect... aspects) {
        return new MobAspect(name, type, entityType, List.of(aspects));
    }

    public static @Nullable MobAspect forEntity(Entity entity) {
        for (MobAspect mob : ALL) {
            if (mob.type().isInstance(entity)) {
                return mob;
            }
        }
        return null;
    }

    public static @Nullable MobAspect forAspects(List<Aspect> aspects) {
        for (MobAspect mob : ALL) {
            if (mob.aspects().containsAll(aspects) && aspects.containsAll(mob.aspects())) {
                return mob;
            }
        }
        return null;
    }
}


