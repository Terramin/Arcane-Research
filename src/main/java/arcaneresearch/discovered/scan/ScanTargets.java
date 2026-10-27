package arcaneresearch.discovered.scan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Objects;

public final class ScanTargets {
    public static final double RANGE = 9.0D;

    private ScanTargets() {
    }

    public enum Kind {
        NONE, BLOCK, ENTITY, NODE
    }

    public static final class Target {
        public final Kind kind;
        public final net.minecraft.core.BlockPos pos;
        public final Entity entity;
        public final String phenomena;
        private final String key;

        Target(Kind kind, net.minecraft.core.BlockPos pos, Entity entity, String phenomena) {
            this.kind = kind;
            this.pos = pos;
            this.entity = entity;
            this.phenomena = phenomena;
            this.key = computeKey(kind, pos, entity);
        }

        private static String computeKey(Kind kind, net.minecraft.core.BlockPos pos, Entity entity) {
            if (kind == null) {
                return "";
            }
            return switch (kind) {
                case BLOCK -> pos == null ? "" : "B:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
                case NODE -> pos == null ? "" : "N:" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
                case ENTITY -> entity == null ? "" : "E:" + entity.getId();
                default -> "";
            };
        }

        public String steadyKey() {
            return key;
        }
    }

    public static Target resolve(Level level, Player player) {
        if (level == null || player == null) {
            return new Target(Kind.NONE, null, null, "");
        }
        Entity pointed = EntityUtils.getPointedEntity(level, player, 1.0D, RANGE, 0.0F, true);
        if (pointed != null) {
            return new Target(Kind.ENTITY, null, pointed, "");
        }
        BlockHitResult hit = blockRay(level, player, RANGE);
        if (hit.getType() == HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos hitPos = hit.getBlockPos();
            if (!level.isEmptyBlock(hitPos)) {
                try {
                    if (level.getBlockEntity(hitPos) instanceof INode node) {
                        String id = "";
                        try {
                            id = node.getId();
                        } catch (Exception ignored) {
                        }
                        return new Target(Kind.NODE, hitPos, null,
                                "NODE" + (id == null ? "" : id));
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (level.getBlockState(hitPos).getBlock()
                            instanceof arcaneresearch.discovered.table.HexNoteTableBlock) {
                        return new Target(Kind.NONE, null, null, "");
                    }
                } catch (Exception ignored) {
                }
                return new Target(Kind.BLOCK, hitPos, null, "");
            }
        }
        Target phenomena = scanPhenomena(level, player);
        if (phenomena != null) {
            return phenomena;
        }
        return new Target(Kind.NONE, null, null, "");
    }

    private static Target scanPhenomena(Level level, Player player) {
        if (level == null || player == null) {
            return null;
        }
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
            java.util.List<IScanEventHandler> handlers = ArcaneresearchApi.scanEventhandlers;
            for (IScanEventHandler handler : handlers) {
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
        if (level == null || player == null) {
            return null;
        }
        return EntityUtils.blockRay(level, player, range);
    }

    public static String scannedKey(Level level, Target target, Player viewer) {
        if (level == null || target == null) {
            return "";
        }
        if (target.kind == Kind.BLOCK && target.pos != null) {
            ItemStack pick = pickStack(level, target.pos, viewer);
            if (pick != null && !pick.isEmpty()) {
                ResourceLocation id = RegsHelper.itemKey(pick.getItem());
                if (id != null) {
                    return "B:" + id;
                }
            }
            BlockState state = level.getBlockState(target.pos);
            ResourceLocation blockId = RegsHelper.blockKey(state.getBlock());
            return blockId == null ? "" : "B:" + blockId;
        }
        if (target.kind == Kind.NODE && target.pos != null) {
            String dim = "";
            try {
                dim = level.dimension().location().toString();
            } catch (Exception ignored) {
            }
            return "N:" + dim + ":" + target.pos.getX() + "," + target.pos.getY()
                    + "," + target.pos.getZ();
        }
        if (target.kind == Kind.ENTITY && target.entity != null) {
            if (target.entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
                try {
                    ItemStack stack = item.getItem();
                    if (!stack.isEmpty()) {
                        ResourceLocation id = RegsHelper.itemKey(stack.getItem());
                        if (id != null) {
                            return "B:" + id;
                        }
                    }
                } catch (Exception ignored) {
                }
                return "B:empty";
            }
            ResourceLocation id = RegsHelper.entityKey(target.entity.getType());
            return id == null ? "" : "E:" + id;
        }
        return "";
    }

    public static ItemStack pickStack(Level level, net.minecraft.core.BlockPos pos, Player viewer) {
        if (level == null || pos == null) {
            return ItemStack.EMPTY;
        }
        try {
            BlockState state = level.getBlockState(pos);
            BlockHitResult hit = blockRay(level, viewer, RANGE);
            if (hit != null) {
                ItemStack stack = state.getCloneItemStack(hit, level, pos, viewer);
                if (stack != null && !stack.isEmpty()) {
                    return stack;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            return new ItemStack(level.getBlockState(pos).getBlock().asItem());
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    public static AspectList aspectsFor(Level level, Target target, Player viewer) {
        if (level == null || target == null) {
            return null;
        }
        if (target.kind == Kind.BLOCK && target.pos != null) {
            try {
                ItemStack pick = pickStack(level, target.pos, viewer);
                if (pick != null && !pick.isEmpty()) {
                    AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods
                            .getObjectAspects(pick);
                    if (tags != null && !tags.aspects.isEmpty()) {
                        return tags;
                    }
                }
            } catch (Exception ignored) {
            }
            return null;
        }
        if (target.kind == Kind.NODE && target.pos != null) {
            return nodeAspects(level, target.pos);
        }
        if (target.kind == Kind.ENTITY && target.entity != null) {
            if (target.entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
                try {
                    ItemStack stack = item.getItem();
                    if (!stack.isEmpty()) {
                        ItemStack one = stack.copy();
                        one.setCount(1);
                        AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods
                                .getObjectAspects(one);
                        if (tags != null && !tags.aspects.isEmpty()) {
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

    public static AspectList nodeAspects(Level level, net.minecraft.core.BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        try {
            if (!(level.getBlockEntity(pos) instanceof INode node)) {
                return null;
            }
            AspectList base;
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
                Object type = node.getNodeType();
                if (type != null) {
                    switch (type.toString()) {
                        case "UNSTABLE":
                            tags.merge(Aspect.ENTROPY, 4);
                            break;
                        case "HUNGRY":
                            tags.merge(Aspect.FLUX, 4);
                            break;
                        case "TAINTED":
                            tags.merge(Aspect.FLUX, 4);
                            break;
                        case "PURE":
                            tags.merge(Aspect.LIFE, 2);
                            tags.add(Aspect.ORDER, 2);
                            break;
                        case "DARK":
                            tags.merge(Aspect.DEATH, 2);
                            tags.add(Aspect.DARKNESS, 2);
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception ignored) {
            }
            return tags.aspects.isEmpty() ? null : tags;
        } catch (Exception e) {
            return null;
        }
    }

    public static AspectList entityAspects(Entity entity) {
        if (entity == null) {
            return null;
        }
        try {
            if (entity instanceof Player) {
                AspectList tags = new AspectList();
                tags.add(Aspect.MAN, 4);
                return tags.aspects.isEmpty() ? null : tags;
            }
            ResourceLocation id = RegsHelper.entityKey(entity.getType());
            String name = id == null ? "" : id.toString();
            java.util.List<ArcaneresearchApi.EntityTags> scanEntities = CommonInternals.scanEntities;
            if (scanEntities == null) {
                return null;
            }
            for (ArcaneresearchApi.EntityTags et : scanEntities) {
                if (et == null || et.entityName == null || !et.entityName.equals(name)) {
                    continue;
                }
                if (et.nbts == null || et.nbts.length == 0) {
                    return et.aspects;
                }
                CompoundTag tc = new CompoundTag();
                entity.saveWithoutId(tc);
                boolean match = true;
                for (ArcaneresearchApi.EntityTagsNBT nbt : et.nbts) {
                    if (!tc.contains(nbt.name) || !Objects.equals(ArcaneresearchApiHelper.getNBTDataFromId(
                            tc, tc.getTagType(nbt.name), nbt.name), nbt.value)) {
                        match = false;
                        break;
                    }
                }
                if (match) return et.aspects;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean validScan(Player player, AspectList aspects) {
        if (player == null || aspects == null) {
            return false;
        }
        try {
            Aspect[] list = aspects.getAspects();
            if (list == null) {
                return false;
            }
            for (Aspect aspect : list) {
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

    public static boolean known(Player player, Aspect aspect) {
        try {
            return arcaneresearch.discovered.table.HexResearchGame.isKnown(player, aspect);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean holdingThaumometer(Player player) {
        if (player == null) {
            return false;
        }
        try {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            var thaum = arcaneresearch.init.ModItems.THAUMOMETER.get();
            return main.getItem() == thaum || off.getItem() == thaum;
        } catch (Exception e) {
            return false;
        }
    }
}