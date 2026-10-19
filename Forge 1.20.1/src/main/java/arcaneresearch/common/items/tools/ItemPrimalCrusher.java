package arcaneresearch.common.items.tools;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.init.ModItems;
public class ItemPrimalCrusher extends DiggerItem implements IWarpingGear {
    public static final Tier PRIMAL_VOID_TIER = new Tier() {
        @Override
        public int getUses() {
            return 500;
        }
        @Override
        public float getSpeed() {
            return 8.0f;
        }
        @Override
        public float getAttackDamageBonus() {
            return 4.0f;
        }
        @Override
        public int getLevel() {
            return 5;
        }
        @Override
        public int getEnchantmentValue() {
            return 20;
        }
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.VOID_METAL_INGOT.get());
        }
    };
    public ItemPrimalCrusher() {
        super(3.5f,
                -2.8f,
                PRIMAL_VOID_TIER,
                BlockTags.MINEABLE_WITH_PICKAXE,
                new Item.Properties().stacksTo(20));
    }
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS) || state.is(BlockTags.TALL_FLOWERS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)) {
            return false;
        }
        return TierSortingRegistry.isCorrectTierForDrops(PRIMAL_VOID_TIER, state);
    }
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return PRIMAL_VOID_TIER.getSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
}
