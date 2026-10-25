package arcaneresearch.common.lib.aura;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.tiles.aura.TileJarNode;
import java.util.List;
public class NodeStackData {
    private NodeStackData() {
    }
    public static ItemStack create(Item item, NodeType type, NodeModifier modifier,
                                   AspectList aspects) {
        return create(item, type, modifier, aspects, aspects);
    }
    public static ItemStack create(Item item, NodeType type, NodeModifier modifier,
                                   AspectList aspects, AspectList base) {
        ItemStack stack = new ItemStack(item);
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
        if (nodeId != null && !nodeId.isEmpty()) {
            stack.getOrCreateTag().putString("nodeId", nodeId);
        }
        return stack;
    }
    public static void applyTo(TileJarNode tile, CompoundTag tag) {
        if (tag == null) {
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
        tile.setNodeModifier(mod);
        if (tag.contains("nodeId")) {
            tile.setId(tag.getString("nodeId"));
        } else if (tag.contains("nodeid")) {
            tile.setId(tag.getString("nodeid"));
        }
    }
    public static void applyTo(AuraNodeTile tile, CompoundTag tag) {
        if (tag == null) {
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
        try {
            AspectList cur;
            if (tile instanceof TileJarNode jar) {
                cur = jar.getAspectsBase();
            } else if (tile instanceof AuraNodeTile node) {
                cur = node.getAspectsBase();
            } else {
                return;
            }
            for (Aspect a : cur.getAspects().clone()) {
                if (base.getAmount(a) <= 0) {
                    cur.remove(a);
                }
            }
            for (Aspect a : base.getAspects()) {
                int want = base.getAmount(a);
                int have = cur.getAmount(a);
                if (want > have) {
                    cur.add(a, want - have);
                } else if (want < have) {
                    cur.remove(a, have - want);
                }
            }
        } catch (Exception ignored) {
        }
    }
    public static AspectList readAspects(ItemStack stack) {
        try {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Aspects")) {
                AspectList aspects = new AspectList();
                aspects.readFromNBT(tag, "Aspects");
                return aspects.size() > 0 ? aspects : null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static NodeType readType(ItemStack stack) {
        try {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                return readType(tag);
            }
        } catch (Exception ignored) {
        }
        return NodeType.NORMAL;
    }
    public static NodeType readType(CompoundTag tag) {
        try {
            if (tag.contains("nodetype")) {
                int ord = tag.getByte("nodetype");
                NodeType[] values = NodeType.values();
                if (ord >= 0 && ord < values.length) {
                    return values[ord];
                }
            }
        } catch (Exception ignored) {
        }
        return NodeType.NORMAL;
    }
    public static NodeModifier readModifier(CompoundTag tag) {
        try {
            if (tag.contains("nodemod")) {
                int ord = tag.getByte("nodemod");
                NodeModifier[] values = NodeModifier.values();
                if (ord >= 0 && ord < values.length) {
                    return values[ord];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        try {
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                return;
            }
            NodeType type = readType(tag);
            NodeModifier mod = readModifier(tag);
            if (mod != null) {
                tooltip.add(NodeNames.modifierName(mod).copy()
                        .append(" ").append(NodeNames.typeName(type)));
            } else {
                tooltip.add(NodeNames.typeName(type));
            }
            AspectList aspects = readAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
                    if (aspect != null) {
                        tooltip.add(Component.literal(aspect.getName() + " x "
                                + aspects.getAmount(aspect)));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
