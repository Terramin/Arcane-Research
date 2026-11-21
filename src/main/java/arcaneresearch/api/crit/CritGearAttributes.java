package arcaneresearch.api.crit;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.ICriticalGear;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Applies the crit bonuses of {@link ICriticalGear} items through NeoForge's
 * attribute event. Forge 1.20.1 used {@code Item#getAttributeModifiers}, which
 * no longer exists in 26.x; {@link ItemAttributeModifierEvent} is the
 * supported replacement and is fired for every attribute query.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class CritGearAttributes {

    private static final Identifier CHANCE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crit_gear_chance");
    private static final Identifier DAMAGE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crit_gear_damage");

    private CritGearAttributes() {
    }

    @SubscribeEvent
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        if (!CritConfig.isEnabled()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ICriticalGear gear)) {
            return;
        }
        double chance = gear.getCritChance(stack);
        double damage = gear.getCritDamage(stack);
        if (chance > 0.0D) {
            event.addModifier(CritAttributes.CRIT_CHANCE,
                    new AttributeModifier(CHANCE_ID, chance, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (damage > 0.0D) {
            event.addModifier(CritAttributes.CRIT_DAMAGE,
                    new AttributeModifier(DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
    }
}
