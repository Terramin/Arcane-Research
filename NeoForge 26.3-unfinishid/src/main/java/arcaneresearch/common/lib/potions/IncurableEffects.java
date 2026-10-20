package arcaneresearch.common.lib.potions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NEOFORGE_PORT: Forge {@code MobEffectInstance.getCurativeItems().clear()} made an effect immune
 * to milk. NeoForge 26.3 dropped curative items; milk is the {@code clear_all_effects} consume
 * effect, which removes each effect through a cancellable {@link MobEffectEvent.Remove}.
 * Effects applied through {@link #addIncurable} are remembered on the entity and that removal
 * is cancelled while the entity is drinking such an item. Other removals (expiry, /effect clear)
 * still work, as with Forge's empty curative list.
 */
public final class IncurableEffects {

    /** Effect ids currently applied as incurable on this entity. */
    public static final class Data {
        public static final MapCodec<Data> CODEC = Identifier.CODEC.listOf()
                .optionalFieldOf("effects", List.of())
                .xmap(Data::new, data -> new ArrayList<>(data.effects));

        final Set<Identifier> effects;

        public Data() {
            this(List.of());
        }

        private Data(List<Identifier> effects) {
            this.effects = new HashSet<>(effects);
        }
    }

    private IncurableEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(IncurableEffects.class);
    }

    /** Same as {@code target.addEffect(effect)}, but milk will not remove the resulting effect. */
    public static boolean addIncurable(LivingEntity target, MobEffectInstance effect) {
        boolean added = target.addEffect(effect);
        if (target.hasEffect(effect.getEffect())) {
            effect.getEffect().unwrapKey().ifPresent(key ->
                    target.getData(ArcaneresearchCapabilities.INCURABLE_EFFECTS.get()).effects.add(key.identifier()));
        }
        return added;
    }

    private static boolean isMarked(LivingEntity entity, Holder<MobEffect> effect) {
        if (!entity.hasData(ArcaneresearchCapabilities.INCURABLE_EFFECTS.get())) {
            return false;
        }
        return effect.unwrapKey()
                .map(key -> entity.getData(ArcaneresearchCapabilities.INCURABLE_EFFECTS.get()).effects.contains(key.identifier()))
                .orElse(false);
    }

    private static void unmark(LivingEntity entity, Holder<MobEffect> effect) {
        if (entity.hasData(ArcaneresearchCapabilities.INCURABLE_EFFECTS.get())) {
            effect.unwrapKey().ifPresent(key ->
                    entity.getData(ArcaneresearchCapabilities.INCURABLE_EFFECTS.get()).effects.remove(key.identifier()));
        }
    }

    private static boolean isDrinkingCure(LivingEntity entity) {
        if (!entity.isUsingItem()) {
            return false;
        }
        ItemStack use = entity.getUseItem();
        Consumable consumable = use.get(DataComponents.CONSUMABLE);
        return consumable != null && consumable.onConsumeEffects().stream()
                .anyMatch(e -> e instanceof ClearAllStatusEffectsConsumeEffect);
    }

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (!isMarked(entity, event.getEffect())) {
            return;
        }
        if (isDrinkingCure(entity)) {
            event.setCanceled(true);
        } else {
            unmark(entity, event.getEffect());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null) {
            unmark(event.getEntity(), event.getEffectInstance().getEffect());
        }
    }
}
