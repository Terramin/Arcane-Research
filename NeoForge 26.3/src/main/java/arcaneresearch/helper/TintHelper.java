package arcaneresearch.helper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

/**
 * Чистые резолверы цвета тинта (порт из Forge 1.20.1 1:1).
 *
 * <p>Возврат — RGB без альфы; вызов из {@code ItemTintSource.calculate} —
 * через {@link #opaque} (тинты 26.3 — ARGB, без альфы слой невидим).
 * {@link #NONE} ({@code -1}) в источники не возвращать: у каждого слоя
 * свой источник.</p>
 *
 * <p>Уже разобрано в данные: phial/label/jar/nitor/candle/banner/crystal
 * (см. {@code *TintSources}, {@code Arcaneresearch.onRegisterItemColors}).
 * Остальное — источник цвета для будущих {@code ItemTintSource}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class TintHelper {

    /** Белый по умолчанию. */
    public static final int WHITE = 0xFFFFFF;

    /**
     * «Тинт не применяется». Только для Forge-семантики;
     * в {@code ItemTintSource} не использовать.
     */
    public static final int NONE = -1;

    private TintHelper() {
    }

    /** RGB -&gt; ARGB для {@code ItemTintSource}. */
    public static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    /** Первый аспект фиала (слой 1), иначе белый. */
    public static int phialAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.consumables.ItemPhial) {
            try {
                AspectList aspects = ((arcaneresearch.common.items.consumables.ItemPhial)
                        stack.getItem()).getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Первый аспект банки (слой 1), иначе белый. */
    public static int jarAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.blocks.essentia.BlockJarItem jarItem) {
            try {
                AspectList aspects = jarItem.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Первый аспект кристалла эссенции (любой слой), иначе белый. */
    public static int crystalEssenceAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.resources.ItemCrystalEssence crystal) {
            try {
                AspectList aspects = crystal.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Аспект ярлыка (слой 1), иначе белый. */
    public static int labelAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.consumables.ItemLabel label) {
            try {
                Aspect aspect = label.getAspect(stack);
                if (aspect != null) {
                    return aspect.getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Цвет карты свечи по блоку, иначе белый. */
    public static int candleMapColor(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && blockItem.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockCandle candle) {
            try {
                return candle.getColor().getMapColor().col;
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /**
     * Мудровая эссенция: цвет первого аспекта из NBT; без NBT — переливание
     * по всем аспектам каждые 0.5 с (порт {@code ItemWispEssence.func_82790_a}).
     */
    public static int wispEssenceAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.resources.ItemWispEssence) {
            try {
                AspectList aspects =
                        ((arcaneresearch.common.items.resources.ItemWispEssence) stack.getItem())
                                .getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
            try {
                Object[] all = Aspect.aspects.values().toArray();
                if (all.length > 0) {
                    int idx = (int) (System.currentTimeMillis() / 500L % all.length);
                    return ((Aspect) all[idx]).getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /**
     * Знамя: слой 1 — краска, слой 2 — аспект из NBT (фолбэк краска),
     * иначе белый. Порт лямбды {@code Arcaneresearch.onRegisterItemColors}.
     */
    public static int bannerColor(ItemStack stack, int tintIndex) {
        net.minecraft.world.item.DyeColor dye = null;
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && blockItem.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockBannerTC banner) {
            try {
                dye = banner.getDyeColor();
            } catch (Exception ignored) {
            }
        }
        int dyeColor = dye != null ? dye.getFireworkColor()
                : net.minecraft.world.level.material.MapColor.COLOR_RED.col;
        if (tintIndex == 1) {
            return dyeColor;
        }
        if (tintIndex == 2) {
            // NEOFORGE_PORT (26.x): ItemStack.getTag() removed -> custom NBT via
            // ArcaneresearchInvHelper.getCustomTag (same root tag shape as Forge).
            net.minecraft.nbt.CompoundTag tag = stack != null
                    ? arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack) : null;
            if (tag != null && tag.contains("aspect")) {
                try {
                    Aspect aspect = Aspect.getAspect(tag.getStringOr("aspect", ""));
                    if (aspect != null) {
                        return aspect.getColor();
                    }
                } catch (Exception ignored) {
                }
            }
            return dyeColor;
        }
        return WHITE;
    }

    /** Кастер: цвет фокуса в слоте (слои &gt; 0), иначе {@link #NONE}. */
    public static int casterFocusColor(ItemStack stack, int tintIndex) {
        if (tintIndex > 0 && stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.casters.ItemCaster caster) {
            try {
                ItemStack focusStack = caster.getFocusStack(stack);
                if (caster.getFocus(stack) instanceof arcaneresearch.common.items.casters.ItemFocus focus
                        && focusStack != null) {
                    return focus.getFocusColor(focusStack);
                }
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

    /** Фокус: собственный цвет, иначе белый. */
    public static int focusColor(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.casters.ItemFocus focus) {
            try {
                return focus.getFocusColor(stack);
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Жезл: цвет фокуса в слоте (слой 1), иначе {@link #NONE}. */
    public static int wandFocusColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.api.wands.IWand wand) {
            try {
                arcaneresearch.api.wands.ItemFocusBasic focus = wand.getFocus(stack);
                if (focus != null) {
                    return focus.getFocusColor(wand.getFocusStack(stack));
                }
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

    /** Установщик големов: цвет материала из {@code props}, иначе белый. */
    public static int golemMaterialColor(ItemStack stack) {
        // NEOFORGE_PORT (26.x): ItemStack.getTag() removed -> custom NBT via
        // ArcaneresearchInvHelper.getCustomTag (same root tag shape as Forge).
        net.minecraft.nbt.CompoundTag tag = stack != null
                ? arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack) : null;
        if (tag != null && tag.contains("props")) {
            try {
                arcaneresearch.api.golems.IGolemProperties props =
                        arcaneresearch.common.golems.GolemProperties.fromLong(
                                tag.getLongOr("props", 0L));
                if (props != null && props.getMaterial() != null) {
                    return props.getMaterial().itemColor;
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /**
     * Красящаяся броня: слой 0 — цвет, остальные — {@link #NONE}.
     * Порт лямбды {@code Arcaneresearch.onRegisterItemColors}.
     */
    public static int dyeableColor(ItemStack stack, int tintIndex) {
        // NEOFORGE_PORT (26.x): DyeableLeatherItem removed -> dyed color is the
        // DYED_COLOR data component (DyedItemColor), same as ItemRobeArmor.
        if (stack != null && !stack.isEmpty()
                && stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR)) {
            try {
                return tintIndex > 0 ? NONE : net.minecraft.world.item.component.DyedItemColor
                        .getOrDefault(stack, 0xFFFFFF);
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

    /** Нитор-блок: собственное значение цвета, иначе белый. */
    public static int nitorBlockColor(net.minecraft.world.level.block.state.BlockState state) {
        if (state != null && state.getBlock()
                instanceof arcaneresearch.common.blocks.misc.BlockNitor nitor) {
            try {
                return nitor.getColorValue();
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /** Инкрустация: цвет заряда на слое 0, иначе белый. */
    public static int inlayChargeColor(net.minecraft.world.level.block.state.BlockState state, int tintIndex) {
        if (tintIndex == 0 && state != null
                && state.getBlock() instanceof arcaneresearch.common.blocks.devices.BlockInlay
                && state.hasProperty(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE)) {
            try {
                return arcaneresearch.common.blocks.devices.BlockInlay.colorMultiplier(
                        state.getValue(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE));
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

    /**
     * Записка исследования (слой 1): цвет первого изученного аспекта,
     * иначе первого требуемого, иначе белый. Зеркало
     * {@code DiscoveredNoteSetup.noteColor}; вызывать из
     * {@code ItemTintSource} через {@link #opaque}.
     */
    public static int researchNoteColor(ItemStack stack, int tintIndex,
            arcaneresearch.discovered.note.DiscoveredNoteData data) {
        if (tintIndex != 1 || stack == null
                || !(stack.getItem() instanceof arcaneresearch.discovered.note.ItemDiscoveredResearchNote)) {
            return WHITE;
        }
        if (data == null) {
            return WHITE;
        }
        try {
            for (int i = 0; i < data.tags.length; i++) {
                Aspect aspect = data.tags[i];
                int got = i < data.have.length ? data.have[i] : 0;
                if (aspect != null && got > 0) {
                    return aspect.getColor();
                }
            }
            if (data.tags.length > 0 && data.tags[0] != null) {
                return data.tags[0].getColor();
            }
        } catch (Exception ignored) {
        }
        return WHITE;
    }
}
