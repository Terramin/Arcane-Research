package arcaneresearch.api.crit;

import arcaneresearch.Arcaneresearch;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * EN: Attribute definitions, registered through {@link CritApi#init(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext)}.
 * RU: Определения атрибутов, регистрируются через {@link CritApi#init(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext)}.
 */
public final class CritAttributes {
    // EN: Deferred register for the two custom attributes.
    // RU: Отложенная регистрация для двух кастомных атрибутов.
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Arcaneresearch.MODID);

    /**
     * EN: Critical chance in percent: 0-100, default 5.
     * RU: Шанс критического удара в процентах: 0-100, по умолчанию 5.
     */
    public static final RegistryObject<Attribute> CRIT_CHANCE = ATTRIBUTES.register("crit_chance",
            () -> new RangedAttribute("attribute.arcaneresearch.crit_chance", 5.0D, 0.0D, 100.0D).setSyncable(true));

    /**
     * EN: Critical damage multiplier in percent: 0-1000, default 150 (= x1.5).
     * RU: Множитель урона крита в процентах: 0-1000, по умолчанию 150 (= x1.5).
     */
    public static final RegistryObject<Attribute> CRIT_DAMAGE = ATTRIBUTES.register("crit_damage",
            () -> new RangedAttribute("attribute.arcaneresearch.crit_damage", 150.0D, 0.0D, 1000.0D).setSyncable(true));

    private CritAttributes() {
    }

    /**
     * EN: Adds both attributes to the player if they are not already present.
     * RU: Добавляет оба атрибута игроку, если их ещё нет.
     */
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, CRIT_CHANCE.get())) {
            event.add(EntityType.PLAYER, CRIT_CHANCE.get());
        }
        if (!event.has(EntityType.PLAYER, CRIT_DAMAGE.get())) {
            event.add(EntityType.PLAYER, CRIT_DAMAGE.get());
        }
    }
}
