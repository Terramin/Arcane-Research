package arcaneresearch.common.lib.aura;
import net.minecraft.network.chat.Component;
public class NodeNames {
    private NodeNames() {
    }
    public static Component typeName(NodeType type) {
        if (type == null) {
            return Component.empty();
        }
        return Component.translatable("aura.nodetype." + type.name());
    }
    public static Component modifierName(NodeModifier modifier) {
        if (modifier == null) {
            return Component.empty();
        }
        return Component.translatable("aura.nodemod." + modifier.name());
    }
}
