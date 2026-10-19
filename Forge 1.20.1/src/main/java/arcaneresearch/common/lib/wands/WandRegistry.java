package arcaneresearch.common.lib.wands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.common.items.wands.WandRodPrimalOnUpdate;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
public class WandRegistry {
    public static WandCap WAND_CAP_IRON;
    public static WandCap WAND_CAP_COPPER;
    public static WandCap WAND_CAP_GOLD;
    public static WandCap WAND_CAP_BRASS;
    public static WandCap WAND_CAP_SILVER;
    public static WandCap WAND_CAP_THAUMIUM;
    public static WandCap WAND_CAP_VOID;
    public static WandRod WAND_ROD_WOOD;
    public static WandRod WAND_ROD_GREATWOOD;
    public static WandRod WAND_ROD_SILVERWOOD;
    public static WandRod WAND_ROD_OBSIDIAN;
    public static WandRod WAND_ROD_BLAZE;
    public static WandRod WAND_ROD_ICE;
    public static WandRod WAND_ROD_QUARTZ;
    public static WandRod WAND_ROD_BONE;
    public static WandRod WAND_ROD_REED;
    public static WandRod STAFF_ROD_GREATWOOD;
    public static WandRod STAFF_ROD_SILVERWOOD;
    public static WandRod STAFF_ROD_OBSIDIAN;
    public static WandRod STAFF_ROD_BLAZE;
    public static WandRod STAFF_ROD_ICE;
    public static WandRod STAFF_ROD_QUARTZ;
    public static WandRod STAFF_ROD_BONE;
    public static WandRod STAFF_ROD_REED;
    public static WandRod STAFF_ROD_PRIMAL;
    private static boolean inited = false;
    private static ItemStack capStackIron() {
        return new ItemStack(Items.IRON_INGOT);
    }
    private static ItemStack capStackCopper() {
        return new ItemStack(Items.COPPER_INGOT);
    }
    private static ItemStack capStackSilver() {
        try {
            return new ItemStack(ModItems.SILVER_NUGGET.get());
        } catch (Exception e) {
            return new ItemStack(Items.IRON_NUGGET);
        }
    }
    private static ItemStack capStackGold() {
        return new ItemStack(Items.GOLD_INGOT);
    }
    private static ItemStack capStackBrass() {
        try {
            return new ItemStack(ModItems.BRASS_INGOT.get());
        } catch (Exception e) {
            return new ItemStack(Items.GOLD_INGOT);
        }
    }
    private static ItemStack capStackThaumium() {
        try {
            return new ItemStack(ModItems.THAUMIUM_INGOT.get());
        } catch (Exception e) {
            return new ItemStack(Items.IRON_INGOT);
        }
    }
    private static ItemStack capStackVoid() {
        try {
            return new ItemStack(ModItems.VOID_METAL_INGOT.get());
        } catch (Exception e) {
            return new ItemStack(Items.NETHERITE_INGOT);
        }
    }
    private static ItemStack rodStackGreatwood() {
        try {
            return new ItemStack(ModBlocks.GREATWOOD_LOG.get());
        } catch (Exception e) {
            return new ItemStack(Blocks.OAK_LOG);
        }
    }
    private static ItemStack rodStackSilverwood() {
        try {
            return new ItemStack(ModBlocks.SILVERWOOD_LOG.get());
        } catch (Exception e) {
            return new ItemStack(Blocks.BIRCH_LOG);
        }
    }
    public static synchronized void initWands() {
        if (inited) {
            return;
        }
        inited = true;
        WAND_CAP_IRON = new WandCap("iron", 1.1F, 0, capStackIron(), 1,
                new ResourceLocation("arcaneresearch", "item/wand/cap_iron_mat"));
        WAND_CAP_COPPER = new WandCap("copper", 1.0F, 0, capStackCopper(), 2,
                new ResourceLocation("arcaneresearch", "item/wand/cap_copper_mat"));
        WAND_CAP_COPPER.setRechargeMultiplier(1.25F);
        WAND_CAP_GOLD = new WandCap("gold", 1.0F, 0, capStackGold(), 3,
                new ResourceLocation("arcaneresearch", "item/wand/cap_gold_mat"));
        WAND_CAP_GOLD.setRechargeMultiplier(1.5F);
        WAND_CAP_BRASS = new WandCap("brass", 1.0F, 1, capStackBrass(), 3,
                new ResourceLocation("arcaneresearch", "item/wand/cap_brass_mat"));
        WAND_CAP_BRASS.setRechargeMultiplier(1.5F);
        WAND_CAP_SILVER = new WandCap("silver", 0.95F, 0, capStackSilver(), 4,
                new ResourceLocation("arcaneresearch", "item/wand/cap_silver_mat"));
        WAND_CAP_SILVER.setRechargeMultiplier(2.0F);
        WAND_CAP_THAUMIUM = new WandCap("thaumium", 0.9F, 0, capStackThaumium(), 6,
                new ResourceLocation("arcaneresearch", "item/wand/cap_thaumium_mat"));
        WAND_CAP_THAUMIUM.setRechargeMultiplier(2.5F);
        WAND_CAP_VOID = new WandCap("void", 1.0F, 3, capStackVoid(), 9,
                new ResourceLocation("arcaneresearch", "item/wand/cap_void_mat"));
        WAND_CAP_VOID.setRechargeMultiplier(3.0F);
        WAND_ROD_WOOD = new WandRod("wood", 100, new ItemStack(Items.STICK), 1,
                new ResourceLocation("arcaneresearch", "item/wand/rod_wood_mat"));
        WAND_ROD_GREATWOOD = new WandRod("greatwood", 250, rodStackGreatwood(), 3,
                new ResourceLocation("arcaneresearch", "item/wand/rod_greatwood_mat"));
        WAND_ROD_SILVERWOOD = new WandRod("silverwood", 500, rodStackSilverwood(), 9,
                new ResourceLocation("arcaneresearch", "item/wand/rod_silverwood_mat"));
        WAND_ROD_OBSIDIAN = new WandRod("obsidian", 375, new ItemStack(Blocks.OBSIDIAN), 6,
                new WandRodPrimalOnUpdate(Aspect.EARTH),
                new ResourceLocation("arcaneresearch", "item/wand/rod_obsidian_mat"));
        WAND_ROD_BLAZE = new WandRod("blaze", 375, new ItemStack(Items.BLAZE_ROD), 6,
                new WandRodPrimalOnUpdate(Aspect.FIRE),
                new ResourceLocation("arcaneresearch", "item/wand/rod_blaze_mat"));
        WAND_ROD_ICE = new WandRod("ice", 375, new ItemStack(Blocks.ICE), 6,
                new WandRodPrimalOnUpdate(Aspect.WATER),
                new ResourceLocation("arcaneresearch", "item/wand/rod_ice_mat"));
        WAND_ROD_QUARTZ = new WandRod("quartz", 375, new ItemStack(Items.QUARTZ), 6,
                new WandRodPrimalOnUpdate(Aspect.ORDER),
                new ResourceLocation("arcaneresearch", "item/wand/rod_quartz_mat"));
        WAND_ROD_BONE = new WandRod("bone", 375, new ItemStack(Items.BONE), 6,
                new WandRodPrimalOnUpdate(Aspect.ENTROPY),
                new ResourceLocation("arcaneresearch", "item/wand/rod_bone_mat"));
        WAND_ROD_REED = new WandRod("reed", 375, new ItemStack(Items.SUGAR_CANE), 6,
                new WandRodPrimalOnUpdate(Aspect.AIR),
                new ResourceLocation("arcaneresearch", "item/wand/rod_reed_mat"));
        STAFF_ROD_GREATWOOD = new WandRod("greatwood_staff", 375, rodStackGreatwood(), 8,
                new ResourceLocation("arcaneresearch", "item/wand/rod_greatwood_mat"));
        STAFF_ROD_SILVERWOOD = new WandRod("silverwood_staff", 750, rodStackSilverwood(), 24,
                new ResourceLocation("arcaneresearch", "item/wand/rod_silverwood_mat"));
        STAFF_ROD_OBSIDIAN = new WandRod("obsidian_staff", 500, new ItemStack(Blocks.OBSIDIAN), 14,
                new WandRodPrimalOnUpdate(Aspect.EARTH),
                new ResourceLocation("arcaneresearch", "item/wand/rod_obsidian_mat"));
        STAFF_ROD_BLAZE = new WandRod("blaze_staff", 500, new ItemStack(Items.BLAZE_ROD), 14,
                new WandRodPrimalOnUpdate(Aspect.FIRE),
                new ResourceLocation("arcaneresearch", "item/wand/rod_blaze_mat"));
        STAFF_ROD_ICE = new WandRod("ice_staff", 500, new ItemStack(Blocks.ICE), 14,
                new WandRodPrimalOnUpdate(Aspect.WATER),
                new ResourceLocation("arcaneresearch", "item/wand/rod_ice_mat"));
        STAFF_ROD_QUARTZ = new WandRod("quartz_staff", 500, new ItemStack(Items.QUARTZ), 14,
                new WandRodPrimalOnUpdate(Aspect.ORDER),
                new ResourceLocation("arcaneresearch", "item/wand/rod_quartz_mat"));
        STAFF_ROD_BONE = new WandRod("bone_staff", 500, new ItemStack(Items.BONE), 14,
                new WandRodPrimalOnUpdate(Aspect.ENTROPY),
                new ResourceLocation("arcaneresearch", "item/wand/rod_bone_mat"));
        STAFF_ROD_REED = new WandRod("reed_staff", 500, new ItemStack(Items.SUGAR_CANE), 14,
                new WandRodPrimalOnUpdate(Aspect.AIR),
                new ResourceLocation("arcaneresearch", "item/wand/rod_reed_mat"));
        STAFF_ROD_GREATWOOD.setStaff(true);
        STAFF_ROD_SILVERWOOD.setStaff(true);
        STAFF_ROD_OBSIDIAN.setStaff(true);
        STAFF_ROD_BLAZE.setStaff(true);
        STAFF_ROD_ICE.setStaff(true);
        STAFF_ROD_QUARTZ.setStaff(true);
        STAFF_ROD_BONE.setStaff(true);
        STAFF_ROD_REED.setStaff(true);
        STAFF_ROD_PRIMAL = new WandRod("primal_staff", 750, new ItemStack(Items.BLAZE_ROD), 28,
                new WandRodPrimalOnUpdate(),
                new ResourceLocation("arcaneresearch", "item/wand/rod_primal_mat"));
        STAFF_ROD_PRIMAL.setStaff(true);
        STAFF_ROD_PRIMAL.setPotencyBonus(true);
    }
}
