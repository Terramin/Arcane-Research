package arcaneresearch.client.renderers.item;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Порт {@code WandModelEvents} (Forge 1.20.1) в NeoForge 26.3.
 *
 * В 26.3 запекшиеся BakedModel-композиты жезла (rod+cap+sceptre+focus из
 * wand.obj/staff.obj через {@code ModelEvent.RegisterAdditional} /
 * {@code ModifyBakingResult}) и {@code RegisterColorHandlersEvent.Item}
 * УДАЛЕНЫ. Их 1:1-замены уже живут в других местах (поэтому этот класс —
 * только карта соответствия, подписок на события здесь нет):
 *
 * - registerWandPartModels + bakeWandModels (registerWandMesh: базовые
 *   wand/staff + варианты cap/rod/focus/sceptre, кэш WandMeshModel.cache,
 *   трансформы WandTransforms) -> {@link WandSpecialRenderer}
 *   (SpecialModelRenderer: слои rod/cap1/cap2/crossbar/focus из тех же
 *   wand.obj/staff.obj в raw corner-space координатах без сдвигов — слой
 *   уже применил display+T(-0.5), итог = Forge 1:1), регистрация
 *   кодеков — {@link SpecialItemModels}, display — модели
 *   assets/arcaneresearch/items/wand.json (minecraft:special);
 * - registerWandColors (tintIndex 1 = цвет фокуса TintHelper.wandFocusColor)
 *   -> цвет вершин focus-группы в WandSpecialRenderer (у кастомной
 *   геометрии нет tintIndex-пайплайна; контракт Forge tintIndex-1 сохранён).
 *
 * Блокер для полного паритета решателя кончика: класс WandTransforms
 * (Forge-константы WAND/STAFF как запасной путь, когда запечённая модель
 * недоступна) в 26.3 не портирован — WandTipHelper использует трансформы
 * запечённой модели, иначе eye+look.
 */
@OnlyIn(Dist.CLIENT)
public final class WandModelEvents {
    private WandModelEvents() {
    }
}
