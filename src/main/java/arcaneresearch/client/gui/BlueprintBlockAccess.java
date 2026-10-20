package arcaneresearch.client.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.client.renderers.tile.AuraNodeRenderHelper;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.lib.crafting.Matrix;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class BlueprintBlockAccess implements BlockAndTintGetter {

    private final BlockState[][][] structure;

    public int sliceLine = 0;

    public BlueprintBlockAccess(Part[][][] data) {
        this(data, false);
    }

    public BlueprintBlockAccess(Part[][][] data, boolean target) {
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

    private static BlockState convert(@Nullable Part part) {
        if (part == null || part.getSource() == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Object source = part.getSource();
        if (source instanceof Block block) {
            // Блок аура-ноды невидим (INVISIBLE + пустая модель): в книге/JEI
            // показываем тонированное стекло в цвет типа ноды, иначе клетка
            // чертежа пуста. Настоящую ноду поверх рисует рендерер книги.
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

    /** Видимый куб-маркер ноды в цвет её типа (зловещая — чёрное стекло). */
    private static BlockState nodeSurrogate(@Nullable Part part) {
        NodeType type = part == null ? null : part.getNodeType();
        if (type == null) {
            return Blocks.GLASS.defaultBlockState();
        }
        return switch (type) {
            // NEOFORGE_PORT: 26.2 ColorCollection consolidation (BLACK_STAINED_GLASS -> STAINED_GLASS.black()); color mapping 1:1.
            case DARK -> Blocks.STAINED_GLASS.black().defaultBlockState();
            case TAINTED -> Blocks.STAINED_GLASS.purple().defaultBlockState();
            case HUNGRY -> Blocks.STAINED_GLASS.red().defaultBlockState();
            case PURE -> Blocks.STAINED_GLASS.white().defaultBlockState();
            case UNSTABLE -> Blocks.STAINED_GLASS.gray().defaultBlockState();
            default -> Blocks.GLASS.defaultBlockState();
        };
    }

    private static BlockState convertTarget(@Nullable Part part) {
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
     * Настоящая аура-нода поверх маркера в 3D-чертеже (книга, JEI): сам блок
     * ноды невидим, convert() кладёт тонированное стекло, а здесь рисуем
     * светящуюся ноду нужного типа — зловещая даёт тёмный вихрь.
     * Поза уже сдвинута в клетку чертежа, её центр — (0.5, 0.5, 0.5).
     */
    // NEOFORGE_PORT: MultiBufferSource -> SubmitNodeCollector (submit-system); тело 1:1.
    public static void renderNodeOverlay(com.mojang.blaze3d.vertex.PoseStack pose,
                                         net.minecraft.client.renderer.SubmitNodeCollector buffer,
                                         @Nullable Part part) {
        if (part == null || !(part.getSource() instanceof Block block) || !isAuraNode(block)) {
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

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public net.minecraft.world.level.CardinalLighting cardinalLighting() {
        return net.minecraft.world.level.CardinalLighting.DEFAULT;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}