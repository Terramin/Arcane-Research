package arcaneresearch.client;

import java.util.EnumMap;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.armor.CultistPlateArmorModel;
import arcaneresearch.client.models.armor.CultistRobeArmorModel;
import arcaneresearch.client.models.armor.FortressArmorModel;
import arcaneresearch.client.models.armor.LeaderArmorModel;
import arcaneresearch.client.models.armor.RobeArmorModel;
import arcaneresearch.client.models.armor.ThaumiumArmorModel;
import arcaneresearch.client.models.armor.VoidArmorModel;
import arcaneresearch.client.models.armor.VoidRobeArmorModel;
import arcaneresearch.init.ModItems;

// NEOFORGE_PORT (26.x armor rework): Forge 1.20.1 drew mod armor via
// Item.initializeClient -> IClientItemExtensions.getHumanoidArmorModel(entity,
// stack, slot, original) + ArmorItem.getArmorTexture -> String. In 26.x:
//  - Item.initializeClient is REMOVED -> extensions register here via
//    RegisterClientExtensionsEvent (same pattern as WandClientSetup).
//  - The hook is now getHumanoidArmorModel(stack, layerType, original); pose
//    copying (crouching/riding/arm poses) is done by the default
//    getGenericArmorModel via ClientHooks.copyModelProperties (verified via
//    javap), so only the Forge per-slot part visibility is kept here.
//  - Textures resolve per equipment-asset LAYER
//    (assets/arcaneresearch/equipment/*.json, REQUIRED: empty layers render
//    nothing) with per-item override in getArmorTexture -> classic
//    textures/entity/armor/*.png paths, so no PNG is moved or duplicated.
//  - Slot comes from the stack's EQUIPPABLE component (Item no longer knows
//    its own slot); LEGS -> HUMANOID_LEGGINGS -> inner model, like Forge.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class ArmorClientSetup {

    private interface TextureFn {
        Identifier get(boolean legs, EquipmentClientInfo.Layer layer);
    }

    private static Identifier tex(String path) {
        return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, path);
    }

    // NEOFORGE_PORT (26.3): Forge chose model/texture by the worn slot (LEGS -> inner);
    // the slot is the stack's EQUIPPABLE slot (layer type is HUMANOID_BABY for babies).
    private static EquipmentSlot slotOf(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null ? equippable.slot() : EquipmentSlot.CHEST;
    }

    // Forge parity (every Item*Armor.initializeClient switch 1:1).
    // NEOFORGE_PORT: HumanoidModel.setAllVisible removed in 26.x -> explicit flags.
    private static void applySlotVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    private static boolean isOverlay(EquipmentClientInfo.Layer layer) {
        return layer.textureId().getPath().endsWith("_overlay");
    }

    private static final class ArmorExtension implements IClientItemExtensions {
        private final Function<ModelPart, HumanoidModel<?>> outerFactory;
        private final Function<ModelPart, HumanoidModel<?>> innerFactory;
        private final ModelLayerLocation outerLocation;
        private final ModelLayerLocation innerLocation;
        private final TextureFn textureFn;
        private final boolean forceUntinted;
        // NEOFORGE_PORT (26.3): one model per slot. Models are drawn after all submits and
        // getGenericArmorModel copies the vanilla (all-visible) part flags, so a shared
        // instance would show the last slot's / every part for each worn piece.
        private final EnumMap<EquipmentSlot, HumanoidModel<?>> models = new EnumMap<>(EquipmentSlot.class);

        /** Custom-model armor (Forge initializeClient parity). */
        ArmorExtension(Function<ModelPart, HumanoidModel<?>> outerFactory,
                ModelLayerLocation outerLocation,
                Function<ModelPart, HumanoidModel<?>> innerFactory,
                ModelLayerLocation innerLocation,
                TextureFn textureFn) {
            this(outerFactory, outerLocation, innerFactory, innerLocation, textureFn, false);
        }

        ArmorExtension(Function<ModelPart, HumanoidModel<?>> outerFactory,
                ModelLayerLocation outerLocation,
                Function<ModelPart, HumanoidModel<?>> innerFactory,
                ModelLayerLocation innerLocation,
                TextureFn textureFn, boolean forceUntinted) {
            this.outerFactory = outerFactory;
            this.outerLocation = outerLocation;
            this.innerFactory = innerFactory;
            this.innerLocation = innerLocation;
            this.textureFn = textureFn;
            this.forceUntinted = forceUntinted;
        }

        /** Vanilla-model armor (Forge texture-only items: goggles, boots). */
        ArmorExtension(TextureFn textureFn, boolean forceUntinted) {
            this(null, null, null, null, textureFn, forceUntinted);
        }

        private HumanoidModel<?> model(EquipmentSlot slot) {
            return models.computeIfAbsent(slot, s -> s == EquipmentSlot.LEGS
                    ? innerFactory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(innerLocation))
                    : outerFactory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(outerLocation)));
        }

        @Override
        public Model getHumanoidArmorModel(ItemStack stack,
                EquipmentClientInfo.LayerType layerType, Model original) {
            if (outerFactory == null) {
                return original;
            }
            EquipmentSlot slot = slotOf(stack);
            HumanoidModel<?> model = model(slot);
            applySlotVisibility(model, slot);
            return model;
        }

        @Override
        public Model getGenericArmorModel(ItemStack stack,
                EquipmentClientInfo.LayerType layerType, Model original) {
            Model replacement = getHumanoidArmorModel(stack, layerType, original);
            if (replacement != original && original instanceof HumanoidModel<?> originalHumanoid
                    && replacement instanceof HumanoidModel<?> replacementHumanoid) {
                // Default behaviour (pose copy), then restore the Forge per-slot visibility
                // that copyModelProperties overwrote.
                net.neoforged.neoforge.client.ClientHooks.copyModelProperties(originalHumanoid, replacementHumanoid);
                applySlotVisibility(replacementHumanoid, slotOf(stack));
            }
            return replacement;
        }

        @Override
        public Identifier getArmorTexture(ItemStack stack,
                EquipmentClientInfo.LayerType layerType,
                EquipmentClientInfo.Layer layer, Identifier fallback) {
            return textureFn.get(slotOf(stack) == EquipmentSlot.LEGS, layer);
        }

        @Override
        public int getArmorLayerTintColor(ItemStack stack,
                EquipmentClientInfo.Layer layer, int layerIndex, int dyeColor) {
            if (forceUntinted) {
                // Forge parity: goggles / traveller boots rendered untinted,
                // but share the dyeable "special" asset with robes.
                return -1;
            }
            return IClientItemExtensions.super.getArmorLayerTintColor(stack, layer, layerIndex, dyeColor);
        }
    }

    // Forge parity tables (Item*Armor.getArmorTexture 1:1).
    private static final TextureFn THAUMIUM_TEX = (legs, l) -> tex(legs
            ? "textures/entity/armor/thaumium_2.png" : "textures/entity/armor/thaumium_1.png");
    private static final TextureFn VOID_TEX = (legs, l) -> tex(legs
            ? "textures/entity/armor/void_2.png" : "textures/entity/armor/void_1.png");
    private static final TextureFn ROBE_TEX = (legs, l) -> {
        if (legs) {
            return tex(isOverlay(l) ? "textures/entity/armor/robes_2_overlay.png"
                    : "textures/entity/armor/robes_2.png");
        }
        return tex(isOverlay(l) ? "textures/entity/armor/robes_1_overlay.png"
                : "textures/entity/armor/robes_1.png");
    };
    // Forge parity: ItemVoidRobeArmor quirk — the base call (type == null)
    // returns the *_overlay.png (dyed), the overlay call the plain png.
    private static final TextureFn VOID_ROBE_TEX = (legs, l) -> tex(isOverlay(l)
            ? "textures/entity/armor/void_robe_armor.png"
            : "textures/entity/armor/void_robe_armor_overlay.png");
    private static final TextureFn FORTRESS_TEX =
            (legs, l) -> tex("textures/entity/armor/fortress_armor.png");
    // NEOFORGE_PORT (26.3): Forge picked zombie_plate_armor.png when worn by
    // EntityInhabitedZombie; the 26.x hooks carry no entity, so InhabitedZombieRenderer
    // raises this flag around its (synchronous) submit, where the texture is resolved.
    public static boolean inhabitedZombieWearer;
    private static final TextureFn CULTIST_PLATE_TEX = (legs, l) -> tex(inhabitedZombieWearer
            ? "textures/entity/armor/zombie_plate_armor.png"
            : "textures/entity/armor/cultist_plate_armor.png");
    private static final TextureFn CULTIST_ROBE_TEX =
            (legs, l) -> tex("textures/entity/armor/cultist_robe_armor.png");
    private static final TextureFn LEADER_TEX =
            (legs, l) -> tex("textures/entity/armor/cultist_leader_armor.png");
    private static final TextureFn GOGGLES_TEX =
            (legs, l) -> tex("textures/entity/armor/goggles.png");
    private static final TextureFn TRAVELLER_TEX =
            (legs, l) -> tex("textures/entity/armor/bootstraveler.png");
    private static final TextureFn CULTIST_BOOTS_TEX =
            (legs, l) -> tex("textures/entity/armor/cultistboots.png");

    private static final ArmorExtension THAUMIUM = new ArmorExtension(
            ThaumiumArmorModel::new, ThaumiumArmorModel.LAYER_LOCATION,
            ThaumiumArmorModel::new, ThaumiumArmorModel.INNER_LAYER_LOCATION, THAUMIUM_TEX);
    private static final ArmorExtension VOID = new ArmorExtension(
            VoidArmorModel::new, VoidArmorModel.LAYER_LOCATION,
            VoidArmorModel::new, VoidArmorModel.INNER_LAYER_LOCATION, VOID_TEX);
    private static final ArmorExtension ROBE = new ArmorExtension(
            RobeArmorModel::new, RobeArmorModel.LAYER_LOCATION,
            RobeArmorModel::new, RobeArmorModel.INNER_LAYER_LOCATION, ROBE_TEX);
    private static final ArmorExtension VOID_ROBE = new ArmorExtension(
            VoidRobeArmorModel::new, VoidRobeArmorModel.LAYER_LOCATION,
            VoidRobeArmorModel::new, VoidRobeArmorModel.LAYER_LOCATION, VOID_ROBE_TEX);
    private static final ArmorExtension FORTRESS = new ArmorExtension(
            FortressArmorModel::new, FortressArmorModel.LAYER_LOCATION,
            FortressArmorModel::new, FortressArmorModel.LAYER_LOCATION, FORTRESS_TEX);
    private static final ArmorExtension CULTIST_PLATE = new ArmorExtension(
            CultistPlateArmorModel::new, CultistPlateArmorModel.LAYER_LOCATION,
            CultistPlateArmorModel::new, CultistPlateArmorModel.LAYER_LOCATION, CULTIST_PLATE_TEX);
    private static final ArmorExtension CULTIST_ROBE = new ArmorExtension(
            CultistRobeArmorModel::new, CultistRobeArmorModel.LAYER_LOCATION,
            CultistRobeArmorModel::new, CultistRobeArmorModel.LAYER_LOCATION, CULTIST_ROBE_TEX);
    private static final ArmorExtension LEADER = new ArmorExtension(
            LeaderArmorModel::new, LeaderArmorModel.LAYER_LOCATION,
            LeaderArmorModel::new, LeaderArmorModel.LAYER_LOCATION, LEADER_TEX);
    private static final ArmorExtension GOGGLES = new ArmorExtension(GOGGLES_TEX, true);
    private static final ArmorExtension TRAVELLER = new ArmorExtension(TRAVELLER_TEX, true);
    private static final ArmorExtension CULTIST_BOOTS = new ArmorExtension(CULTIST_BOOTS_TEX, true);

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(THAUMIUM,
                ModItems.THAUMIUM_HELM.get(), ModItems.THAUMIUM_CHEST.get(),
                ModItems.THAUMIUM_LEGS.get(), ModItems.THAUMIUM_BOOTS.get());
        event.registerItem(VOID,
                ModItems.VOID_HELM.get(), ModItems.VOID_CHEST.get(),
                ModItems.VOID_LEGS.get(), ModItems.VOID_BOOTS.get());
        event.registerItem(FORTRESS,
                ModItems.FORTRESS_HELM.get(), ModItems.FORTRESS_CHEST.get(),
                ModItems.FORTRESS_LEGS.get());
        event.registerItem(VOID_ROBE,
                ModItems.VOID_ROBE_HELM.get(), ModItems.VOID_ROBE_CHEST.get(),
                ModItems.VOID_ROBE_LEGS.get());
        event.registerItem(ROBE,
                ModItems.CLOTH_CHEST.get(), ModItems.CLOTH_LEGS.get(),
                ModItems.CLOTH_BOOTS.get());
        event.registerItem(CULTIST_PLATE,
                ModItems.CRIMSON_PLATE_HELM.get(), ModItems.CRIMSON_PLATE_CHEST.get(),
                ModItems.CRIMSON_PLATE_LEGS.get());
        event.registerItem(CULTIST_ROBE,
                ModItems.CRIMSON_ROBE_HELM.get(), ModItems.CRIMSON_ROBE_CHEST.get(),
                ModItems.CRIMSON_ROBE_LEGS.get());
        event.registerItem(LEADER,
                ModItems.CRIMSON_PRAETOR_HELM.get(), ModItems.CRIMSON_PRAETOR_CHEST.get(),
                ModItems.CRIMSON_PRAETOR_LEGS.get());
        event.registerItem(GOGGLES, ModItems.GOGGLES.get());
        event.registerItem(TRAVELLER, ModItems.TRAVELLER_BOOTS.get());
        event.registerItem(CULTIST_BOOTS, ModItems.CRIMSON_BOOTS.get());
    }
}
