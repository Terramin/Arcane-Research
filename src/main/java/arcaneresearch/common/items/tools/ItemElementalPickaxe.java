package arcaneresearch.common.items.tools;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemElementalPickaxe extends Item {

    public ItemElementalPickaxe(Item.Properties props) {
        
        super(props
                .rarity(Rarity.RARE)
                .pickaxe(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 1, -2.8F));
    }

    
    
    

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || isPvPEnabled(attacker.level()))) {
            
            target.igniteForSeconds(2);
        }
        super.hurtEnemy(stack, target, attacker);
    }

    private boolean isPvPEnabled(Level level) {
        if (level instanceof net.minecraft.server.level.ServerLevel pvpLevel) {
            return pvpLevel.isPvpAllowed();
        }
        return true;
    }
}
