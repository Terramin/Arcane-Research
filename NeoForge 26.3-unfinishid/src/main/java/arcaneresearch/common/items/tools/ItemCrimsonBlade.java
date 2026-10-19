package arcaneresearch.common.items.tools;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchToolMaterials;
import arcaneresearch.api.items.IWarpingGear;

import java.util.function.Consumer;

public class ItemCrimsonBlade extends Item implements IWarpingGear {

    // NEOFORGE_PORT: Forge Tier -> ToolMaterial record; canonical definition lives in
    // ArcaneresearchToolMaterials (values verbatim from Forge CRIMSON_VOID_TIER:
    // uses 200, speed 8.0, bonus 3.5, level 4, enchant 20, repair void ingot).
    // Field name kept so existing references keep resolving.
    public static final ToolMaterial CRIMSON_VOID_TIER = ArcaneresearchToolMaterials.TOOLMAT_CRIMSON_VOID;

    public ItemCrimsonBlade(Item.Properties props) {
        // NEOFORGE_PORT: SwordItem REMOVED in 26.x -> plain Item with .sword(material, damage, speed).
        super(props
                .rarity(Rarity.EPIC)
                .sword(CRIMSON_VOID_TIER, 3, -2.4F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_void)
    // -> REPAIRABLE component, so no override is needed (or possible).

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {

            boolean canApplyEffects = true;
            if (target instanceof Player && attacker instanceof Player) {
                if (target.level() instanceof net.minecraft.server.level.ServerLevel pvpLevel && !pvpLevel.isPvpAllowed()) {
                    canApplyEffects = false;
                }
            }

            if (canApplyEffects) {

                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("enchantment.special.sapgreat").withStyle(style -> style.withColor(0x8B0000)));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}