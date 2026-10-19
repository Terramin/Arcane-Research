package arcaneresearch.client.lib.events;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class WandTipHelper {
    /**
     * Боковой сдвиг якоря наружу кристалла в единицах модели (model -Z):
     * зеркало левой руки разворачивает его автоматически (правая — вправо,
     * левая — влево). После display-масштаба ≈8 см.
     */
    public static final float TIP_DZ_SIDE = -0.12F;
    /** То же для луча экскавации (тип 2) — чуть шире. */
    public static final float TIP_DZ_BEAM = -0.16F;

    private WandTipHelper() {
    }

    public static boolean isRightHand(Player player) {
        try {
            boolean mainHolds =
                    player.getMainHandItem().getItem() instanceof arcaneresearch.api.wands.IWand;
            boolean offHolds =
                    player.getOffhandItem().getItem() instanceof arcaneresearch.api.wands.IWand;
            InteractionHand hand = mainHolds ? InteractionHand.MAIN_HAND
                    : (offHolds ? InteractionHand.OFF_HAND : player.getUsedItemHand());
            return (hand == InteractionHand.MAIN_HAND)
                    == (player.getMainArm() == HumanoidArm.RIGHT);
        } catch (Exception e) {
            return true;
        }
    }

    public static float charge(Player player) {
        try {
            if (!player.isUsingItem()) {
                return 0.0F;
            }
            ItemStack use = player.getUseItem();
            if (!(use.getItem() instanceof arcaneresearch.api.wands.IWand)) {
                return 0.0F;
            }
            // NEOFORGE_PORT: ItemStack.getUseDuration(LivingEntity) (was no-arg in 1.20.1).
            float f = (float) use.getUseDuration(player) - (float) player.getUseItemRemainingTicks();
            float f1 = f / 20.0F;
            f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
            return Mth.clamp(f1, 0.0F, 1.0F);
        } catch (Exception e) {
            return 0.0F;
        }
    }

    public static Vec3 tipWorld(Player player, float partialTicks) {
        return tipWorld(player, partialTicks, 0.0F, 0.0F, 0.0F);
    }

    /**
     * @param modelDx,modelDy,modelDz сдвиг точки в единицах модели
     *        (до display-трансформа). Зеркало левой руки применяется
     *        автоматически: model -Z даёт наружу в обе стороны
     *        (правая рука — вправо, левая — влево).
     */
    public static Vec3 tipWorld(Player player, float partialTicks,
                                float modelDx, float modelDy, float modelDz) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.mainCamera();
            boolean first = false;
            try {
                // NEOFORGE_PORT: Minecraft.cameraEntity field -> getCameraEntity().
                first = mc.options.getCameraType().isFirstPerson() && mc.getCameraEntity() == player;
            } catch (Exception ignored) {
            }
            if (first) {
                Vec3 tip = solveTip(player, partialTicks, modelDx, modelDy, modelDz);
                if (tip != null) {
                    return tip;
                }
            }
            Vec3 eye = player.getEyePosition(partialTicks);
            Vec3 look = player.getViewVector(partialTicks);
            return eye.add(look.scale(1.0D));
        } catch (Exception e) {
            try {
                Vec3 eye = player.getEyePosition(partialTicks);
                Vec3 look = player.getViewVector(partialTicks);
                return eye.add(look.scale(1.0D));
            } catch (Exception ignored) {
                return new Vec3(player.getX(), player.getY() + 1.0D, player.getZ());
            }
        }
    }

    /**
     * Динамический кончик: та же поза, что рендерится (база + волна живыми
     * значениями), плюс display-трансформ жезла, плюс сдвиг -0.5 дословно
     * как ItemRenderer.render (после display, перед квадами); точка фокуса
     * (0.5, 1.263, 0.5) из view-space в мир. Никаких захардкоженных якорей.
     * Без сдвига -0.5 старт уходил от кристалла на ~0.6 view-единиц.
     */
    private static Vec3 solveTip(Player player, float partialTicks,
                                   float modelDx, float modelDy, float modelDz) {
        try {
            if (!(player instanceof net.minecraft.client.player.LocalPlayer local)) {
                return null;
            }
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            boolean mainWand = main.getItem() instanceof arcaneresearch.api.wands.IWand;
            boolean offWand = off.getItem() instanceof arcaneresearch.api.wands.IWand;
            if (!mainWand && !offWand) {
                return null;
            }
            InteractionHand hand = mainWand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack handStack = mainWand ? main : off;
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm()
                    : player.getMainArm().getOpposite();
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;
            arcaneresearch.client.renderers.item.WandUseAnimation.AnimState st =
                    arcaneresearch.client.renderers.item.WandUseAnimation.resolveState(
                            local, handStack, hand, partialTicks);
            com.mojang.blaze3d.vertex.PoseStack ps = new com.mojang.blaze3d.vertex.PoseStack();
            // View-bob дословно как renderHandsWithItems (иначе прицел
            // и рука разъезжаются при движении мыши/ходьбе).
            float bx = net.minecraft.util.Mth.lerp(partialTicks, local.xBobO, local.xBob);
            float by = net.minecraft.util.Mth.lerp(partialTicks, local.yBobO, local.yBob);
            ps.rotate(com.mojang.math.Axis.XP.rotationDegrees(
                    (local.getViewXRot(partialTicks) - bx) * 0.1F));
            ps.rotate(com.mojang.math.Axis.YP.rotationDegrees(
                    (local.getViewYRot(partialTicks) - by) * 0.1F));
            // Живые equip/swing: equip медленный — снапшот хука (макс. лаг кадр),
            // swing быстрый — СТРОГО live-аналитика! Частицы (луч) рисуются
            // РАНЬШЕ рук в кадре, снапшот свинга отставал бы на кадр и луч
            // вечно догонял бы машущую руку.
            float equip = hand == InteractionHand.MAIN_HAND
                    ? arcaneresearch.client.renderers.item.WandUseAnimation.lastEquipMain
                    : arcaneresearch.client.renderers.item.WandUseAnimation.lastEquipOff;
            // NEOFORGE_PORT: LivingEntity.attackAnim/swingingArm fields removed in 26.x
            // (private SwingState); live swing via isSwinging()+getCurrentSwing().hand()
            // + getSwingAnimation(partial) — тот же взмах руки.
            InteractionHand swinging = InteractionHand.MAIN_HAND;
            try {
                if (player.isSwinging() && player.getCurrentSwing() != null) {
                    swinging = player.getCurrentSwing().hand();
                }
            } catch (Exception ignored) {
            }
            float sw = 0.0F;
            try {
                sw = swinging == hand ? player.getSwingAnimation(partialTicks) : 0.0F;
            } catch (Exception ignored) {
            }
            arcaneresearch.client.renderers.item.WandUseAnimation.applyBasePose(ps, arm, i,
                    st != null && st.using(), local, local.getUseItem(), equip, sw, partialTicks);
            if (st != null) {
                arcaneresearch.client.renderers.item.WandUseAnimation.applyWaveBlock(
                        ps, st.focusStack(), st.u(), st.tilt());
            }
            // Снимок матрицы до display — для сверки с живой матрицей рендера.
            org.joml.Matrix4f preM = new org.joml.Matrix4f(ps.last().pose());
            float[] preT = new float[]{preM.m30(), preM.m31(), preM.m32()};
            arcaneresearch.api.wands.IWand wand =
                    (arcaneresearch.api.wands.IWand) handStack.getItem();
            // NEOFORGE_PORT: display-трансформ руки = WandTransforms firstperson
            // (Forge solveTip брал его из запечённой модели: getModelWithOverrides
            // -> getTransforms().getTransform(FIRST_PERSON_*_HAND).apply(...);
            // BakedModel убран в 26.x, но bakeWandModels инициализировал мешь
            // этими же константами WAND/STAFF, а firstperson-слоты у них
            // совпадают — числа дословно: rot [0,-90,25],
            // trans [1.13,3.2,1.13]x0.0625, scale 0.68).
            // Порядок T-R-S и зеркало левой руки (x-трансляция и y/z-повороты
            // в минус) — дословно 1.20.1 ItemTransform.apply (сверено с обоими
            // Mojang-исходниками: 1.20.1 translate->rotate->scale, 26.3 так же
            // + folded translate(-0.5)). Зеркало: translation.x, rotation.y/z
            // в минус — как apply(true) в обеих версиях.
            // Сдвиг -0.5 дословно как ItemRenderer.render: handleCameraTransforms
            // (display выше), затем translate(-0.5, -0.5, -0.5), затем квады.
            // Без него точка (0.5, y, 0.5) считается в пространстве ДО сдвига,
            // а кристалл рисуется ПОСЛЕ — старт отрывало от кристалла.
            // Боб (bobHurt/bobView) levels GameRenderer здесь НЕ повторяем:
            // он сидит и в проекции мира, и в стеке руки — для мировой точки
            // нужна поза без боба, иначе боб задвоится.
            boolean right = arm == HumanoidArm.RIGHT;
            float dtx = (right ? 1.13F : -1.13F) * 0.0625F;
            float dty = 3.2F * 0.0625F;
            float dtz = 1.13F * 0.0625F;
            float dry = right ? -90.0F : 90.0F;
            float drz = right ? 25.0F : -25.0F;
            ps.translate(dtx, dty, dtz);
            ps.rotate(com.mojang.math.Axis.YP.rotationDegrees(dry));
            ps.rotate(com.mojang.math.Axis.ZP.rotationDegrees(drz));
            ps.scale(0.68F, 0.68F, 0.68F);
            ps.translate(-0.5F, -0.5F, -0.5F);
            org.joml.Matrix4f m = new org.joml.Matrix4f(ps.last().pose());
            // Центр кристалла фокуса (фокус y 1.06–1.26 → центр 1.16);
            // без фокуса — верх крышки (y 1.125).
            boolean hasFocus = false;
            try {
                hasFocus = wand.getFocus(handStack) != null;
            } catch (Exception ignored) {
            }
            float tipY = hasFocus ? 1.16F : 1.125F;
            org.joml.Vector4f p = new org.joml.Vector4f(0.5F + modelDx, tipY + modelDy, 0.5F + modelDz, 1.0F);
            m.transform(p);
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.mainCamera();
            // NEOFORGE_PORT: БЕЗ инверсии x/z. В 1.20.1 camera.rotation()
            // маппил другую локальную рамку (там инверсия компенсировала и
            // без неё «луч уходил левее жезла»); в 26.3 rotation() маппит
            // стандартное GL view-пространство (+X=право, +Y=верх, −Z=вперёд:
            // camera.LEFT=(-1,0,0) ротируется в мировое «влево») в мир —
            // сверено с Camera.java 26.3. Инверсия здесь зеркалит точку
            // лево/право и вперёд/назад: кончик определялся не с той стороны.
            org.joml.Vector3f v = new org.joml.Vector3f(p.x, p.y, p.z);
            v.rotate(camera.rotation());
            Vec3 pos = camera.position();
            return new Vec3(pos.x + v.x, pos.y + v.y, pos.z + v.z);
        } catch (Exception ignored) {
            return null;
        }
    }
}
