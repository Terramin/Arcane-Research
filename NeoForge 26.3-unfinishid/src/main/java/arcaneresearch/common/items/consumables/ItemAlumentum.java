package arcaneresearch.common.items.consumables;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.entities.projectile.EntityAlumentum;

public class ItemAlumentum extends Item {

    public ItemAlumentum(Item.Properties props) {
        super(props.stacksTo(64));
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // InteractionResultHolder.sidedSuccess(stack, client) -> InteractionResult.SUCCESS.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                0.3f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide()) {
            EntityAlumentum alumentum = new EntityAlumentum(level, player);
            alumentum.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 0.4f, 2.0f);
            level.addFreshEntity(alumentum);
        }

        return InteractionResult.SUCCESS;
    }

    // NEOFORGE_PORT: Forge getBurnTime override (4800 ticks) -> COOKING_FUEL component,
    // see init/ModFuels + data/arcaneresearch/context_int_provider/cooking/time_alumentum.json.
}
