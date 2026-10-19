package arcaneresearch.common.items.consumables;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import arcaneresearch.common.entities.projectile.EntityAlumentum;
import javax.annotation.Nullable;
public class ItemAlumentum extends Item {
    public ItemAlumentum() {
        super(new Properties().stacksTo(64));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                0.3f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
        if (!level.isClientSide) {
            EntityAlumentum alumentum = new EntityAlumentum(level, player);
            alumentum.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.4f, 2.0f);
            level.addFreshEntity(alumentum);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return 4800;
    }
}
