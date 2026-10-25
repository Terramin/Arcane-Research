package arcaneresearch.common.entities.monster.cult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.init.ModEntities;
import arcaneresearch.init.ModItems;
public class EntityCultistKnight extends EntityCultist {
    public EntityCultistKnight(EntityType<? extends EntityCultistKnight> type, Level level) {
        super(type, level);
    }
    public EntityCultistKnight(Level level) {
        this(ModEntities.CULTIST_KNIGHT.get(), level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MAX_HEALTH, 30.0);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new arcaneresearch.common.entities.ai.combat.AICultistHurtByTarget(this, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityEldritchGuardian.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }
    @Override
    protected void setLoot(DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON_PLATE_HELM.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON_PLATE_CHEST.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON_PLATE_LEGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.CRIMSON_BOOTS.get()));
        float swordChance = (level().getDifficulty() == Difficulty.HARD) ? 0.05f : 0.01f;
        if (random.nextFloat() < swordChance) {
            int roll = random.nextInt(5);
            if (roll == 0) {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.VOID_SWORD.get()));
                setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON_ROBE_HELM.get()));
            } else {
                setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.THAUMIUM_SWORD.get()));
                if (random.nextBoolean()) {
                    setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
        } else {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CRIMSON_BLADE.get()));
        }
    }
    @Override
    protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {
        float f = difficulty.getSpecialMultiplier();
        ItemStack weapon = getMainHandItem();
        if (!weapon.isEmpty() && random.nextFloat() < 0.25f * f) {
            EnchantmentHelper.enchantItem(random, weapon, (int) (5.0f + f * random.nextInt(18)), false);
        }
    }
}
