package arcaneresearch.common.items.casters;

import arcaneresearch.api.casters.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

public class ItemFocus extends Item {

    private static final ThreadLocal<DecimalFormat> VIS_FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("#######.#"));

    private final int maxComplexity;

    public ItemFocus(int complexity) {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.maxComplexity = complexity;
    }

    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        if (focusStack == null || core == null) {
            return;
        }

        CompoundTag tag = core.serialize();
        if (tag == null) {
            return;
        }

        focusStack.getOrCreateTag().put("package", tag);
    }

    @Nullable
    public static FocusPackage getPackage(ItemStack focusStack) {
        if (focusStack == null || focusStack.isEmpty()) {
            return null;
        }

        CompoundTag tag = focusStack.getTagElement("package");
        if (tag == null) {
            return null;
        }

        FocusPackage pack = new FocusPackage();
        pack.deserialize(tag);
        return pack;
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
        if (focusStack == null || focusStack.isEmpty() || !focusStack.hasTag()) {
            return 0xFFFFFF;
        }

        CompoundTag tag = focusStack.getTag();
        if (tag == null) {
            return 0xFFFFFF;
        }

        if (tag.contains("color")) {
            return tag.getInt("color");
        }

        int color = 0xFFFFFF;
        FocusPackage core = getPackage(focusStack);

        if (core != null) {
            FocusEffect[] effects = core.getFocusEffects();

            if (effects != null && effects.length > 0) {
                int r = 0;
                int g = 0;
                int b = 0;
                int count = 0;

                for (FocusEffect effect : effects) {
                    if (effect == null) {
                        continue;
                    }

                    String key = effect.getKey();
                    if (key == null) {
                        continue;
                    }

                    int elementColor = FocusEngine.getElementColor(key);
                    r += (elementColor >> 16) & 0xFF;
                    g += (elementColor >> 8) & 0xFF;
                    b += elementColor & 0xFF;
                    count++;
                }

                if (count > 0) {
                    r /= count;
                    g /= count;
                    b /= count;
                    color = 0xFF000000 | (r << 16) | (g << 8) | b;
                    focusStack.getOrCreateTag().putInt("color", color);
                }
            }
        }

        return color;
    }

    public String getSortingHelper(ItemStack focusStack) {
        if (focusStack == null || focusStack.isEmpty() || !focusStack.hasTag()) {
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        addFocusInformation(stack, level, tooltip, flag);
    }

    public void addFocusInformation(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FocusPackage pack = getPackage(stack);
        if (pack == null) {
            return;
        }

        float visCost = getVisCost(pack);
        String amount = VIS_FORMAT.get().format(visCost);

        tooltip.add(Component.translatable("item.arcaneresearch.focus.cost", amount)
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));

        var nodes = pack.nodes;
        if (nodes == null) {
            return;
        }

        for (IFocusElement node : nodes) {
            if (node instanceof FocusNode focusNode && !(focusNode instanceof FocusMediumRoot)) {
                buildInfo(tooltip, focusNode, 0);
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

        String unlocalized = node.getUnlocalizedName();
        String nodeName = unlocalized != null
                ? Component.translatable(unlocalized).getString()
                : "";

        String text = indent.toString() + ChatFormatting.DARK_PURPLE + nodeName;

        var settingList = node.getSettingList();
        if (settingList != null && !settingList.isEmpty()) {
            text += ChatFormatting.DARK_AQUA + " [";
            boolean first = true;

            for (String key : settingList) {
                if (key == null) {
                    continue;
                }

                NodeSetting setting = node.getSetting(key);
                if (setting == null) {
                    continue;
                }

                text += (first ? "" : ", ") + setting.getLocalizedName() + " " + setting.getValueText();
                first = false;
            }

            text += "]";
        }

        tooltip.add(Component.literal(text));

        if (node instanceof FocusModSplit split) {
            var splitPackages = split.getSplitPackages();
            if (splitPackages == null) {
                return;
            }

            for (FocusPackage branch : splitPackages) {
                if (branch == null) {
                    continue;
                }

                var branchNodes = branch.nodes;
                if (branchNodes == null) {
                    continue;
                }

                for (IFocusElement element : branchNodes) {
                    if (element instanceof FocusNode branchNode) {
                        buildInfo(tooltip, branchNode, depth + 1);
                    }
                }
            }
        }
    }

    private float getVisCost(FocusPackage pack) {
        return pack == null ? 0.0f : pack.getComplexity() / 5.0f;
    }

    public float getVisCost(ItemStack focusStack) {
        return getVisCost(getPackage(focusStack));
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