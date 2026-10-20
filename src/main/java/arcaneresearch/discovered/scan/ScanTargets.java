package arcaneresearch.discovered.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.helper.RegsHelper;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.internal.CommonInternals;
import arcaneresearch.api.research.IScanEventHandler;
import arcaneresearch.api.research.ScanResult;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.utils.EntityUtils;

/**
 * Цели изучения и их аспекты. Порт {@code ItemThaumometer.doScan} +
 * {@code ScanManager.getScanAspects/generateEntityAspects/validScan}
 * (TC 4.2.3.5) на API проекта (только чтение, без правок).
 *
 * Порядок: сущность (9 блоков) -&gt; узел ауры -&gt; блок (9 блоков) -&gt;
 * явления аддонов. Дальность 9 — как у проектного таумометра
 * (линза показывает то же самое).
 *
 * NEOFORGE_PORT: ResourceLocation -&gt; Identifier; BlockState
 * getCloneItemStack(hit, level, pos, viewer) удалён -&gt;
 * state.getCloneItemStack(level, pos, true) (тот же вызов, что
 * TileResearchTable); saveWithoutId(CompoundTag) удалён -&gt;
 * saveWithoutId(TagValueOutput) со снапшотом в CompoundTag (та же shape,
 * что ScanEntity.checkThing); getTagType(name) -&gt; get(name).getId().
 */
public final class ScanTargets {

    /** Дальность изучения (паритет проектного SCAN_RANGE). */
    public static final double RANGE = 9.0D;

    private ScanTargets() {
    }

    public enum Kind {
        NONE, BLOCK, ENTITY, NODE
    }

    /** Цель под прицелом: позиция блока/узла, сущность или явление. */
    public static final class Target {
        public final Kind kind;
        public final BlockPos pos;
        public final Entity entity;
        /** Узел: {@code "NODE"+id} (паритет {@code ScanResult.phenomena}). */
        public final String phenomena;

        public Target(Kind kind, BlockPos pos, Entity entity, String phenomena) {
            this.kind = kind;
            this.pos = pos;
            this.entity = entity;
            this.phenomena = phenomena;
        }

        /** Ключ стабильности цели (срыв изучения при смене — паритет startScan). */
        public String steadyKey() {
            switch (kind) {
                case BLOCK:
                    return "B:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
                case NODE:
                    return "N:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
                case ENTITY:
                    return "E:" + entity.getId();
                default:
                    return "";
            }
        }
    }

    /** Прицел: сущность важнее узла, узел важнее блока (паритет {@code ItemThaumometer.doScan}). */
    public static Target resolve(Level level, Player player) {
        Entity pointed = EntityUtils.getPointedEntity(level, player, 1.0D, RANGE, 0.0F, true);
        if (pointed != null) {
            return new Target(Kind.ENTITY, null, pointed, "");
        }
        BlockHitResult hit = blockRay(level, player, RANGE);
        if (hit.getType() == HitResult.Type.BLOCK && !level.isEmptyBlock(hit.getBlockPos())) {
            // Узел ауры — явление, а не блок (паритет ветки INode).
            try {
                if (level.getBlockEntity(hit.getBlockPos()) instanceof INode node) {
                    String id = "";
                    try {
                        id = node.getId();
                    } catch (Exception ignored) {
                    }
                    return new Target(Kind.NODE, hit.getBlockPos(), null,
                            "NODE" + (id == null ? "" : id));
                }
            } catch (Exception ignored) {
            }
            // Собранная пара — мебель без предмета-подбора: не изучаем
            // (иначе каждое удержание спамило бы "Unknown object").
            try {
                if (level.getBlockState(hit.getBlockPos()).getBlock()
                        instanceof arcaneresearch.discovered.table.HexNoteTableBlock) {
                    return new Target(Kind.NONE, null, null, "");
                }
            } catch (Exception ignored) {
            }
            return new Target(Kind.BLOCK, hit.getBlockPos(), null, "");
        }
        // Явления аддонов — только когда прицел пуст (паритет doScan:252-256).
        Target phenomena = scanPhenomena(level, player);
        if (phenomena != null) {
            return phenomena;
        }
        return new Target(Kind.NONE, null, null, "");
    }

    /**
     * Явления аддонов ({@code ArcaneresearchApi.scanEventhandlers}).
     * Адаптация {@code ScanResult}: сущность — прямо, узел по id
     * без реестра координат неразрешим (ядро узлы находит само).
     */
    private static Target scanPhenomena(Level level, Player player) {
        ItemStack held;
        try {
            held = holdingThaumometer(player) ? player.getMainHandItem() : ItemStack.EMPTY;
            if (held.isEmpty() || held.getItem() != arcaneresearch.init.ModItems.THAUMOMETER.get()) {
                held = player.getOffhandItem();
            }
        } catch (Exception e) {
            held = ItemStack.EMPTY;
        }
        try {
            for (IScanEventHandler handler : ArcaneresearchApi.scanEventhandlers) {
                if (handler == null) {
                    continue;
                }
                ScanResult result;
                try {
                    result = handler.scanPhenomena(held, level, player);
                } catch (Exception e) {
                    continue;
                }
                if (result == null) {
                    continue;
                }
                if (result.type == 2 && result.entity != null && result.entity.isAlive()) {
                    return new Target(Kind.ENTITY, null, result.entity, "");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static BlockHitResult blockRay(Level level, Player player, double range) {
        return EntityUtils.blockRay(level, player, range);
    }

    /**
     * Ключ «уже изучено» (паритет {@code generateItemHash} + хэш сущности).
     * Блок — по предмету-подбору, узел — измерение+позиция, лежащий
     * предмет — как объект (общий список со scannedObjects, паритет
     * ветки EntityItem), сущность — по id типа.
     */
    public static String scannedKey(Level level, Target target, Player viewer) {
        if (target.kind == Kind.BLOCK) {
            ItemStack pick = pickStack(level, target.pos, viewer);
            if (!pick.isEmpty()) {
                Identifier id = RegsHelper.itemKey(pick.getItem());
                if (id != null) {
                    return "B:" + id;
                }
            }
            Identifier blockId = RegsHelper.blockKey(
                    level.getBlockState(target.pos).getBlock());
            return "B:" + blockId;
        }
        if (target.kind == Kind.NODE) {
            String dim = "";
            try {
                dim = level.dimension().identifier().toString();
            } catch (Exception ignored) {
            }
            return "N:" + dim + ":" + target.pos.getX() + "," + target.pos.getY()
                    + "," + target.pos.getZ();
        }
        if (target.kind == Kind.ENTITY) {
            if (target.entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
                try {
                    ItemStack stack = item.getItem();
                    if (!stack.isEmpty()) {
                        Identifier id = RegsHelper.itemKey(stack.getItem());
                        if (id != null) {
                            return "B:" + id;
                        }
                    }
                } catch (Exception ignored) {
                }
                return "B:empty";
            }
            Identifier id = RegsHelper.entityKey(target.entity.getType());
            return "E:" + id;
        }
        return "";
    }

    /** Предмет-подбор блока (паритет {@code getPickBlock/createStackedBlock}). */
    public static ItemStack pickStack(Level level, BlockPos pos, Player viewer) {
        try {
            BlockState state = level.getBlockState(pos);
            // NEOFORGE_PORT (26.3): player-aware NeoForge hook (parity with Forge
            // getCloneItemStack(hit, level, pos, viewer) -> IForgeBlock override path).
            ItemStack stack = state.getCloneItemStack(pos, level, true, viewer);
            if (stack != null && !stack.isEmpty()) {
                return stack;
            }
        } catch (Exception ignored) {
        }
        try {
            return new ItemStack(level.getBlockState(pos).getBlock().asItem());
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Аспекты цели (паритет {@code ScanManager.getScanAspects}):
     * блок — теги объекта, узел — {@link #nodeAspects}, лежащий предмет —
     * теги стака (паритет ветки EntityItem), сущность — реестр
     * {@code scanEntities} + NBT.
     */
    public static AspectList aspectsFor(Level level, Target target, Player viewer) {
        if (target.kind == Kind.BLOCK) {
            try {
                ItemStack pick = pickStack(level, target.pos, viewer);
                if (!pick.isEmpty()) {
                    AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods
                            .getObjectAspects(pick);
                    if (tags != null && tags.size() > 0) {
                        return tags;
                    }
                }
            } catch (Exception ignored) {
            }
            return null;
        }
        if (target.kind == Kind.NODE) {
            return nodeAspects(level, target.pos);
        }
        if (target.kind == Kind.ENTITY) {
            if (target.entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
                try {
                    ItemStack stack = item.getItem();
                    if (!stack.isEmpty()) {
                        ItemStack one = stack.copy();
                        one.setCount(1);
                        AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods
                                .getObjectAspects(one);
                        if (tags != null && tags.size() > 0) {
                            return tags;
                        }
                    }
                } catch (Exception ignored) {
                }
                return null;
            }
            return entityAspects(target.entity);
        }
        return null;
    }

    /**
     * Порт {@code ScanManager.generateNodeAspects}: каждый аспект узла —
     * {@code max(4, amount/10)} + бонус по типу узла. Подстановки набора
     * TC6 (нет HUNGER/TAINT/HEAL): голод и порча — FLUX 4, чистота —
     * LIFE 2 + ORDER 2.
     */
    public static AspectList nodeAspects(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        try {
            if (!(level.getBlockEntity(pos) instanceof INode node)) {
                return null;
            }
            AspectList base = null;
            try {
                base = node.getAspects();
            } catch (Exception e) {
                base = null;
            }
            AspectList tags = new AspectList();
            if (base != null) {
                for (Aspect aspect : base.getAspectsSortedByName()) {
                    if (aspect == null) {
                        continue;
                    }
                    try {
                        tags.merge(aspect, Math.max(4, base.getAmount(aspect) / 10));
                    } catch (Exception ignored) {
                    }
                }
            }
            try {
                switch (node.getNodeType()) {
                    case UNSTABLE:
                        if (Aspect.ENTROPY != null) {
                            tags.merge(Aspect.ENTROPY, 4);
                        }
                        break;
                    case HUNGRY:
                        if (Aspect.FLUX != null) {
                            tags.merge(Aspect.FLUX, 4);
                        }
                        break;
                    case TAINTED:
                        if (Aspect.FLUX != null) {
                            tags.merge(Aspect.FLUX, 4);
                        }
                        break;
                    case PURE:
                        if (Aspect.LIFE != null) {
                            tags.merge(Aspect.LIFE, 2);
                        }
                        if (Aspect.ORDER != null) {
                            tags.add(Aspect.ORDER, 2);
                        }
                        break;
                    case DARK:
                        if (Aspect.DEATH != null) {
                            tags.merge(Aspect.DEATH, 2);
                        }
                        if (Aspect.DARKNESS != null) {
                            tags.add(Aspect.DARKNESS, 2);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ignored) {
            }
            return tags.size() > 0 ? tags : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Порт {@code ScanManager.generateEntityAspects}: реестр
     * {@code CommonInternals.scanEntities} ({@code entityName} — id типа),
     * NBT-совпадение — как {@code ScanEntity.checkThing}.
     * Пасхалки с никами и случайные аспекты игрокам не портированы:
     * игрок даёт MAN 4 (паритет ветки EntityPlayer без особых ников — ядро).
     */
    public static AspectList entityAspects(Entity entity) {
        if (entity == null) {
            return null;
        }
        try {
            if (entity instanceof Player) {
                AspectList tags = new AspectList();
                if (Aspect.MAN != null) {
                    tags.add(Aspect.MAN, 4);
                }
                return tags.size() > 0 ? tags : null;
            }
            Identifier id = RegsHelper.entityKey(entity.getType());
            String name = id == null ? "" : id.toString();
            for (ArcaneresearchApi.EntityTags et : CommonInternals.scanEntities) {
                if (et == null || !et.entityName.equals(name)) {
                    continue;
                }
                if (et.nbts == null || et.nbts.length == 0) {
                    return et.aspects;
                }
                // NEOFORGE_PORT (26.3): with registry context, so registry-backed fields
                // (equipment/components) are written like Forge saveWithoutId(CompoundTag).
                net.minecraft.world.level.storage.TagValueOutput tvo =
                        net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
                entity.saveWithoutId(tvo);
                CompoundTag tc = tvo.buildResult();
                boolean match = true;
                for (ArcaneresearchApi.EntityTagsNBT nbt : et.nbts) {
                    if (!tc.contains(nbt.name) || !ArcaneresearchApiHelper.getNBTDataFromId(
                            tc, tc.get(nbt.name).getId(), nbt.name).equals(nbt.value)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return et.aspects;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Можно ли выдать (паритет {@code ScanManager.validScan}): у всех
     * составных открыты оба родителя. Иначе клиент получил бы
     * {@code tc.discoveryerror}.
     */
    public static boolean validScan(Player player, AspectList aspects) {
        if (player == null || aspects == null) {
            return false;
        }
        try {
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null || aspect.isPrimal()) {
                    continue;
                }
                Aspect[] comp = aspect.getComponents();
                if (comp == null || comp.length < 2 || comp[0] == null || comp[1] == null) {
                    return false;
                }
                if (!known(player, comp[0]) || !known(player, comp[1])) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /** Проверка знания аспекта (делегировано — единый гейт мини-игр). */
    public static boolean known(Player player, Aspect aspect) {
        try {
            return arcaneresearch.discovered.table.HexResearchGame.isKnown(player, aspect);
        } catch (Exception e) {
            return false;
        }
    }

    /** Таумометр в руках (главная или вторая рука). Общая проверка клиента и сервера. */
    public static boolean holdingThaumometer(Player player) {
        if (player == null) {
            return false;
        }
        try {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            return main.getItem() == arcaneresearch.init.ModItems.THAUMOMETER.get()
                    || off.getItem() == arcaneresearch.init.ModItems.THAUMOMETER.get();
        } catch (Exception e) {
            return false;
        }
    }
}
