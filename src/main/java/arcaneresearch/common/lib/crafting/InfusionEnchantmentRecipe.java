package arcaneresearch.common.lib.crafting;
import com.google.common.collect.Multimap;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import arcaneresearch.init.ModRecipeSerializers;
import java.util.List;
import java.util.Random;
public class InfusionEnchantmentRecipe extends InfusionRecipeType {
    public final EnumInfusionEnchantment enchantment;
    public InfusionEnchantmentRecipe(ResourceLocation id, EnumInfusionEnchantment ench, AspectList as, NonNullList<Ingredient> components) {
        super(id, "", Ingredient.EMPTY, components, as, ItemStack.EMPTY, ench.research, 4);
        this.enchantment = ench;
    }
    @Override
    public boolean matchesInfusion(List<ItemStack> input, ItemStack central, Level level, Player player) {
        if (central == null || central.isEmpty() || !ArcaneresearchCapabilities.isResearchKnown(player, getResearch())) {
            return false;
        }
        if (EnumInfusionEnchantment.getInfusionEnchantmentLevel(central, enchantment) >= enchantment.maxLevel) {
            return false;
        }
        if (!enchantment.toolClasses.contains("all")) {
            Multimap<Attribute, AttributeModifier> itemMods = central.getAttributeModifiers(EquipmentSlot.MAINHAND);
            boolean cool = false;
            if (itemMods != null && itemMods.containsKey(Attributes.ATTACK_DAMAGE) && enchantment.toolClasses.contains("weapon")) {
                cool = true;
            }
            if (!cool) {
                if (enchantment.toolClasses.contains("axe") && central.canPerformAction(net.minecraftforge.common.ToolActions.AXE_DIG))
                    cool = true;
                if (enchantment.toolClasses.contains("pickaxe") && central.canPerformAction(net.minecraftforge.common.ToolActions.PICKAXE_DIG))
                    cool = true;
                if (enchantment.toolClasses.contains("shovel") && central.canPerformAction(net.minecraftforge.common.ToolActions.SHOVEL_DIG))
                    cool = true;
                if (enchantment.toolClasses.contains("sword") && central.canPerformAction(net.minecraftforge.common.ToolActions.SWORD_DIG))
                    cool = true;
                if (enchantment.toolClasses.contains("hoe") && central.canPerformAction(net.minecraftforge.common.ToolActions.HOE_DIG))
                    cool = true;
            }
            if (!cool && central.getItem() instanceof ArmorItem armorItem) {
                String at = "none";
                switch (armorItem.getType()) {
                    case HELMET -> at = "helm";
                    case CHESTPLATE -> at = "chest";
                    case LEGGINGS -> at = "legs";
                    case BOOTS -> at = "boots";
                }
                if (enchantment.toolClasses.contains("armor") || enchantment.toolClasses.contains(at)) {
                    cool = true;
                }
            }
            if (!cool && (enchantment.toolClasses.contains("bauble")
                    || enchantment.toolClasses.contains("amulet")
                    || enchantment.toolClasses.contains("belt")
                    || enchantment.toolClasses.contains("ring"))) {
                if (CuriosCompat.isCuriosLoaded()) {
                    try {
                        Class<?> curioInterface = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem");
                        if (curioInterface.isAssignableFrom(central.getItem().getClass())) {
                            cool = true;
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
            if (!cool && central.getItem() instanceof IRechargable && enchantment.toolClasses.contains("chargable")) {
                cool = true;
            }
            if (!cool && central.getItem() instanceof arcaneresearch.api.wands.IWand
                    && enchantment.toolClasses.contains("wand")) {
                cool = true;
            }
            if (!cool) {
                return false;
            }
        }
        return RecipeMatcher.findMatches(input, getComponents()) != null;
    }
    @Override
    public ItemStack getRecipeOutput(Player player, ItemStack input, List<ItemStack> comps) {
        if (input == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = input.copy();
        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(out, enchantment);
        if (cl >= enchantment.maxLevel) {
            return ItemStack.EMPTY;
        }
        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        Random rand = new Random(System.nanoTime());
        if (rand.nextInt(10) < el.size()) {
            int base = 1;
            if (input.hasTag()) {
                base += input.getTag().getByte("TC.WARP");
            }
            out.addTagElement("TC.WARP", ByteTag.valueOf((byte) base));
        }
        EnumInfusionEnchantment.addInfusionEnchantment(out, enchantment, cl + 1);
        return out;
    }
    @Override
    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> comps) {
        AspectList out = new AspectList();
        if (input == null || input.isEmpty()) {
            return out;
        }
        int cl = EnumInfusionEnchantment.getInfusionEnchantmentLevel(input, enchantment) + 1;
        if (cl > enchantment.maxLevel) {
            return out;
        }
        List<EnumInfusionEnchantment> el = EnumInfusionEnchantment.getInfusionEnchantments(input);
        int otherEnchantments = el.size();
        if (el.contains(enchantment)) {
            --otherEnchantments;
        }
        float modifier = cl + otherEnchantments * 0.33f;
        for (Aspect a : super.getAspects().getAspects()) {
            out.add(a, (int) (super.getAspects().getAmount(a) * modifier));
        }
        return out;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.INFUSION_ENCHANTMENT.get();
    }
}
