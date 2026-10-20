package arcaneresearch.common.items.tools;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.init.ModItems;
import java.util.List;
public class ItemVoidSword extends SwordItem implements IWarpingGear {
    public ItemVoidSword() {
        super(ArcaneresearchMaterials.TOOLMAT_VOID, 3, -2.4F,
                new Item.Properties());
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
        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || !(attacker instanceof Player) || isPvPEnabled(attacker.level()))) {
            try {
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
            } catch (Exception ignored) {
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
    private boolean isPvPEnabled(Level level) {
        if (level.getServer() != null) {
            return level.getServer().isPvpAllowed();
        }
        return true;
    }
    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapless").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
