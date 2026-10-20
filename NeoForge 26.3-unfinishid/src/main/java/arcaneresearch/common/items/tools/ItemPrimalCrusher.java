package arcaneresearch.common.items.tools;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.ArcaneresearchToolMaterials;
import arcaneresearch.api.items.IWarpingGear;

public class ItemPrimalCrusher extends Item implements IWarpingGear {

    // NEOFORGE_PORT: Forge Tier -> ToolMaterial record; canonical definition lives in
    // ArcaneresearchToolMaterials (values verbatim from Forge PRIMAL_VOID_TIER:
    // uses 500, speed 8.0, bonus 4.0, level 5, enchant 20, repair void ingot).
    // Field name kept so existing references keep resolving.
    public static final ToolMaterial PRIMAL_VOID_TIER = ArcaneresearchToolMaterials.TOOLMAT_PRIMAL_VOID;

    public ItemPrimalCrusher(Item.Properties props) {
        // NEOFORGE_PORT: DiggerItem REMOVED in 26.x -> plain Item with
        // .pickaxe(material, damage, speed). stacksTo(20) kept 1:1 from Forge.
        super(props
                .stacksTo(20)
                .pickaxe(PRIMAL_VOID_TIER, 3.5F, -2.8F));
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)
                // NEOFORGE_PORT: BlockTags.TALL_FLOWERS removed in 26.x (tall
                // flowers now ride on FLOWERS); the check above covers them.
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)) {
            return false;
        }
        // NEOFORGE_PORT: TierSortingRegistry removed with tiers; the top-tier
        // check is the material's incorrectBlocksForDrops tag (netherite tier).
        return !state.is(PRIMAL_VOID_TIER.incorrectBlocksForDrops());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return PRIMAL_VOID_TIER.speed();
        }
        return super.getDestroySpeed(stack, state);
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_void)
    // -> REPAIRABLE component, so no override is needed (or possible).

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0
                // NEOFORGE_PORT (26.3): Forge only ticked the player inventory (mob gear never
                // reached inventoryTick); 26.3 EntityEquipment.tick also ticks mob equipment.
                && entity instanceof Player) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
}
