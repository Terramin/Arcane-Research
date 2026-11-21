package arcaneresearch.api.crit;

import arcaneresearch.Arcaneresearch;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * EN: Attribute definitions, registered through {@link CritApi#init(net.neoforged.bus.api.IEventBus)}.
 * RU: Определения атрибутов, регистрируются через {@link CritApi#init(net.neoforged.bus.api.IEventBus)}.
 */
public final class CritAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Arcaneresearch.MODID);

    /** EN: Critical chance in percent: 0-100, default 5. RU: Шанс крита в процентах: 0-100, по умолчанию 5. */
    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE = ATTRIBUTES.register("crit_chance",
            () -> new RangedAttribute("attribute.arcaneresearch.crit_chance", 5.0D, 0.0D, 100.0D).setSyncable(true));

    /** EN: Critical damage multiplier in percent: 0-1000, default 150 (= x1.5). RU: Множитель урона крита. */
    public static final DeferredHolder<Attribute, Attribute> CRIT_DAMAGE = ATTRIBUTES.register("crit_damage",
            () -> new RangedAttribute("attribute.arcaneresearch.crit_damage", 150.0D, 0.0D, 1000.0D).setSyncable(true));

    private CritAttributes() {
    }

    /** EN: Adds both attributes to the player if they are not already present. RU: Добавляет атрибуты игроку. */
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        if (!event.has(EntityTypes.PLAYER, CRIT_CHANCE)) {
            event.add(EntityTypes.PLAYER, CRIT_CHANCE);
        }
        if (!event.has(EntityTypes.PLAYER, CRIT_DAMAGE)) {
            event.add(EntityTypes.PLAYER, CRIT_DAMAGE);
        }
    }
}
