package arcaneresearch.common.items.tools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.init.ModItems;
import javax.annotation.Nullable;
import java.util.List;
public class ItemCrimsonBlade extends SwordItem implements IWarpingGear {
    public static final Tier CRIMSON_VOID_TIER = new Tier() {
        @Override
        public int getUses() {
            return 200;
        }
        @Override
        public float getSpeed() {
            return 8.0f;
        }
        @Override
        public float getAttackDamageBonus() {
            return 3.5f;
        }
        @Override
        public int getLevel() {
            return 4;
        }
        @Override
        public int getEnchantmentValue() {
            return 20;
        }
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VOID_METAL_INGOT.get());
        }
    };
    public ItemCrimsonBlade() {
        super(CRIMSON_VOID_TIER,
                3,
                -2.4f,
                new Item.Properties()
                        .rarity(Rarity.EPIC));
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            boolean canApplyEffects = true;
            if (target instanceof Player && attacker instanceof Player) {
                if (target.level().getServer() != null && !target.level().getServer().isPvpAllowed()) {
                    canApplyEffects = false;
                }
            }
            if (canApplyEffects) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapgreat").withStyle(style -> style.withColor(0x8B0000)));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
