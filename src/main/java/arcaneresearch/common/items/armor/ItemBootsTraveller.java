package arcaneresearch.common.items.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;

// NEOFORGE_PORT: ArmorItem REMOVED in 26.x (verified via javap) -> plain Item with
// .humanoidArmor(material, type). ForgeMod.STEP_HEIGHT_ADDITION -> vanilla Attributes.STEP_HEIGHT.
public class ItemBootsTraveller extends Item implements IRechargable, IRunicArmor {

    /**
     * Паритет TC4: вся таум-броня — IRunicArmor с нулевым базовым зарядом
     * (пул собирается из TC.RUNIC от рунического аугмента).
     */
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    // NEOFORGE_PORT: AttributeModifier(UUID, ...) REMOVED -> (Identifier, amount, Operation);
    // Operation.ADDITION -> ADD_VALUE (verified via javap).
    private static final Identifier STEP_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "traveller_step");

    private static final double STEP_BONUS = 0.4;

    public ItemBootsTraveller(Item.Properties props) {
        // NEOFORGE_PORT: super(material, Type.BOOTS, props) -> super(props.humanoidArmor(material, ArmorType.BOOTS)).
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_SPECIAL, ArmorType.BOOTS)
                // Forge isValidRepairItem: leather or the material's enchanted fabric.
                .repairable(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID, "repairs_traveller_boots")))
                .stacksTo(1)
                .durability(350)
                .rarity(Rarity.RARE));
    }

    // NEOFORGE_PORT: Forge isValidRepairItem (leather || material match) -> REPAIRABLE
    // component with the repairs_traveller_boots tag (leather + #repairs_enchanted_fabric).

    // NEOFORGE_PORT: Forge armor-model/texture parity -> arcaneresearch.client.ArmorClientSetup.

    @Override
    // NEOFORGE_PORT: inventoryTick(ItemStack, Level, Entity, int, boolean) REMOVED in 26.x ->
    // inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot) (server-only hook).
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) {
            return;
        }
        boolean worn = player.getItemBySlot(EquipmentSlot.FEET) == stack;
        boolean hasCharge = RechargeHelper.getCharge(stack) > 0;

        // NEOFORGE_PORT: level is ServerLevel so the old !level.isClientSide() guard is implied.
        // NBT root tag -> CUSTOM_DATA via ArcaneresearchInvHelper (getCustomTag returns a COPY,
        // so the mutated tag is written back with setCustomTag).
        if (worn && entity.tickCount % 20 == 0) {
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            int e = root.getIntOr("energy", 0);
            if (e > 0) {
                e--;
            } else if (RechargeHelper.consumeCharge(stack, player, 1)) {
                e = 60;
            }
            root.putInt("energy", e);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }

        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);

        boolean stepApplies = worn && hasCharge && !player.isShiftKeyDown();
        if (step != null) {
            boolean present = step.getModifier(STEP_ID) != null;
            if (stepApplies && !present) {
                // NEOFORGE_PORT: AttributeModifier is a record (id, amount, op); no name.
                step.addTransientModifier(new AttributeModifier(
                        STEP_ID, STEP_BONUS, AttributeModifier.Operation.ADD_VALUE));
            } else if (!stepApplies && present) {
                step.removeModifier(STEP_ID);
            }
        }

        applyMovementAssist(player, worn && hasCharge);
    }

    // NEOFORGE_PORT: Forge ran this movement assist on both sides from inventoryTick. The 26.x
    // inventoryTick is server-only while player movement is client-authoritative, so the local
    // player's half is driven from PlayerTickEvent (ClientMotion below). Logic is 1:1.
    private static void applyMovementAssist(Player player, boolean active) {
        if (active && !player.getAbilities().flying && player.zza > 0.0F) {
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

    @net.neoforged.fml.common.EventBusSubscriber(modid = Arcaneresearch.MODID)
    public static final class ClientMotion {
        private ClientMotion() {
        }

        @net.neoforged.bus.api.SubscribeEvent
        public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
            Player player = event.getEntity();
            if (!player.level().isClientSide() || !player.isLocalPlayer()) {
                return;
            }
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.getItem() instanceof ItemBootsTraveller) {
                applyMovementAssist(player, RechargeHelper.getCharge(boots) > 0);
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
