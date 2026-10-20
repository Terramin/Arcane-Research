package arcaneresearch.common.lib.aura;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 common/lib/aura/NodeNames.java (22 lines, no API deps).
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
