package arcaneresearch.common.items.consumables;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.entities.projectile.EntityCausalityCollapser;
public class ItemCausalityCollapser extends Item {
    public ItemCausalityCollapser() {
        super(new Item.Properties()
                .stacksTo(16));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.playSound(SoundEvents.EGG_THROW, 0.3f,
                0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide()) {
            EntityCausalityCollapser proj = new EntityCausalityCollapser(level, player);
            proj.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.8f, 2.0f);
            level.addFreshEntity(proj);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
