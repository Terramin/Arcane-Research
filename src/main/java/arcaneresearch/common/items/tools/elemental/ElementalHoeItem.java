package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ElementalHoeItem extends Item implements ElementalTool {
    public ElementalHoeItem(Item.Properties props) {
        super(ElementalTool.createProperties(props)
                .hoe(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, -3, 0.0F));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        boolean did = false;
        ItemStack stack = ctx.getItemInHand();
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                BlockPos pp = pos.offset(xx, 0, zz);
                UseOnContext offset = new UseOnContext(level, player, ctx.getHand(), stack,
                        new net.minecraft.world.phys.BlockHitResult(ctx.getClickLocation(),
                                ctx.getClickedFace(), pp, false));
                if (super.useOn(offset).consumesAction()) {
                    did = true;
                }
            }
        }
        if (!did) {
            did = arcaneresearch.common.lib.utils.Utils.useBonemealAtLoc(level, player, pos);
        }
        return did ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
