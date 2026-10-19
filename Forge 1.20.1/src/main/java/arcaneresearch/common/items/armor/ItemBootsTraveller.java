package arcaneresearch.common.items.armor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import java.util.UUID;
public class ItemBootsTraveller extends ArmorItem implements IRechargable, IRunicArmor {
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }
    private static final UUID STEP_UUID =
            UUID.fromString("0d3d95f5-4a9e-4c8a-9b2e-6f2a6c9e4b11");
    private static final double STEP_BONUS = 0.4;
    public ItemBootsTraveller() {
        super(ArcaneresearchMaterials.ARMORMAT_SPECIAL, Type.BOOTS,
                new Item.Properties()
                        .stacksTo(1)
                        .durability(350)
                        .rarity(Rarity.RARE));
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.LEATHER) || super.isValidRepairItem(toRepair, repair);
    }
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "arcaneresearch:textures/entity/armor/bootstraveler.png";
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }
        boolean worn = player.getItemBySlot(EquipmentSlot.FEET) == stack;
        boolean hasCharge = RechargeHelper.getCharge(stack) > 0;
        if (worn && !level.isClientSide() && entity.tickCount % 20 == 0) {
            int e = stack.hasTag() ? stack.getTag().getInt("energy") : 0;
            if (e > 0) {
                e--;
            } else if (RechargeHelper.consumeCharge(stack, player, 1)) {
                e = 60;
            }
            stack.getOrCreateTag().putInt("energy", e);
        }
        AttributeInstance step = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        boolean stepApplies = worn && hasCharge && !player.isShiftKeyDown();
        if (step != null) {
            boolean present = step.getModifier(STEP_UUID) != null;
            if (stepApplies && !present) {
                step.addTransientModifier(new AttributeModifier(
                        STEP_UUID, "Traveller step", STEP_BONUS, AttributeModifier.Operation.ADDITION));
            } else if (!stepApplies && present) {
                step.removeModifier(STEP_UUID);
            }
        }
        if (worn && hasCharge && !player.getAbilities().flying && player.zza > 0.0F) {
            if (player.onGround()) {
                float bonus = 0.05F;
                if (player.isInWater()) {
                    bonus /= 4.0F;
                }
                player.moveRelative(1.0F, new Vec3(0.0D, 0.0D, bonus));
            } else {
                if (player.isInWater()) {
                    player.moveRelative(1.0F, new Vec3(0.0D, 0.0D, 0.025F));
                }
                Vec3 look = player.getLookAngle();
                Vec3 flat = new Vec3(look.x, 0.0D, look.z);
                if (flat.lengthSqr() > 1.0E-6) {
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.add(
                            flat.normalize().scale(player.zza * 0.03D)));
                }
            }
        }
    }
    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity entity) {
        return 240;
    }
    @Override
    public EnumChargeDisplay showInHud(ItemStack stack, LivingEntity entity) {
        return EnumChargeDisplay.PERIODIC;
    }
}
