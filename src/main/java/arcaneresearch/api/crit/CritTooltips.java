package arcaneresearch.api.crit;

import arcaneresearch.api.items.ICriticalGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * EN: Centralised tooltip rendering for critical hit stats.
 * RU: Централизованный вывод строк статов крита в подсказках предметов.
 */
public final class CritTooltips {
    private CritTooltips() {
    }

    /**
     * EN: Gold-colored line with the crit chance bonus.
     * RU: Золотая строка с бонусом шанса крита.
     */
    public static Component chanceLine(double percent) {
        return Component.translatable("tooltip.arcaneresearch.crit_chance", (int) Math.round(percent))
                .withStyle(ChatFormatting.GOLD);
    }

    /**
     * EN: Gold-colored line with the crit damage bonus.
     * RU: Золотая строка с бонусом урона крита.
     */
    public static Component damageLine(double percent) {
        return Component.translatable("tooltip.arcaneresearch.crit_damage", (int) Math.round(percent))
                .withStyle(ChatFormatting.GOLD);
    }

    /**
     * EN: Adds the bonus lines of a held gear item to an item tooltip.
     * RU: Добавляет строки бонусов экипированного предмета в подсказку.
     */
    public static void addGearTooltip(ItemStack stack, List<Component> tooltip) {
        // EN: Do not advertise crit bonuses while the system is disabled in config.
        // RU: Не показываем бонусы крита, если система отключена в конфиге.
        if (!CritConfig.isEnabled()) {
            return;
        }
        // EN: Only items implementing ICriticalGear contribute crit tooltip lines.
        // RU: Только предметы, реализующие ICriticalGear, дают строки про крит.
        if (stack.getItem() instanceof ICriticalGear gear) {
            tooltip.add(chanceLine(gear.getCritChance(stack)));
            tooltip.add(damageLine(gear.getCritDamage(stack)));
        }
    }
}
