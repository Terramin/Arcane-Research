# discovered — порт свитков/записок и стола исследований (TC3 + reforged)

## Реализация (компилируется, живёт только в `discovered/`)

### `note/` — записка исследования, с нуля, в паритете с оригиналами
- `DiscoveredNoteData` — данные свитка: порт TC3 `ResearchNoteData`
  (tags/progress/failedTags, getTotalProgress, getTagProgress)
  + reforged `ResearchNoteData` (key/tags/progress/failed, read/create).
  Адаптация: NBT-тег `DiscoveredNote {key, tags[], need[], have[], fail{}}`
  вместо DataComponent; количества `need` хранятся в NBT (у ResearchEntry
  проекта нет аспект-стоимости).
- `ItemDiscoveredResearchNote` — предмет: порт TC3 `ItemResearchNotes`
  (стак 1, glint готовой, rarity, тултип, завершение на 100% на сервере)
  + reforged `ResearchNotesItem` (use/appendHoverText).
- `DiscoveredNotes` — хелперы: порт reforged ResearchManager
  (createNote/getData/updateData/getResearchSlot) + TC3
  (createResearchNoteForPlayer); завершение идёт через проектные
  `ResearchManager.completeResearch/doesPlayerHaveRequisites`,
  siblings — через `ResearchCategories.getResearch(key).getSiblings()`.
- `DiscoveredNoteSetup` — автоподключение без правок вне `discovered/`
  (`@Mod.EventBusSubscriber`): регистрация `research_notes` + `discovery`
  (тот же класс, transmute как в reforged),
  вкладка креатива, тинт иконки (паритет TC3-прохода + reforged
  `ResearchNoteTint` — цвет первого изученного аспекта).
- `DiscoveredNotes` — хелперы + `consumeInk/consumePaper` (паритет reforged),
  `toDiscovery`, `createNoteFromSupplies`.
- `table/DiscoveredResearchAspects` — точные ручные наборы всех 33 открытий
  (в духе reforged ConfigResearch; TC6 точных наборов не имеет).
  Запасной вариант — формула категории (`requiredAspects`, для ключей
  без исследования — категория требующего).
- Вкладка: 33 записки = 33 исследованиям с required_discovery (1:1),
  пустых нет.
- Мини-игра только под discovery-исследования (готовые, недоступные,
  потраченные/предъявленные и без требований — мимо); входы тратятся
  только при реальном прогрессе; текст книги до предъявления — шифр.
- Мёртвый код помечен `@Deprecated` (не удалён): `DiscoveredNotes.findNoteSlot`,
  `createNoteFromSupplies`, `consumeInk`, `consumePaper`,
  `DiscoveredResearchAspects.keys()`.
- `required_discovery` (API проекта): `ResearchStage.discovery` + парсинг
  (`#` срезается) + ряд в книге + серверная проверка/списание в пакете
  прогресса + `isStageAutoComplete`. Открытие = discovery-предмет с тем же
  NBT-ключом (стол выдаёт через `toDiscovery`); хелперы — `DiscoveredNotes`.
- Тексты на пергаменте: 6 ключей берут stage-тексты проекта, остальные 53 —
  `discoverednote.KEY.name/text` из lang (en+ru, порт name/longText из reforged
  research.xml); фолбэки — `DiscoveredNotes.researchName/researchText`.
- `client/DiscoveredNoteHandRenderer` — записка в руке «как карта», порт TC3
- `client/DiscoveredNoteText` — текст на пергаменте: резка по `<BR>` на абзацы,
  `<IMG>...</IMG>` вырезается (картинкам на пергаменте не место),
  перенос по ширине; низ НЕ режется — масштаб и ширина переноса
  подбираются совместно (shrink-to-fit: чем мельче текст, тем шире
  перенос, строки занимают всю ширину пергамента);
  используется рукой и попапом стола.
  `renderResearchNotes` + reforged `renderNotes` на `RenderHandEvent`
  (пергамент, заголовок с обфускацией при <0.2, сетка иконок через
  `aspect.getImage()`, `%` при >=0.333332, текст стадий шифром при <0.5).
  Руки — точный порт reforged `renderMapHand` (повороты 92/45/-41
  + прямой рендер рук, YP 90° для обеих); тело ванильного renderPlayerArm
  НЕ используется (давало двойную трансформацию).
- Используются существующие lang (`research_notes`+`discovery`,
  `tc.discoveryunknown/progress/error`, `tc.aspect.*`) и текстуры.

### Перенесённые ассеты (новые файлы, ничего не заменено)
- `textures/item/`: `research_notes_overlay.png`, `discovery.png`,
  `discovery_overlay.png` (из reforged)
- `textures/misc/`: `parchment.png`, `ss_research.png`, `ss_tags_1/2.png` (из TC3)
- `textures/gui/guiresearchtable.png` (стол записок, задел)
- `textures/models/`: `table.png`, `restable.png`, `worktable.png` (стол, задел)
- `textures/block/tablequill.png` (стол, задел)
- `models/item/`: `research_notes.json`, `discovery.json` (2 слоя, layer1 под тинт)
- lang `item.arcaneresearch.discovery` (en_us/ru_ru, добавлено, не заменено)

### Стол записок (`table/` + `client/`, мини-игра TC3/reforged) — ГОТОВО, двухблочный
- Двухблочная структура (паритет TC3 BlockTable main+сосед и reforged
  MAIN/SIDE): `PART {MAIN,SIDE}` + `FACING`, SIDE спереди от MAIN,
  установка ставит обе половины, `canSurvive`/`updateShape` следят за связью,
  слом одной половины сносит вторую без дропа (лут только с MAIN),
  клик с любой половины открывает меню MAIN, BE только на MAIN.
- Одиночный стол `table` (порт reforged TableBlock без wand-части):
  свойство AXIS, крафт из плит+досок, свой лут, таб, lang.
  Создание стола записок — паритет TC3 (research.xml: два стола рядом +
  клик Scribing tools): ПКМ письменными принадлежностями по одиночному
  столу с соседним столом превращает пару в note_table
  (кликнутый = MAIN, сосед = SIDE, -1 прочность чернил).
- Хитбокс 1:1 с JSON-моделью: верх/ножки/перекладина/чернильница
  раздельно для MAIN и SIDE, повороты по конвенции blockstate
  (y=90: +x→+z — доказано по ванильной лестнице из client.jar).
  Одиночный стол тоже с хитбоксом (верх+ножки+перекладина, AXIS).
  SIDE ставится сразу на обеих сторонах (предсказание, как двери).
- Отображение как в reforged (JSON-модели, БЕЗ BER): портированы
  `models/block/table.json`, `note_table.json` (верх 32x16 на 2 блока,
  ножки, перекладина, чернильница, пергамент, перо — как TC3
  TileResearchTableRenderer рисовал пергамент+перо поверх модели),
  `note_table_side.json` (пустая), `blockstates/table.json`,
  `blockstates/note_table.json`, `models/item/table.json`;
  текстуры `block/table_model|restable_model|worktable_model.png`,
  `block/woodplain|parchment|tablequill.png`.
  Важно: прозрачность (перо/пергамент) работает через cutout-слои,
  заданные кодом в `DiscoveredTableClient` (`ItemBlockRenderTypes`, конвенция
  проекта); ключ `render_type` в JSON оставлен как подсказка.
  Ориентация модели сверена с TC3 (md2-5 → повороты 270/90/180/0).
- GUI — полный порт reforged ResearchTableScreen: спрайт safe-тоггла,
  теги через проектный AspectRenderer (количество+бонус+димминг),
  круговая диаграмма с линиями нестабильности (`DiscoveredGuiLines`),
  тултипы диаграммы с процентами, кулдаун обеих кнопок, без лейблов.
- Мини-игра GUI (паритет GuiResearchTable): кнопка-спрайт 15x15 (241,0/15)
  с антидребезгом 150мс, `Cursory`/`Thorough` при наведении на зону (51,130),
  растущий попап-пергамент (0.05↔0.48, longText, шифр при <0.5),
  тултипы тегов (+failed), диаграмма с барами и `%` при >=0.333332.

## Референсы

Справочные копии `tc3_orig/` (`Thaumcraft3.0.3.zip.src`) и `reforged/`
(`thaumcraft-reforged`) удалены после завершения порта — остались только
реализация (`note/`, `table/`, `client/`) и этот README.

## Что портировано (свитки = research notes, research note, стол исследования)

Источники: TC3 (`ItemResearchNotes`, `ResearchNoteData`, `BlockTable`,
`ContainerResearchTable`, `GuiResearchTable`, `HandheldItemRenderer`) и
reforged (`ResearchNoteData`, `ResearchNotesItem`, `ResearchTableBlock`,
`ResearchTableMenu`, `ResearchTableScreen`, `HandheldItemRenderer`,
`ConfigResearch`).

## Правила
- Код — только в `src/main/java/arcaneresearch/discovered/` (реализация + референсы).
- Ассеты — только новые файлы и additive-строки lang, замен нет
  (см. git status: `??` + lang-добавления).
