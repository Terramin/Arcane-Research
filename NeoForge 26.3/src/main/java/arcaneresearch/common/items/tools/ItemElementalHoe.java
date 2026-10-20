package arcaneresearch.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.Utils;

public class ItemElementalHoe extends Item {

    public ItemElementalHoe(Item.Properties props) {
        // NEOFORGE_PORT: HoeItem REMOVED in 26.x -> plain Item with .hoe(material, damage, speed).
        // stacksTo(5) kept 1:1 from Forge (unusual for a tool, but verbatim).
        super(props
                .rarity(Rarity.RARE)
                .stacksTo(5)
                .hoe(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, -3, 0.0F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            return super.useOn(ctx);
        }
        boolean did = false;
        ItemStack stack = ctx.getItemInHand();
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                BlockPos pp = pos.offset(xx, 0, zz);
                UseOnContext offset = new UseOnContext(level, player, ctx.getHand(), stack,
                        new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), pp, false));
                if (super.useOn(offset).consumesAction()) {
                    if (level.isClientSide()) {
                        doBamfFX(pp, xx == 0 && zz == 0);
                    }
                    did = true;
                }
            }
        }
        if (!did) {
            did = Utils.useBonemealAtLoc(level, player, pos);
            if (did) {
                if (!level.isClientSide()) {
                    // NEOFORGE_PORT: broadcastBreakEvent consumer overload REMOVED;
                    // hurtAndBreak(int, LivingEntity, InteractionHand) is the direct replacement.
                    stack.hurtAndBreak(3, player, ctx.getHand());
                    level.levelEvent(2005, pos, 0);
                } else {
                    doMistFX(pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void doBamfFX(BlockPos pp, boolean center) {
        FXDispatcher.INSTANCE.drawBamf(pp.getX() + 0.5D, pp.getY() + 1.01D, pp.getZ() + 0.5D,
                0.3F, 0.12F, 0.1F, center, false, Direction.UP);
    }

    @OnlyIn(Dist.CLIENT)
    private void doMistFX(BlockPos pos) {
        FXDispatcher.INSTANCE.drawBlockMistParticles(pos, 4259648);
    }
}
