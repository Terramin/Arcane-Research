# Thaumic Tinkerer → Arcane Research (Forge 1.20.1) — план портирования

Оригинал: `D:\Table\BackupFile\Sync\development\Projects\mods\thaumic-tinkerer-reforged` (NeoForge 26.3, modid `thaumictinkerer`)
Порт: `D:\Table\BackupFile\Sync\development\Projects\mods\Arcane-Research\main` (Forge 1.20.1, modid `thaumictinker`)

Легенда: `[x]` — портировано и проверено · `[~]` — портировано частично / с отличиями · `[ ]` — не портировано

---

## 0. Сводка

| Область | Оригинал | Порт | Статус |
|---|---|---|---|
| Фич-классы (21) | 21 | 21 | `[x]` |
| Зарегистрированные блоки/предметы | 89 id | 86 id | `[x]` (плюс 3 «лишних» — переименованы) |
| BlockEntity / Menu / Effect / Enchantment | 12 BE, 3 меню, 4 эффекта, 13 чар | те же | `[x]` |
| Исследования (research) | 53 записи, код | 74 записи, JSON | `[x]` (порт опережает) |
| Рецепты | 8 кодовых + ссылки на JSON | 121 JSON | `[x]` (порт опережает) |
| Loot tables | нет (в оригинале) | 18 | `[x]` |
| Модели / blockstates / текстуры | нет (в оригинале) | 34 + 127 + 163 | `[x]` |
| Сетевые пакеты | 13 | 10 | `[ ]` 3 пакета |
| Клиентские рендереры | 38 файлов | 20 файлов | `[ ]` 18 файлов |
| Конфиг | 8 переключателей | нет | `[ ]` |
| Gametests | 25 файлов | нет | `[ ]` |
| JEI | плагин | нет | `[~]` намеренно вырезан |

**Главный вывод:** каркас и контент портированы полностью; расходятся только «хвосты» — дата-компоненты 1.21, часть клиентских рендереров, 3 сетевых пакета, конфиг и 6 рецептов заражённых семян.

---

## 1. Ядро / регистрация

- [x] Свой `@Mod`-класс `thaumictinker.ThaumicTinker` (в оригинале — `ThaumicTinkerer`)
- [x] `mods.toml` аддона с зависимостью `arcaneresearch`
- [x] `TtCore` (modid `thaumictinker`, id-хелпер) — аналог `ThaumicTinkerer`
- [x] `TtFeatures.load()` — вызов всех 21 фич-классов
- [x] `TtRegistry` — единая точка регистрации (блоки, предметы, BE, меню, эффекты, чары, сущности, рецепты, креативная вкладка)
- [x] Регистрация только на шине аддона (`thaumictinker.TtCore.MODID`), а не `arcaneresearch`
- [x] Загрузка фич в событии `minecraft:block` (чтобы блоки базового мода уже резолвились)
- [x] Креативная вкладка `thaumictinker:tab` (в оригинале `thaumictinkerer:thaumictinkerer`)
- [x] `TtIntegration` — категория исследований `TT_CATEGORY` + `registerResearchLocation`
- [x] JEI удалён из `build.gradle` (намеренно)
- [ ] `registry/ModSounds.java` — в оригинале пустой DeferredRegister, переносить нечего

## 2. Фичи (по 21 оригинальной)

| Фича | Оригинальный файл | Порт | Статус |
|---|---|---|---|
| DarkQuartz | `darkquartz/DarkQuartzFeature` | `darkquartz/DarkQuartzFeature` | `[x]` |
| Decor (travel/ward) | `decor/DecorFeature` | `decor/DecorFeature` | `[x]` |
| Funnel | `funnel/FunnelFeature` | `funnel/FunnelFeature` | `[x]` |
| Gas | `gas/GasFeature` | `gas/GasFeature` | `[x]` |
| Mobilizer | `mobilizer/MobilizerFeature` | `mobilizer/MobilizerFeature` | `[x]` |
| Repairer | `repairer/RepairerFeature` | `repairer/RepairerFeature` | `[x]` |
| Transvector | `transvector/TransvectorFeature` | `transvector/TransvectorFeature` | `[x]` |
| Foci | `foci/FociFeature` | `foci/FociFeature` | `[x]` |
| Enchanting | `enchanting/EnchantingFeature` | `enchanting/EnchantingFeature` | `[x]` |
| Crops | `crops/CropsFeature` | `crops/CropsFeature` | `[~]` не хватает 6 рецептов семян (см. §4) |
| Elements | `elements/ElementsFeature` | `elements/ElementsFeature` | `[x]` |
| Attractor | `attractor/AttractorFeature` | `attractor/AttractorFeature` | `[x]` |
| Tablet | `tablet/TabletFeature` | `tablet/TabletFeature` | `[x]` |
| Gear | `gear/GearFeature` | `gear/GearFeature` | `[x]` |
| Necromancy | `necromancy/NecromancyFeature` | `necromancy/NecromancyFeature` | `[~]` нет механики `last_tablet` (см. §4) |
| Kami | `kami/KamiFeature` | `kami/KamiFeature` | `[x]` |
| AwakenedTools | `kami/tool/AwakenedToolsFeature` | `kami/tool/AwakenedToolsFeature` | `[x]` |
| AwakenedArmor | `awakenedarmor/AwakenedArmorFeature` | `awakenedarmor/AwakenedArmorFeature` | `[x]` |
| Trinkets | `kami/trinkets/TrinketsFeature` | `kami/trinkets/TrinketsFeature` | `[x]` |
| Gateway | `kami/gateway/GatewayFeature` | `kami/gateway/GatewayFeature` | `[x]` |
| KamiFoci | `kami/foci/KamiFociFeature` | `kami/foci/KamiFociFeature` | `[x]` |

## 3. Предметы (поимённо)

### 3.1 Блоки (34) — все зарегистрированы
- [x] `dark_quartz_block`, `chiseled_dark_quartz_block`, `dark_quartz_pillar`, `dark_quartz_slab`, `dark_quartz_stairs`
- [x] `travel_slab`, `travel_stairs`, `ward_slab`
- [x] `essentia_funnel`
- [x] `gaseous_light`, `gaseous_shadow`, `nitor_gas`
- [x] `levitational_locomotive`, `levitational_locomotive_relay`
- [x] `thaumic_restorer`
- [x] `transvector_interface`, `transvector_dislocator`, `ethereal_platform`
- [x] `focus_flight`, `focus_dislocation`, `focus_telekinesis`, `focus_smelt`, `focus_heal`, `focus_ender_chest`, `focus_deflect`
- [x] `osmotic_enchanter`, `obsidian_totem`
- [x] `infused_farmland`, `infused_grain_crop`
- [x] `aer_imbued_fire`, `aqua_imbued_fire`, `ordo_imbued_fire`, `perditio_imbued_fire`, `terra_imbued_fire`, `ignis_imbued_fire`, `forcefield`
- [x] `kinetic_attractor`, `corporeal_attractor`
- [x] `dynamism_tablet`, `necromancy_tablet`, `warp_gate`

### 3.2 Предметы (60+) — все зарегистрированы
- [x] `dark_quartz` · [x] `infused_seeds` (+ варианты 6 аспектов) · [x] `infused_inkwell`
- [x] `soul_mould` · [x] `focus_*` (7 базовых + `focus_recall`, `focus_shadowbeam`, `focus_xp_drain`)
- [x] `spellbinding_cloth` · [x] `share_tome` · [x] `blood_sword` · [x] `cleansing_talisman` · [x] `xp_talisman` · [x] `revealing_helm`
- [x] `ichor`, `ichor_cloth`, `ichorium_ingot`, `ichorium_nugget`, `ichorium_cap`, `ichorcloth_rod`, `ichorcloth_cowl/robe/leggings/boots`, `ichorium_pickaxe/shovel/axe/sword`
- [x] `awakened_ichorium_pickaxe/shovel/axe/sword` · [x] `cowl_of_the_abyssal_depths`, `robes_of_the_stratosphere`, `leggings_of_the_burning_mantle`, `boots_of_the_horizontal_shield`
- [x] `gaseous_illuminae`, `gaseous_tenebrae`, `bright_nitor`, `fume_dissipator`
- [x] `block_talisman`, `placement_mirror`, `protoclay`, `ichor_pouch`, `cat_amulet`
- [x] `sky_pearl`, `transvector_binder`, `nether_shard`, `ender_shard`
- [x] `aer/aqua/ordo/perditio/terra/ignis_imbued_fire` (+предметы-бутылки `*_infused_potion`, `*_infused_grain`)
- [x] 17 наборов «душных аспектов» × 3 формы (base/condensed/infused) = 51 предмет
  - `aqua`, `humanus`, `aer`, `volatus`, `ignis`, `praecantatio`, `exanimis`, `victus`, `bestia`, `vitium`, `terra`, `alienis`, `motus`, `metallum`, `gelum`, `tenebrae`, `herba`
  - `[~]` имена переименованы под аспекты базового мода (corpus→victus, venenum→vitium, iter→motus, limus→gelum, pannus→tenebrae, messis→herba) — осознанная адаптация

### 3.3 Прочее
- [x] 4 эффекта: `air_imbued`, `water_imbued`, `earth_imbued`, `fire_imbued`
- [x] 13 чар: `ascent_boost`, `auto_smelt`, `desintegrate`, `dispersed_strike`, `final_strike`, `focused_strike`, `pounce`, `quick_draw`, `shatter`, `shockwave`, `slow_fall`, `tunnel`, `valiance`
- [x] 1 entity type: `shadowbeam_staff_beam`
- [x] 2 BlockEntityType: `warded_slab`, `kinetic_attractor`/`corporeal_attractor`, `necromancy_tablet`, `osmotic_enchanter`, `infused_farmland`, `infused_grain_crop`, `funnel`, `transvector_*`, `dynamism_tablet`, `warp_gate`
- [x] 3 MenuType: `corporeal_attractor`, `osmotic_enchanter`, `ichor_pouch`
- [x] 2 RecipeSerializer: `spell_cloth`, `share_tome`

---

## 4. Недоделки в логике (P0 — ломает геймплей)

- [ ] **6 рецептов заражённых семян** (`CropsFeature.recipes()` в оригинале).
  Инфузия `wheat_seeds` + 4 осколка стихии → `infused_seeds` **с записанным аспектом**
  (aer / ignis / terra / aqua / ordo / perditio) и пустыми tendencies.
  В оригинале — `TcStack.withData(...)`; в порте нужен JSON-рецепт с `"nbt"` в `result`
  (`{"SeedAspect":"aer","CustomModelData":1}`).
  Без этого цепочка «семена → урожай» не работает.
- [ ] **Механика `last_tablet`** (`NecromancyTabletBlockEntity`): помеченный «педиктал» должен
  помнить позицию таблиточки (`LAST_TABLET`), чтобы душу нельзя было призвать на двух
  табличках сразу. Оригинал: `NecromancyFeature.LAST_TABLET.set(marked, worldPosition)`
  + `matches(stack)` в проверке. В порте отсутствует.
- [ ] **Исследование `INFUSED_INKWELL`** — есть в оригинале (`GearFeature.INFUSED_INKWELL_RESEARCH`),
  отсутствует в `assets/thaumictinker/research/tt_research.json`.

---

## 5. Платформенные слои (1.21 → 1.20.1)

Оригинал использует NeoForge 1.21 дата-компоненты (`TtStackKey` / `TtAttachments`).
Порт переводит их на NBT — соответствие проверено:

| Компонент оригинала | Порт | Статус |
|---|---|---|
| `soul_pattern` | NBT `thaumictinker:soul_pattern` | `[x]` |
| `gear_active` | NBT `thaumictinker:active` | `[x]` |
| `talisman_xp` | NBT `thaumictinker:xp` | `[x]` |
| `share_data` | NBT `ShareData` | `[x]` |
| `seed_aspect` / `seed_tendencies` | NBT `SeedAspect` / `SeedTendencies` | `[x]` |
| `talisman_block` / `talisman_count` / `ring_active` | NBT `TalismanBlock`/`TalismanCount`/`RingActive` | `[x]` |
| `mirror_block` / `mirror_size` | NBT `MirrorBlock`/`MirrorSize` | `[x]` |
| `pearl_location` | NBT в `SkyPearlItem` | `[x]` |
| `binder_origin` | NBT `dim`/`x`/`y`/`z` | `[x]` |
| `awakened_armor_disabled` | NBT `PIECE_DISABLED_TAG` | `[x]` |
| `dislocated_block` | NBT `HELD_TAG` | `[x]` |
| `last_tablet` | — | `[ ]` см. §4 |
| `TtConsumables.drinkable` (цвет/эффекты зелья) | `InfusedPotionItem` без цвета | `[ ]` |

---

## 6. Сеть

- [x] `FociEffectPayload`, `CropFxPayload`, `ElementsFxPayload`, `ShadowbeamPayload`
- [x] `EnchanterAddEnchant` → `EnchanterEditPayload`, `EnchanterStartWorking` → `EnchanterStartPayload`
- [x] `MobMagnetButtonPayload`, `TabletButtonPayload`
- [x] `WarpGateButtonPayload`, `WarpGateTeleportPayload`
- [ ] `ArmorStatusPayload` — статус комплекта пробуждённой брони
- [ ] `ToggleArmorPayload` — переключение отдельной части брони
- [ ] `GatewayFxPayload` — FX портала warp gate

---

## 7. Клиент

- [x] `client/attractor/*` (AttractorClient, AttractorItemRenderer, AttractorRenderer, AttractorRendering, CorporealAttractorScreen, AttractorGeometry)
- [x] `client/enchanting/*` (EnchanterRenderer, EnchanterScreen, EnchantingClient)
- [x] `client/gateway/*` (GatewayClient, WarpGateScreen, WarpGateRenderer, WarpGateDestinationsScreen, PearlInfo)
- [x] `client/tablet/*` (TabletClient, DynamismTabletRenderer, DynamismTabletScreen, TabletGeometry, TabletSink)
- [x] `client/trinkets/*` (TrinketsClient, MirrorOverlay, IchorPouchScreen)
- [x] `client/kami/KamiFociClient` · [x] `client/geom/SpinningCubesGeometry`
- [ ] `client/awakenedarmor/AwakenedArmorClient` + `WingsLayer` + `geom/WingsGeometry` — слой крыльев
- [ ] `client/camo/CamoBlockModel` + `CamoModels` — модель камуфляжного блока
- [ ] `client/crops/CropsClient` + `CropAspectTint` + `SeedAspectTint` — тонировка по аспекту
- [ ] `client/elements/ElementsClientEffects` — клиентские эффекты стихий
- [ ] `client/foci/FociClientEffects` — клиентские эффекты фокусов
- [ ] `client/funnel/FunnelRenderer` — рендер воронки эссенции
- [ ] `client/gas/GasClientFx` — клиентские эффекты газов
- [ ] `client/kami/AwakenedToolsClient` + `ShadowFx` — рендер пробуждённых инструментов
- [ ] `client/repairer/RepairerItemRenderer` + `RepairerRenderer` + `RepairerRendering` + `geom/RepairerGeometry`
- [ ] `client/geom/GhostBlockGeometry` + `ModelQuadSink` — рендер «призрачного» блока
- [ ] `client/platform/TtGuiBlit` + `TtGuiTint` + `ModelVertexSink` + `TtClientNetwork` — GUI-хелперы
- [ ] `client/jei/ThaumicTinkererJeiPlugin` + `WandCategory` — JEI (намеренно вырезан)

---

## 8. Конфиг

Оригинал: `Config.java` (NeoForge `ModConfigSpec`), 8 булевых переключателей.
Порт: конфига нет.

- [ ] `Config.java` + регистрация в `ThaumicTinker` (Forge `ModConfigSpec`)
- [ ] `kami_enabled` (`general.kami.forceenabled`)
- [ ] `imbued_fire` (`general.imbuedFire.enabled`)
- [ ] `imbued_fire_cake` (`general.imbuedFire.cake.enabled`)
- [ ] `share_tome_survival` (`general.shareTome.survival.enabled`)
- [ ] `crops_allow_bonemeal` (`general.cropsAllowBonemeal.enabled`)
- [ ] `mod_flight` (`general.modFlight.enabled`)
- [ ] `placement_mirror_blocks` (`general.placementMirror.blocks.show`)
- [ ] `tooltip_indicators` (`general.tooltipIndicators.enabled`)
- [ ] lang-ключи `thaumictinker.configuration.*` (8 + 8 `.tooltip`)

---

## 9. Ресурсы

- [x] Модели: 34 blockstate + 127 block + 163 item — самосогласованно (0 битых ссылок)
- [x] Текстуры: 278 файлов, все ссылки из моделей резолвятся
- [x] Loot tables: 18
- [x] Рецепты: 121 JSON (в оригинале рецепты кодовые — порт опережает)
- [x] Исследования: `assets/thaumictinker/research/tt_research.json` (74 записи)
- [ ] 4 иконки эффектов: `textures/mob_effect/{air,earth,fire,water}_imbued.png`
- [ ] 3 текстуры GUI оригинала без аналога в порте: `gui/aspectanalyzer.png`, `gui/potioneffects.png`, `gui/remoteplacer.png`, `gui/soulhearts.png`
- [ ] Текстуры оригинала, не имеющие аналога в порте (фичи 1.12, не портированы): `block/aspectanalyzer*`, `block/golemconnector`, `block/portal*`, `block/remoteplacer*`, `block/mobilization_levitator`, `block/emptytexture`
- [ ] `misc/repair.png`, `misc/repairoff.png` — индикаторы ремонта (связано с `tooltip_indicators`)
- [ ] `model/ichor1-2.png`, `model/ichorgem1-2.png`, `model/repairer.png` — декоративные
- [ ] lang: `item.Wand.ICHOR.cap`, `item.Wand.ICHORCLOTH.rod` (названия грифа/навершия)
- [ ] lang: `key.category.thaumictinker.main` (категория биндов)
- [ ] lang: префикс `ttinkerer.research_text.*` / `ttinkerer.research_text_kami.*` (альтернативный, в порте используется `tc.research_text.*`)
- [ ] lang: `tc.research_page.*` для `MAGNETS.2`, `PERIPHERALS.1`, `ASPECT_ANALYZER.1`, `GOLEM_CONNECTOR.1-4` (последние три — фичи 1.12, не портируются)
- [~] lang: `jei.thaumictinker.*` (3 ключа) — JEI вырезан

---

## 10. Тесты

- [ ] 25 файлов `test/*GameTests.java` из оригинала (gametest-ы Forge 1.20.1)
- [ ] `test/CoreGameTests.java` + `test/TtGameTestSuite.java` — точка входа

---

## 11. Известные отличия (осознанные, не чинить)

- `thaumictinkerer:thaumictinkerer` (id вкладки) → `thaumictinker:tab`
- Дата-компоненты NeoForge 1.21 → NBT 1.20.1
- `DeferredRegister`/`DeferredBlock`/`DeferredItem` (NeoForge) → `TtRegistry` с ручным `RegisterEvent` (Forge)
- Кодовые рецепты `TtRecipes` → JSON-рецепты базового мода
- Кодовые исследования `TtResearch`/`TtResearchItem` → JSON `tt_research.json`
- `Aspect.combine` → `util/AspectCombination` (через `Aspect.mixList`)
- `Config` (NeoForge `ModConfigSpec`) → ещё не перенесён (§8)
- JEI → вырезан
