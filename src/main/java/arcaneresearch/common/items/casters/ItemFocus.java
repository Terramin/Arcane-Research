package arcaneresearch.common.items.casters;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.casters.*;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
public class ItemFocus extends Item {
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#######.#");
    private final int maxComplexity;
    public ItemFocus(int complexity) {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.maxComplexity = complexity;
    }
    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        CompoundTag tag = core.serialize();
        focusStack.getOrCreateTag().put("package", tag);
    }
    @Nullable
    public static FocusPackage getPackage(ItemStack focusStack) {
        if (focusStack.isEmpty()) {
            return null;
        }
        CompoundTag tag = focusStack.getTagElement("package");
        if (tag != null) {
            FocusPackage pack = new FocusPackage();
            pack.deserialize(tag);
            return pack;
        }
        return null;
    }
    public static ItemFocus createBasic() {
        return new ItemFocus(15);
    }
    public static ItemFocus createBlank() {
        return new ItemFocus(25);
    }
    public static ItemFocus createAdvanced() {
        return new ItemFocus(50);
    }
    public int getFocusColor(ItemStack focusStack) {
        if (focusStack.isEmpty() || !focusStack.hasTag()) {
            return 0xFFFFFF;
        }
        CompoundTag tag = focusStack.getTag();
        if (tag.contains("color")) {
            return tag.getInt("color");
        }
        int color = 0xFFFFFF;
        FocusPackage core = getPackage(focusStack);
        if (core != null) {
            FocusEffect[] effects = core.getFocusEffects();
            int r = 0, g = 0, b = 0;
            for (FocusEffect effect : effects) {
                java.awt.Color c = new java.awt.Color(FocusEngine.getElementColor(effect.getKey()));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
            if (effects.length > 0) {
                r /= effects.length;
                g /= effects.length;
                b /= effects.length;
            }
            color = new java.awt.Color(r, g, b).getRGB();
            focusStack.getOrCreateTag().putInt("color", color);
        }
        return color;
    }
    public String getSortingHelper(ItemStack focusStack) {
        if (focusStack.isEmpty() || !focusStack.hasTag()) {
            return null;
        }
        CompoundTag tag = focusStack.getTag();
        int sh = tag != null ? tag.getInt("srt") : 0;
        if (sh == 0) {
            FocusPackage pack = getPackage(focusStack);
            if (pack != null) {
                sh = pack.getSortingHelper();
                focusStack.getOrCreateTag().putInt("srt", sh);
            }
        }
        return focusStack.getHoverName().getString() + sh;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addFocusInformation(stack, level, tooltip, flag);
    }
    public void addFocusInformation(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FocusPackage pack = getPackage(stack);
        if (pack != null) {
            float visCost = getVisCost(stack);
            String amount = VIS_FORMAT.format(visCost);
            tooltip.add(Component.translatable("item.arcaneresearch.focus.cost", amount)
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
            for (IFocusElement node : pack.nodes) {
                if (node instanceof FocusNode focusNode && !(focusNode instanceof FocusMediumRoot)) {
                    buildInfo(tooltip, focusNode, 0);
                }
            }
        }
    }
    private void buildInfo(List<Component> tooltip, FocusNode node, int depth) {
        if (node == null || node instanceof FocusMediumRoot) {
            return;
        }
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        String text = indent.toString() + ChatFormatting.DARK_PURPLE
                + Component.translatable(node.getUnlocalizedName()).getString();
        if (!node.getSettingList().isEmpty()) {
            text += ChatFormatting.DARK_AQUA + " [";
            boolean first = true;
            for (String key : node.getSettingList()) {
                NodeSetting setting = node.getSetting(key);
                text += (first ? "" : ", ") + setting.getLocalizedName() + " " + setting.getValueText();
                first = false;
            }
            text += "]";
        }
        tooltip.add(Component.literal(text));
        if (node instanceof FocusModSplit split) {
            for (FocusPackage branch : split.getSplitPackages()) {
                for (IFocusElement element : branch.nodes) {
                    if (element instanceof FocusNode branchNode) {
                        buildInfo(tooltip, branchNode, depth + 1);
                    }
                }
            }
        }
    }
    public float getVisCost(ItemStack focusStack) {
        FocusPackage pack = getPackage(focusStack);
        if (pack == null) {
            return 0.0f;
        }
        return pack.getComplexity() / 5.0f;
    }
    public int getActivationTime(ItemStack focusStack) {
        FocusPackage pack = getPackage(focusStack);
        if (pack == null) {
            return 0;
        }
        int complexity = pack.getComplexity();
        return Math.max(5, (complexity / 5) * (complexity / 4));
    }
    public int getMaxComplexity() {
        return maxComplexity;
    }
}
