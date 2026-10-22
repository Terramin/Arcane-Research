package arcaneresearch.common.items.consumables;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.entities.projectile.EntityBottleTaint;

public class ItemBottleTaint extends Item {

    public ItemBottleTaint(Item.Properties props) {
        super(props
                .stacksTo(8));
    }

    
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.playSound(SoundEvents.EGG_THROW, 0.5f,
                0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {
            EntityBottleTaint bottle = new EntityBottleTaint(level, player);
            bottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.66f, 1.0f);
            level.addFreshEntity(bottle);
        }

        return InteractionResult.SUCCESS;
    }
}
