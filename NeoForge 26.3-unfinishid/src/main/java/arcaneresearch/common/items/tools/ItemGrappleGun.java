package arcaneresearch.common.items.tools;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.common.entities.projectile.EntityGrapple;
import arcaneresearch.init.ModSounds;

public class ItemGrappleGun extends Item implements IRechargable {

    public ItemGrappleGun(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity entity) {
        return 100;
    }

    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, LivingEntity entity) {
        return EnumChargeDisplay.NORMAL;
    }

    public boolean isLoaded(ItemStack stack) {
        // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA via ArcaneresearchInvHelper (COPY semantics).
        CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null) {
            return tag.getByteOr("loaded", (byte) 0) == 1;
        }
        return false;
    }

    public void setLoaded(ItemStack stack, boolean loaded) {
        // NEOFORGE_PORT: getOrCreateTag() REMOVED -> get copy, mutate, write back.
        CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
        tag.putByte("loaded", (byte) (loaded ? 1 : 0));
        ArcaneresearchInvHelper.setCustomTag(stack, tag);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (!EntityGrapple.grapples.containsKey(entity.getId())
                && ArcaneresearchInvHelper.hasCustomTag(stack)
                && ArcaneresearchInvHelper.getCustomTag(stack).getByteOr("loaded", (byte) 0) == 1) {
            CompoundTag tag = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            tag.putByte("loaded", (byte) 0);
            ArcaneresearchInvHelper.setCustomTag(stack, tag);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                ModSounds.ICE.get(), SoundSource.PLAYERS, 3.0f, 0.8f + level.getRandom().nextFloat() * 0.1f);

        if (!level.isClientSide() && RechargeHelper.getCharge(stack) > 0) {
            EntityGrapple grapple = new EntityGrapple(level, player, hand);
            grapple.shootFromRotation(player, player.getXRot(), player.getYRot(), -5.0f, 1.5f, 0.0f);

            double handSign = (grapple.getHand() == InteractionHand.MAIN_HAND) ? 1.0 : -1.0;
            double px = -Mth.cos((player.getYRot() - 0.5f) / 180.0f * (float) Math.PI) * 0.2f * handSign;
            double pz = -Mth.sin((player.getYRot() - 0.5f) / 180.0f * (float) Math.PI) * 0.3f * handSign;
            Vec3 look = player.getLookAngle();

            grapple.setPos(grapple.getX() + px + look.x, grapple.getY(), grapple.getZ() + pz + look.z);

            if (level.addFreshEntity(grapple)) {
                RechargeHelper.consumeCharge(stack, player, 1);
                setLoaded(stack, true);
            }
        }

        // NEOFORGE_PORT: InteractionResultHolder.sidedSuccess REMOVED (and use() now returns
        // plain InteractionResult); SUCCESS preserves the both-sides-success semantics.
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == this && newStack.getItem() == this
                && !ItemStack.isSameItemSameComponents(oldStack, newStack)) {

            CompoundTag oldTag = ArcaneresearchInvHelper.getCustomTag(oldStack);
            CompoundTag newTag = ArcaneresearchInvHelper.getCustomTag(newStack);
            boolean wasLoaded = oldTag == null || oldTag.getByteOr("loaded", (byte) 0) == 0;
            boolean isLoaded = newTag == null || newTag.getByteOr("loaded", (byte) 0) == 0;
            return wasLoaded != isLoaded;
        }
        return oldStack.getItem() != newStack.getItem();
    }
}
