package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ElementalPickaxeItem extends Item implements ElementalTool {
    public ElementalPickaxeItem(Item.Properties props) {
        super(ElementalTool.createProperties(props).pickaxe(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 1, -2.8F));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel serverLevel
                && serverLevel.isPvpAllowed()
                && !(target instanceof Player)) {
            target.setRemainingFireTicks(40);
        }
        super.hurtEnemy(stack, target, attacker);
    }
}
