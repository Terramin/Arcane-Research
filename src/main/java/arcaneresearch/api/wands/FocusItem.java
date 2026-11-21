package arcaneresearch.api.wands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

public class FocusItem extends Item {
    public static LinkedHashMap<String, FocusItem> foci = new LinkedHashMap<>();
    private final String id;
    private final Identifier texture;
    private final int focusColor;

    public FocusItem(String id, Identifier texture, int renderColor, Properties properties) {
        super(properties.stacksTo(1));
        this.id = id;
        this.texture = texture;
        this.focusColor = renderColor;
        foci.put(id, this);
    }

    public FocusItem(String id, Identifier texture, int renderColor) {
        this(id, texture, renderColor, new Properties());
    }

    public FocusItem(String id, int renderColor) {
        this(id, Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/focus"), renderColor, new Properties());
    }

    public FocusItem(String id, int renderColor, Properties properties) {
        this(id, Identifier.fromNamespaceAndPath("arcaneresearch", "item/wand/focus"), renderColor, properties);
    }

    public int getFocusColor(ItemStack focusstack) {
        return this.focusColor;
    }

    public String getFocusId() {
        return this.id;
    }

    public Identifier getFocusTexture() {
        return this.texture;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public boolean canBePlacedInTurret() {
        return false;
    }

    public float getTurretCorrection(ItemStack focusstack) {
        return 0.0F;
    }

    public float getTurretRange(ItemStack focusstack) {
        return 32.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        AspectList al = getVisCost(stack);
        if (al != null && al.size() > 0) {
            tooltip.accept(Component.translatable(isVisCostPerTick(stack) ? "item.Focus.cost2" : "item.Focus.cost1"));
            for (Aspect aspect : al.getAspectsSortedByName()) {
                DecimalFormat myFormatter = new DecimalFormat("#####.##");
                String amount = myFormatter.format(al.getAmount(aspect));
                tooltip.accept(Component.literal(" \u00A7" + aspect.getChatcolor() + aspect.getName() + "\u00A7r x " + amount));
            }
        }
        addFocusInformation(stack, context, tooltip, flag);
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    public void addFocusInformation(ItemStack focusstack, TooltipContext context,
                                    Consumer<Component> tooltip, TooltipFlag flag) {
        LinkedHashMap<Short, Integer> map = new LinkedHashMap<>();
        for (short upgradeId : getAppliedUpgrades(focusstack)) {
            if (upgradeId >= 0) {
                int amt = 1;
                if (map.containsKey(upgradeId)) {
                    amt = map.get(upgradeId) + 1;
                }
                map.put(upgradeId, amt);
            }
        }
        for (Short upgradeId : map.keySet()) {
            String key = FocusUpgradeType.types[upgradeId] != null
                    ? FocusUpgradeType.types[upgradeId].getLocalizedName()
                    : ("focus.upgrade." + upgradeId);
            int count = map.get(upgradeId);
            String suffix = count > 1
                    ? " " + Component.translatable("enchantment.level." + count).getString()
                    : "";
            tooltip.accept(Component.literal(ChatFormatting.DARK_PURPLE + key + suffix));
        }
    }

    @Deprecated
    public void addFocusInformation(ItemStack focusstack, Player player, List<String> list, boolean advanced) {
        List<Component> tmp = new ArrayList<>();
        addFocusInformation(focusstack, TooltipContext.of(player == null ? null : player.level()), tmp::add,
                advanced ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        for (Component c : tmp) {
            list.add(c.getString());
        }
    }

    public boolean isVisCostPerTick(ItemStack focusstack) {
        return false;
    }

    public Rarity getRarity(ItemStack focusstack) {
        return Rarity.RARE;
    }

    public enum WandFocusAnimation {
        WAVE, CHARGE
    }

    public WandFocusAnimation getAnimation(ItemStack focusstack) {
        return WandFocusAnimation.WAVE;
    }

    public String getSortingHelper(ItemStack focusstack) {
        String out = this.id;
        for (short upgradeId : getAppliedUpgrades(focusstack)) {
            out = out + upgradeId;
        }
        return out;
    }

    public AspectList getVisCost(ItemStack focusstack) {
        return null;
    }

    public int getActivationCooldown(ItemStack focusstack) {
        return 0;
    }

    public int getMaxAreaSize(ItemStack focusstack) {
        return 1;
    }

    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return null;
    }

    public short[] getAppliedUpgrades(ItemStack focusstack) {
        short[] l = {-1, -1, -1, -1, -1};
        ListTag nbttaglist = getFocusUpgradeTagList(focusstack);
        if (nbttaglist == null) {
            return l;
        }
        for (int j = 0; j < nbttaglist.size(); j++) {
            if (j >= 5) {
                break;
            }
            l[j] = nbttaglist.getCompoundOrEmpty(j).getShortOr("id", (short) -1);
        }
        return l;
    }

    public boolean applyUpgrade(ItemStack focusstack, FocusUpgradeType type, int rank) {
        short[] upgrades = getAppliedUpgrades(focusstack);
        if (upgrades[rank - 1] != -1 || rank < 1 || rank > 5) {
            return false;
        }
        upgrades[rank - 1] = type.id;
        setFocusUpgradeTagList(focusstack, upgrades);
        return true;
    }

    public boolean canApplyUpgrade(ItemStack focusstack, Player player, FocusUpgradeType type, int rank) {
        return true;
    }

    public boolean isUpgradedWith(ItemStack focusstack, FocusUpgradeType focusUpgradetype) {
        return getUpgradeLevel(focusstack, focusUpgradetype) > 0;
    }

    public int getUpgradeLevel(ItemStack focusstack, FocusUpgradeType focusUpgradetype) {
        short[] list = getAppliedUpgrades(focusstack);
        int level = 0;
        for (short upgradeId : list) {
            if (upgradeId == focusUpgradetype.id) {
                level++;
            }
        }
        return level;
    }

    public boolean onFocusActivation(ItemStack wandstack, net.minecraft.world.level.Level level, LivingEntity entity, HitResult hit, int useCount) {
        return true;
    }

    public boolean onFocusBlockStartBreak(ItemStack wandstack, BlockPos pos, Player player) {
        return false;
    }

    public List<ItemStack> getVariants() {
        List<ItemStack> variants = new ArrayList<>();
        variants.add(new ItemStack(this));
        return variants;
    }

    private ListTag getFocusUpgradeTagList(ItemStack focusstack) {
        if (focusstack.isEmpty() || !ArcaneresearchInvHelper.hasCustomTag(focusstack)) {
            return null;
        }
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(focusstack);
        if (tag == null || !tag.contains("upgrade")) {
            return null;
        }
        return tag.getListOrEmpty("upgrade");
    }

    private void setFocusUpgradeTagList(ItemStack focusstack, short[] upgrades) {
        if (focusstack.isEmpty()) {
            return;
        }
        CompoundTag nbttagcompound = ArcaneresearchInvHelper.getOrCreateCustomTag(focusstack);
        ListTag tlist = new ListTag();
        nbttagcompound.put("upgrade", tlist);
        for (short upgradeId : upgrades) {
            CompoundTag f = new CompoundTag();
            f.putShort("id", upgradeId);
            tlist.add(f);
        }
        ArcaneresearchInvHelper.setCustomTag(focusstack, nbttagcompound);
    }
}
