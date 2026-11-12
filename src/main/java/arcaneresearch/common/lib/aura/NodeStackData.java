package arcaneresearch.common.lib.aura;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.tiles.aura.TileJarNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NodeStackData {
    private static final NodeType[] TYPES = NodeType.values();
    private static final NodeModifier[] MODIFIERS = NodeModifier.values();

    private NodeStackData() {
    }

    public static ItemStack create(Item item, NodeType type, NodeModifier modifier,
                                   AspectList aspects) {
        return create(item, type, modifier, aspects, aspects);
    }

    public static ItemStack create(Item item, NodeType type, NodeModifier modifier,
                                   AspectList aspects, AspectList base) {
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        if (stack.isEmpty()) {
            return stack;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (aspects != null) {
            aspects.writeToNBT(tag, "Aspects");
        }
        if (base != null) {
            base.writeToNBT(tag, "AspectsBase");
        }
        if (type != null) {
            tag.putByte("nodetype", (byte) type.ordinal());
        }
        if (modifier != null) {
            tag.putByte("nodemod", (byte) modifier.ordinal());
        }
        return stack;
    }

    public static ItemStack create(Item item, NodeType type, NodeModifier modifier,
                                   AspectList aspects, AspectList base, String nodeId) {
        ItemStack stack = create(item, type, modifier, aspects, base);
        if (stack.isEmpty()) {
            return stack;
        }
        if (nodeId != null && !nodeId.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.putString("nodeId", nodeId);
            }
        }
        return stack;
    }

    public static void applyTo(@NotNull TileJarNode tile,@NotNull CompoundTag tag) {
        if (tag.contains("Aspects")) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(tag, "Aspects");
            tile.setAspects(aspects);
        }
        if (tag.contains("AspectsBase")) {
            AspectList base = new AspectList();
            base.readFromNBT(tag, "AspectsBase");
            syncBase(tile, base);
        }
        tile.setNodeType(readType(tag));
        NodeModifier mod = readModifier(tag);
        tile.setNodeModifier(mod);
        if (tag.contains("nodeId")) {
            String id = tag.getString("nodeId");
            tile.setId(id);
        } else if (tag.contains("nodeid")) {
            String id = tag.getString("nodeid");
            tile.setId(id);
        }
    }

    public static void applyTo(AuraNodeTile tile, CompoundTag tag) {
        if (tile == null || tag == null) {
            return;
        }
        if (tag.contains("Aspects")) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(tag, "Aspects");
            tile.setAspects(aspects);
        }
        if (tag.contains("AspectsBase")) {
            AspectList base = new AspectList();
            base.readFromNBT(tag, "AspectsBase");
            syncBase(tile, base);
        }
        tile.setNodeType(readType(tag));
        NodeModifier mod = readModifier(tag);
        if (mod != null) {
            tile.setNodeModifier(mod);
        }
    }

    private static void syncBase(arcaneresearch.api.aspects.IAspectContainer tile, AspectList base) {
        if (tile == null || base == null) {
            return;
        }
        AspectList cur;
        if (tile instanceof TileJarNode jar) {
            cur = jar.getAspectsBase();
        } else if (tile instanceof AuraNodeTile node) {
            cur = node.getAspectsBase();
        } else {
            return;
        }
        if (cur == null) {
            return;
        }
        Aspect[] curAspects = cur.getAspects();
        Aspect[] baseAspects = base.getAspects();
        if (curAspects == null || baseAspects == null) {
            return;
        }
        Aspect[] curCopy = curAspects.clone();
        for (Aspect a : curCopy) {
            if (a != null && base.getAmount(a) <= 0) {
                cur.remove(a);
            }
        }
        for (Aspect a : baseAspects) {
            if (a == null) {
                continue;
            }
            int want = base.getAmount(a);
            int have = cur.getAmount(a);
            if (want > have) {
                cur.add(a, want - have);
            } else if (want < have) {
                cur.remove(a, have - want);
            }
        }
    }

    public static AspectList readAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Aspects")) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(tag, "Aspects");
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }

    public static NodeType readType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return NodeType.NORMAL;
        }
        return readType(stack.getTag());
    }

    public static NodeType readType(CompoundTag tag) {
        if (tag == null) {
            return NodeType.NORMAL;
        }
        if (tag.contains("nodetype")) {
            int ord = tag.getByte("nodetype");
            if (ord >= 0 && ord < TYPES.length) {
                return TYPES[ord];
            }
        }
        return NodeType.NORMAL;
    }

    public static NodeModifier readModifier(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        if (tag.contains("nodemod")) {
            int ord = tag.getByte("nodemod");
            if (ord >= 0 && ord < MODIFIERS.length) {
                return MODIFIERS[ord];
            }
        }
        return null;
    }

    public static void appendTooltip(@NotNull ItemStack stack, @NotNull List<Component> tooltip) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        NodeType type = readType(tag);
        NodeModifier mod = readModifier(tag);
        Component typeName = NodeNames.typeName(type);
        if (mod != null) {
            Component modName = NodeNames.modifierName(mod);
            tooltip.add(modName.copy().append(" ").append(typeName));
        } else {
            tooltip.add(typeName);
        }
    }
}