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

    public ItemFocus(Item.Properties props, int complexity) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.maxComplexity = complexity;
    }

    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        
        CompoundTag tag = core.serialize();
        CompoundTag root = arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(focusStack);
        root.put("package", tag);
        arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(focusStack, root);
    }

    @Nullable
    public static FocusPackage getPackage(ItemStack focusStack) {
        if (focusStack.isEmpty()) {
            return null;
        }

        CompoundTag root = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(focusStack);
        if (root != null && root.contains("package")) {
            CompoundTag tag = root.getCompoundOrEmpty("package");
            FocusPackage pack = new FocusPackage();
            pack.deserialize(tag);
            return pack;
        }

        return null;
    }

    public static ItemFocus createBasic(Item.Properties props) {
        return new ItemFocus(props, 15);
    }

    public static ItemFocus createBlank(Item.Properties props) {
        return new ItemFocus(props, 25);
    }

    public static ItemFocus createAdvanced(Item.Properties props) {
        return new ItemFocus(props, 50);
    }

    public int getFocusColor(ItemStack focusStack) {
        if (focusStack.isEmpty() || !arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(focusStack)) {
            return 0xFFFFFF;
        }

        CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(focusStack);
        if (tag != null && tag.contains("color")) {
            return tag.getIntOr("color", 0);
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
            CompoundTag root = arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(focusStack);
            root.putInt("color", color);
            arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(focusStack, root);
        }

        return color;
    }

    public String getSortingHelper(ItemStack focusStack) {
        if (focusStack.isEmpty() || !arcaneresearch.api.ArcaneresearchInvHelper.hasCustomTag(focusStack)) {
            return null;
        }

        CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(focusStack);
        int sh = tag != null ? tag.getIntOr("srt", 0) : 0;

        if (sh == 0) {
            FocusPackage pack = getPackage(focusStack);
            if (pack != null) {
                sh = pack.getSortingHelper();
                CompoundTag root = arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(focusStack);
                root.putInt("srt", sh);
                arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(focusStack, root);
            }
        }

        return focusStack.getHoverName().getString() + sh;
    }

    
    
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        addFocusInformation(stack, level, tooltip, flag);
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        addFocusInformation(stack, context.level(), lines, flag);
        for (Component line : lines) {
            tooltip.accept(line);
        }
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
