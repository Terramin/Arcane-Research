package arcaneresearch.common.items.casters;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.casters.*;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.api.wands.FocusItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFocus extends FocusItem {

    private final int maxComplexity;

    protected ItemFocus(int complexity) {
        this("caster_focus_" + complexity, complexity);
    }

    private ItemFocus(String id, int complexity) {
        super(id, ResourceLocation.fromNamespaceAndPath("arcaneresearch", "item/wand/focus"),
                0xFFFFFF, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
        this.maxComplexity = complexity;
    }

    public static ItemFocus createBasic() {
        return new ItemFocus("caster_focus_basic", 15);
    }

    public static ItemFocus createBlank() {
        return new ItemFocus("caster_focus_blank", 25);
    }

    public static ItemFocus createAdvanced() {
        return new ItemFocus("caster_focus_advanced", 50);
    }

    public static void setPackage(ItemStack focusStack, FocusPackage core) {
        if (focusStack == null || core == null) {
            return;
        }
        CompoundTag tag = core.serialize();
        if (tag != null) {
            focusStack.getOrCreateTag().put("package", tag);
        }
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

    private static ItemStack resolveFocusStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof IFocusHost wand) {
            return wand.getFocusStack(stack);
        }
        return stack;
    }

    @Override
    public AspectList getVisCost(ItemStack focusStack) {
        AspectList out = new AspectList();
        collectCost(getPackage(resolveFocusStack(focusStack)), out, 0);
        return out;
    }

    private static void collectCost(FocusPackage pack, AspectList out, int depth) {
        if (pack == null || pack.nodes == null || depth > 6) {
            return;
        }
        synchronized (pack.nodes) {
            for (IFocusElement element : pack.nodes) {
                if (element instanceof FocusNode node) {
                    addPrimalCost(out, node.getAspect(),
                            Math.max(1, Math.round(node.getComplexity() / 5.0F)), 0);
                }
                if (element instanceof FocusModSplit split) {
                    for (FocusPackage branch : split.getSplitPackages()) {
                        collectCost(branch, out, depth + 1);
                    }
                } else if (element instanceof FocusPackage sub) {
                    collectCost(sub, out, depth + 1);
                }
            }
        }
    }

    private static void addPrimalCost(AspectList out, Aspect aspect, int vis, int depth) {
        if (aspect == null || vis <= 0 || depth > 6) {
            return;
        }
        if (aspect.isPrimal()) {
            out.add(aspect, vis);
            return;
        }
        Aspect[] parts = aspect.getComponents();
        if (parts == null || parts.length == 0) {
            out.add(Aspect.MAGIC, vis);
            return;
        }
        for (Aspect part : parts) {
            addPrimalCost(out, part, Math.max(1, Math.round(vis / (float) parts.length)), depth + 1);
        }
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity,
                                     HitResult hit, int useCount) {
        FocusPackage pack = getPackage(resolveFocusStack(wandstack));
        if (pack == null) {
            return false;
        }
        if (!level.isClientSide) {
            FocusEngine.castFocusPackage(entity, pack);
        }
        return true;
    }

    @Override
    public int getActivationCooldown(ItemStack focusStack) {
        return getActivationTime(resolveFocusStack(focusStack));
    }

    @Override
    public int getFocusColor(ItemStack focusStack) {
        if (focusStack == null || focusStack.isEmpty() || !focusStack.hasTag()) {
            return 0xFFFFFF;
        }
        CompoundTag tag = focusStack.getTag();
        if (tag != null && tag.contains("color")) {
            return tag.getInt("color");
        }

        int color = 0xFFFFFF;
        FocusPackage core = getPackage(focusStack);
        if (core != null) {
            FocusEffect[] effects = core.getFocusEffects();
            if (effects != null && effects.length > 0) {
                int r = 0, g = 0, b = 0, count = 0;
                for (FocusEffect effect : effects) {
                    if (effect == null || effect.getKey() == null) {
                        continue;
                    }
                    int elementColor = FocusEngine.getElementColor(effect.getKey());
                    r += (elementColor >> 16) & 0xFF;
                    g += (elementColor >> 8) & 0xFF;
                    b += elementColor & 0xFF;
                    count++;
                }
                if (count > 0) {
                    color = 0xFF000000 | ((r / count) << 16) | ((g / count) << 8) | (b / count);
                    focusStack.getOrCreateTag().putInt("color", color);
                }
            }
        }
        return color;
    }

    @Override
    public String getSortingHelper(ItemStack focusStack) {
        if (focusStack == null || focusStack.isEmpty()) {
            return getDescriptionId();
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
    public void addFocusInformation(ItemStack focusStack, @Nullable Level level,
                                    @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        FocusPackage pack = getPackage(focusStack);
        if (pack == null) {
            return;
        }
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
                ? Component.translatable(unlocalized).getString() : "";
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
            for (FocusPackage branch : split.getSplitPackages()) {
                if (branch == null || branch.nodes == null) {
                    continue;
                }
                for (IFocusElement element : branch.nodes) {
                    if (element instanceof FocusNode branchNode) {
                        buildInfo(tooltip, branchNode, depth + 1);
                    }
                }
            }
        }
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
