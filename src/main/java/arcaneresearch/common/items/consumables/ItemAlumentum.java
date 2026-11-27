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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ItemAlumentum extends Item {
    public ItemAlumentum() {
        super(new Properties().stacksTo(AlumentumRules.STACK_SIZE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                AlumentumRules.SOUND_VOLUME, AlumentumRules.soundPitch(level.random.nextFloat()));
        if (!level.isClientSide) {
            EntityAlumentum alumentum = new EntityAlumentum(level, player);
            alumentum.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    AlumentumRules.THROW_ANGLE, AlumentumRules.THROW_INACCURACY,
                    AlumentumRules.THROW_POWER);
            level.addFreshEntity(alumentum);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return AlumentumRules.BURN_TIME;
    }
}
