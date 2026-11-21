# INDEV — состояние порта NeoForge 26.3

Ветка: `neoforge/26.3`. Ветки `neoforge/26.3-local` и `forge/1.20.1` не трогались.

## Что сделано

- Перенесены все файлы из `CHANGED_FILES_SINCE_84bdeae6.md` (891 запись) и адаптированы под NeoForge 26.3 / MC 26.3 / Java 25.
- Сборка и 409 юнит-тестов ранее проходили (`./gradlew.bat build`). После последних правок по запуску финальный прогон `build` был прерван — нужно повторить.
- Выделенный сервер запускается, создаёт мир и завершает common setup без ошибок.
- Клиент запускается через IDE-конфигурацию `Client`, входит в мир, JEI и рендеринг работают.
- Добавлены: crit/lifesteal API, story messages, debug logger, visnerite, portal scroll, crimson cult, тесты, новые рецепты/данные.
- Бонусы crit-экипировки подключены через `ItemAttributeModifierEvent` (замена удалённого `Item#getAttributeModifiers`).

## Что осталось доделать

### 1. Шейдеры и визуальные эффекты (главный остаток)
- Оригинальные эффекты использовали `ShaderInstance`/`RenderStateShard`/`GlStateManager`, которых нет в 26.x.
- MC 26.3 компилирует шейдеры в SPIR-V; legacy GLSL из 1.20.1 не компилируется (`'location' : SPIR-V requires location`, `non-opaque uniforms outside a block`).
- Сейчас `portal_wave`, `scan_ripple`, `player_glitch` рисуются через ванильные `RenderPipeline` (`GUI_TEXTURED`, `LIGHTNING`) — эффекты видны, но **нет настоящего искажения экрана**.
- Нужно написать SPIR-V-совместимые шейдеры + pipeline-ассеты и вернуть их в `ArcaneFxPipelines`.
- Post-эффекты（`blurtc`, `desaturatetc`, `hunger`, `sunscorned`, `teleport_warp`）могут работать не полностью: в логе клиента есть предупреждение `Requested post effect does not exist: minecraft:end_of_frame`.

### 2. Рендеринг предметов/брони
- Кастомные модели брони подключены через `client/ArmorClientSetup` (NeoForge `IClientItemExtensions`), но визуальная парность с Forge-версией не проверена в игре.
- Legacy-рендеры предметов (`EldritchPortalItemRenderer`, `HungryChestItemRenderer`, `NodeItemRenderer`, `NodeModelEvents`, `EldritchPortalModelEvents`, `WandMeshModel`) оставлены как заглушки; используются `SpecialModelRenderer`-реализации. Требуется визальная проверка.

### 3. Игровая проверка механик
- Сервер и клиент только запускались; механики (сканирование, порталы, големы, исследовательские столы, JEI, куриос-слоты, story messages) не прогонялись в геймплее.
- Требуется вручную проверить крафты, генерацию visnerite, работу триггеров и команды `/arcaneresearch`.

### 4. Данные и рецепты
- Кастомный ингредиент `arcaneresearch:crystal` в рецептах верстака заменён на тег `#arcaneresearch:clusters` (9 рецептов) — крафты стали менее строгими.
- `ore_visnerite` перенесён в реестр `worldgen/feature`, добавлен тип фичи; нужно проверить распределение руды в мире.
- Три placed feature перенаправлены на mod-фичи (`oak_bees_0002`, `fancy_oak_bees_0002`, `trees_birch_and_oak`).
- Из тега `curios:belt` удалён несуществующий `wand_focus_pouch`.

### 5. Технический долг
- Повторить `./gradlew.bat build --no-daemon --no-build-cache` после последних правок (рецепты, Item.Properties, worldgen, шейдеры).
- Mixin `compatibilityLevel: JAVA_21` при классах Java 25 — Mixin пишет предупреждения о версии класса; при появлении поддержки JAVA_25 обновить конфиги.
- JEI API помечен как deprecated (`RecipeType.create`, `addItemStack`, `getTooltip`) — мигрировать на новый API.
- Предупреждения ресурсов: отсутствует `arcaneresearch:item/wand_focus_pouch`, отсутствуют текстуры `models/node_converter` (`node_converter`, `node_converter_over`).
- `@OnlyIn` удалён из исходников, но warning `loadwarning.neoforge.onlyin` всё ещё появляется — выяснить источник (возможно, зависимости или stale-классы).
- В логе клиента есть warning `arcaneresearch.refmap.json could not be read` (для dev-окружения безвредно).

## Как проверять

```bat
cd D:\Table\BackupFile\Sync\development\Projects\Minecraft-mods\Arcane-Research-26.3
.\gradlew.bat build --no-daemon --no-build-cache --console=plain
.\gradlew.bat runServer --no-daemon --no-build-cache --console=plain
```

Клиент: IDE run configuration `Client`.
