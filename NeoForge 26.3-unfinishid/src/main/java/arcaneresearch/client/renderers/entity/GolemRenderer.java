package arcaneresearch.client.renderers.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.parts.GolemMaterial;
import arcaneresearch.client.models.entity.GolemModel;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.lib.utils.EntityUtils;

// NEOFORGE_PORT (26.3): MobRenderer<T, S, M>. All entity reads for the OBJ
// layer (props/material, motion lean, swing, ghost/xray visibility, held and
// carried items resolved via ItemModelResolver) happen in extractRenderState;
// GolemObjLayer.submit draws from State only. Ghost/xray passes preserved.
public class GolemRenderer extends MobRenderer<EntityArcaneresearchGolem, GolemRenderer.State, GolemModel> {

    public static class State extends LivingEntityRenderState {
        public EntityArcaneresearchGolem golem;
        public float partialTicks;
        public Identifier matTexture;
        public boolean holding;
        public boolean flyerLike;
        public boolean flash;
        public double moveSpeedSq;
        public float turnDelta;
        public float swing;
        public boolean ghostOn;
        public boolean sealXray;
        public final ItemStackRenderState mainHandState = new ItemStackRenderState();
        public final ItemStackRenderState carriedState = new ItemStackRenderState();
        public boolean mainHandIsBlock;
        public boolean carriedIsBlock;
    }

    private static final Identifier DEFAULT_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/golems/mat_wood.png");

    private final ItemModelResolver itemModelResolver;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 0.3F);
        this.itemModelResolver = context.getItemModelResolver();
        this.addLayer(new GolemObjLayer(this));
    }

    private static boolean holdsSealDisplayer(Player player) {
        return player.getMainHandItem().getItem() instanceof ISealDisplayer
                || player.getOffhandItem().getItem() instanceof ISealDisplayer;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityArcaneresearchGolem entity, State state, float partialTicks) {
        // NEOFORGE_PORT: old render() zeroed body yaw before drawing; same here.
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        super.extractRenderState(entity, state, partialTicks);
        state.golem = entity;
        state.partialTicks = partialTicks;
        var props = entity.getProperties();
        GolemMaterial material = props != null ? props.getMaterial() : null;
        state.matTexture = material != null && material.texture != null ? material.texture : DEFAULT_TEXTURE;
        state.holding = !entity.getMainHandItem().isEmpty();
        state.flyerLike = props != null && (props.hasTrait(
                arcaneresearch.api.golems.EnumGolemTrait.WHEELED)
                || props.hasTrait(arcaneresearch.api.golems.EnumGolemTrait.FLYER));
        state.flash = entity.hurtTime > 0 || entity.deathTime > 0;
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        state.moveSpeedSq = dx * dx + dz * dz;
        state.turnDelta = entity.getYRot() - entity.yRotO;
        state.swing = entity.getArmSwingPhase(partialTicks);
        ItemStack mainHand = entity.getMainHandItem();
        this.itemModelResolver.updateForNonLiving(state.mainHandState,
                mainHand, ItemDisplayContext.HEAD, entity);
        state.mainHandIsBlock = mainHand.getItem() instanceof net.minecraft.world.item.BlockItem;
        ItemStack carried = entity.getCarrying().size() > 1 ? entity.getCarrying().get(1) : ItemStack.EMPTY;
        if (carried == null) {
            carried = ItemStack.EMPTY;
        }
        this.itemModelResolver.updateForNonLiving(state.carriedState, carried, ItemDisplayContext.HEAD, entity);
        state.carriedIsBlock = carried.getItem() instanceof net.minecraft.world.item.BlockItem;

        Player player = Minecraft.getInstance().player;
        boolean visible = !entity.isInvisible();
        boolean translucent = !visible && player != null && !entity.isInvisibleTo(player);
        state.ghostOn = translucent;
        state.sealXray = player != null && player.isShiftKeyDown() && holdsSealDisplayer(player)
                && !EntityUtils.canEntityBeSeen(player, entity.getX(),
                        entity.getY() + entity.getBbHeight() / 2.0, entity.getZ());
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return state.matTexture != null ? state.matTexture : DEFAULT_TEXTURE;
    }
}