package arcaneresearch.common.entities.monster;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;

public class EntityBrainyZombie extends Zombie {

    public EntityBrainyZombie(EntityType<? extends EntityBrainyZombie> type, Level level) {
        super(type, level);
    }

    public EntityBrainyZombie(Level level) {
        super(ModEntities.BRAINY_ZOMBIE.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel serverLevel, DamageSource source, boolean wasRecentlyHit) {
        // NEOFORGE_PORT: looting param removed in 26.x; derived from the killer's weapon
        // (Enchantments.LOOTING level), same value the old caller passed.
        int lootingLevel = arcaneresearch.common.lib.utils.EntityUtils.getLootingLevel(serverLevel, source);
        super.dropCustomDeathLoot(serverLevel, source, wasRecentlyHit);

        if (random.nextInt(10) - lootingLevel <= 4) {
            spawnAtLocation((net.minecraft.server.level.ServerLevel) level(), new ItemStack(ModItems.ZOMBIE_BRAIN.get()), 1.5f);
        }
    }

    @Override
    public int getArmorValue() {
        return super.getArmorValue() + 1;
    }
}
