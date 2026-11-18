package arcaneresearch.common.items.tools;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModItems;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemElementalAxe extends AxeItem {
    public ItemElementalAxe() {
        super(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 5.0F, -3.0F,
                new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, @NotNull ItemStack stack, int remainingUseDuration) {
        List<ItemEntity> stuff = level.getEntitiesOfClass(ItemEntity.class,
                user.getBoundingBox().inflate(10.0D), e -> !e.isRemoved());
        if (!stuff.isEmpty()) {
            for (ItemEntity e : stuff) {
                if (!e.isRemoved()) {
                    Vec3 ep = e.position();
                    Vec3 pp = user.position();
                    double d6 = ep.x - pp.x;
                    double d8 = ep.y - pp.y + user.getBbHeight() / 2.0F;
                    double d10 = ep.z - pp.z;
                    double dist = Math.sqrt(d6 * d6 + d8 * d8 + d10 * d10);
                    d6 /= dist;
                    d8 /= dist;
                    d10 /= dist;
                    Vec3 m = e.getDeltaMovement().add(-d6 * 0.3D, -d8 * 0.3D + 0.1D, -d10 * 0.3D);
                    e.setDeltaMovement(Mth.clamp(m.x, -0.25D, 0.25D),
                            Mth.clamp(m.y, -0.25D, 0.25D), Mth.clamp(m.z, -0.25D, 0.25D));
                    if (level.isClientSide()) {
                        doBubbleFX(level, e, ep);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doBubbleFX(Level level, ItemEntity e, Vec3 ep) {
        FXDispatcher.INSTANCE.crucibleBubble(
                (float) ep.x + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F,
                (float) ep.y + e.getBbHeight()
                        + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F,
                (float) ep.z + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F,
                0.33F, 0.33F, 1.0F);
    }
}
