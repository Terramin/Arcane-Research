package arcaneresearch.api.crit;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * EN: Common config of the critical hit API (file: config/arcaneresearch-crit.toml).
 * RU: Общий конфиг API критического удара (файл: config/arcaneresearch-crit.toml).
 */
public final class CritConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /**
     * EN: Master switch. When false, no critical rolls happen and no critical hits are applied.
     * RU: Главный переключатель. При false броски крита не выполняются и криты не наносятся.
     */
    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment(
                    "EN: Enable the critical hit system (default: true).",
                    "RU: Включить систему критического удара (по умолчанию: true).",
                    "EN: Set to false to completely disable crit chance and crit damage.",
                    "RU: Установите false, чтобы полностью отключить шанс и урон крита."
            )
            .define("enabled", true);

    /** EN: Built spec, registered from CritApi.init(). RU: Готовая спецификация, регистрируемая в CritApi.init(). */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private CritConfig() {
    }

    /**
     * EN: Current value of the master switch. Forge updates it on config reload.
     * RU: Текущее значение главного переключателя. Forge обновляет его при перезагрузке конфига.
     */
    public static boolean isEnabled() {
        return ENABLED.get();
    }
}
