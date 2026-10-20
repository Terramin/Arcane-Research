package arcaneresearch.client.gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.client.renderers.tile.AuraNodeRenderHelper;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.lib.crafting.Matrix;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import javax.annotation.Nullable;
@OnlyIn(Dist.CLIENT)
public class BlueprintBlockAccess implements BlockAndTintGetter {
    private final BlockState[][][] structure;
    /**
     * true — клетки аура-ноды показываются стеклом-заглушкой (цвет по типу
     * ноды); false — нодовый блок билет AIR, сама нода рисуется светящимся
     * оверлеем {@link #renderNodeOverlay} (чертёж/книга/JEI).
     */
    private final boolean glassForNodes;
    public int sliceLine = 0;
    public BlueprintBlockAccess(Part[][][] data) {
        this(data, false, false);
    }
    /**
     * @param target target-режим (закладка): слои повёрнуты, нода — стеклом-заглушкой
     */
    public BlueprintBlockAccess(Part[][][] data, boolean target) {
        this(data, target, target);
    }
    public BlueprintBlockAccess(Part[][][] data, boolean target, boolean glassNodes) {
        this.glassForNodes = glassNodes;
        int ySize = data.length;
        int xSize = data[0].length;
        int zSize = data[0][0].length;
        structure = new BlockState[ySize][xSize][zSize];
        for (int y = 0; y < ySize; ++y) {
            Part[][] layer = data[y];
            if (target) {
                Matrix matrix = new Matrix(data[y]);
                matrix.Rotate90DegRight(3);
                layer = matrix.getMatrix();
            }
            for (int x = 0; x < layer.length; ++x) {
                for (int z = 0; z < layer[x].length; ++z) {
                    structure[ySize - y - 1][x][z] =
                            target ? convertTarget(layer[x][z]) : convert(layer[x][z]);
                }
            }
        }
    }
    private BlockState convert(@Nullable Part part) {
        if (part == null || part.getSource() == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Object source = part.getSource();
        if (source instanceof Block block) {
            if (isAuraNode(block)) {
                return nodeSurrogate(part);
            }
            return block.defaultBlockState();
        }
        if (source instanceof ItemStack stack) {
            Block b = Block.byItem(stack.getItem());
            if (b != null && b != Blocks.AIR) {
                if (isAuraNode(b)) {
                    return nodeSurrogate(part);
                }
                return b.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (source instanceof BlockState state) {
            if (isAuraNode(state.getBlock())) {
                return nodeSurrogate(part);
            }
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }
    private static boolean isAuraNode(Block block) {
        try {
            return block == AuraBlocks.AURA_NODE.get();
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Раньше здесь была тонированная стеклянная заглушка по типу ноды; теперь
     * в основном чертеже (glassForNodes == false) клетка ноды пуста — ноду
     * рисует {@link #renderNodeOverlay} поверх прохода блоков. Стекло остаётся
     * только в target-режиме закладки, где оверлей не вызывается.
     */
    private BlockState nodeSurrogate(@Nullable Part part) {
        if (!glassForNodes) {
            return Blocks.AIR.defaultBlockState();
        }
        NodeType type = part == null ? null : part.getNodeType();
        if (type == null) {
            return Blocks.GLASS.defaultBlockState();
        }
        return switch (type) {
            case DARK -> Blocks.BLACK_STAINED_GLASS.defaultBlockState();
            case TAINTED -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            case HUNGRY -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            case PURE -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            case UNSTABLE -> Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            default -> Blocks.GLASS.defaultBlockState();
        };
    }
    private BlockState convertTarget(@Nullable Part part) {
        if (part == null) {
            return Blocks.AIR.defaultBlockState();
        }
        if (part.getTarget() == null) {
            return convert(part);
        }
        Object target = part.getTarget();
        if (target instanceof ItemStack stack) {
            Block b = Block.byItem(stack.getItem());
            if (b != null && b != Blocks.AIR) {
                return b.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        }
        if (target instanceof Block block) {
            return block.defaultBlockState();
        }
        if (target instanceof BlockState state) {
            return state;
        }
        return Blocks.AIR.defaultBlockState();
    }
    private static AspectList blueprintNodeAspects = null;
    /**
     * Рисует клетку 3D-чертежа так же, как блок выглядит в мире: обычные
     * блоки — через renderSingleBlock, а блоки, чья геометрия целиком в
     * BlockEntityRenderer (Eldritch Altar и прочие EntityBlock с рендером
     * формы ENTITYBLOCK_ANIMATED) — напрямую через их BER, геометрия
     * совпадает с мировой. Поза должна быть уже сдвинута в клетку (угол
     * 0,0,0 — как у BER в мире).
     */
    public static void renderBlueprintState(PoseStack pose, MultiBufferSource buffer,
                                            @Nullable BlockState state, int light) {
        if (state == null || state.isAir()) {
            return;
        }
        if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED
                && state.getBlock() instanceof EntityBlock entityBlock) {
            BlockEntity be = null;
            try {
                be = entityBlock.newBlockEntity(BlockPos.ZERO, state);
            } catch (Exception ignored) {
            }
            BlockEntityRenderer<BlockEntity> ber = be != null
                    ? Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be)
                    : null;
            if (ber != null) {
                try {
                    ber.render(be, 0.0F, pose, buffer, light, OverlayTexture.NO_OVERLAY);
                } catch (Exception ignored) {
                }
                return;
            }
        }
        try {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pose, buffer,
                    light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
        } catch (Exception ignored) {
        }
    }
    /**
     * Настоящая аура-нода в 3D-чертеже (книга, JEI): нодовый блок невидим
     * (convert() кладёт AIR без стекла-заглушки), здесь рисуем светящуюся
     * ноду нужного типа — зловещая даёт тёмный вихрь.
     * Поза уже сдвинута в клетку чертежа, её центр — (0.5, 0.5, 0.5).
     */
    public static void renderNodeOverlay(com.mojang.blaze3d.vertex.PoseStack pose,
                                         net.minecraft.client.renderer.MultiBufferSource buffer,
                                         @Nullable Part part) {
        if (part == null) {
            return;
        }
        Object source = part.getSource();
        Block block = null;
        if (source instanceof Block b) {
            block = b;
        } else if (source instanceof ItemStack stack) {
            Block byItem = Block.byItem(stack.getItem());
            if (byItem != null && byItem != Blocks.AIR) {
                block = byItem;
            }
        }
        if (block == null || !isAuraNode(block)) {
            return;
        }
        try {
            if (blueprintNodeAspects == null) {
                blueprintNodeAspects = new AspectList();
                for (Aspect aspect : Aspect.getPrimalAspects()) {
                    if (aspect != null) {
                        blueprintNodeAspects.add(aspect, 25);
                    }
                }
            }
            NodeType type = part.getNodeType();
            if (type == null) {
                type = NodeType.NORMAL;
            }
            AuraNodeRenderHelper.renderNode(pose, buffer, blueprintNodeAspects, type, null,
                    BlockPos.ZERO, null, new Vector3f(0.5F, 0.5F, 0.5F), 0.9F,
                    new AuraNodeRenderHelper.Visibility(true, true, 64.0D), 0.0F);
        } catch (Exception ignored) {
        }
    }
    public int getHeight() {
        return structure.length;
    }
    /**
     * Скрыта ли клетка на уровне y анимацией слоёв (sliceLine) — та же
     * логика, что в {@link #getBlockState(BlockPos)}: скрытые слои
     * возвращают AIR, и оверлей ноды для них рисовать нельзя, чтобы
     * свечение не оставалось висеть при показе среза.
     */
    public boolean isHiddenBySlice(int y) {
        int hidden = sliceLine;
        if (hidden > structure.length) {
            hidden = 0;
        }
        return y >= structure.length - hidden;
    }
    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }
    @Override
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int hidden = sliceLine;
        if (hidden > structure.length) {
            hidden = 0;
        }
        if (y >= 0 && y < structure.length - hidden
                && x >= 0 && x < structure[y].length
                && z >= 0 && z < structure[y][x].length) {
            return structure[y][x][z];
        }
        return Blocks.AIR.defaultBlockState();
    }
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }
    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return 0xFFFFFF;
    }
    @Nullable
    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }
    @Override
    public int getMinBuildHeight() {
        return 0;
    }
    @Override
    public float getShade(Direction direction, boolean shade) {
        return 1.0f;
    }
}
