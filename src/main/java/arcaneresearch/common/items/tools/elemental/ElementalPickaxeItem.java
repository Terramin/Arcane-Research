package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ElementalPickaxeItem extends PickaxeItem implements ElementalTool {
    public ElementalPickaxeItem() {
        super(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 1, -2.8F,
                ElementalTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ElementalTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || isPvPEnabled(attacker.level()))) {
            target.setSecondsOnFire(2);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private boolean isPvPEnabled(Level level) {
        return level.getServer() == null || level.getServer().isPvpAllowed();
    }
}
