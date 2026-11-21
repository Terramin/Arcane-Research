package arcaneresearch.api.crit;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * EN: Common config of the critical hit API.
 * RU: Общий конфиг API критического удара.
 */
public final class CritConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * EN: Master switch. When false, no critical rolls happen and no critical hits are applied.
     * RU: Главный переключатель. При false броски крита не выполняются и криты не наносятся.
     */
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment(
                    "EN: Enable the critical hit system (default: true).",
                    "RU: Включить систему критического удара (по умолчанию: true).",
                    "EN: Set to false to completely disable crit chance and crit damage.",
                    "RU: Установите false, чтобы полностью отключить шанс и урон крита."
            )
            .define("enabled", true);

    /** EN: Built spec. RU: Готовая спецификация. */
    public static final ModConfigSpec SPEC = BUILDER.build();

    private CritConfig() {
    }

    /** EN: Current value of the master switch. RU: Текущее значение главного переключателя. */
    public static boolean isEnabled() {
        return ENABLED.get();
    }
}
