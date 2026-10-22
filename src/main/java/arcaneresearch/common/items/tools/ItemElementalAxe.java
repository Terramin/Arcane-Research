package arcaneresearch.common.items.tools;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;

import java.util.List;

public class ItemElementalAxe extends Item {

    public ItemElementalAxe(Item.Properties props) {
        
        super(props
                .rarity(Rarity.RARE)
                .axe(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 5.0F, -3.0F));
    }

    
    
    

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        List<ItemEntity> stuff = level.getEntitiesOfClass(ItemEntity.class,
                user.getBoundingBox().inflate(10.0D), e -> !e.isRemoved());
        if (stuff != null && !stuff.isEmpty()) {
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
                (float) ep.x + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F,
                (float) ep.y + e.getBbHeight()
                        + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F,
                (float) ep.z + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F,
                0.33F, 0.33F, 1.0F);
    }
}
