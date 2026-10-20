package arcaneresearch.client.renderers.block;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.obj.MeshLoader;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.lib.obj.MeshPart;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// NEOFORGE_PORT: BakedModel/ItemOverrides/ModelData removed in 26.3 (BlockStateModel
// rework). This class keeps the Forge face-selection/rotation math 1:1; the baked
// side is CrystalBakedModel (DynamicBlockStateModel) wired via
// CrystalModelRegistration (RegisterBlockStateModels, type "arcaneresearch:crystal").
public class CrystalModel {

    public static final Identifier OBJ_LOCATION =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/crystal.obj");
    public static final Identifier TEXTURE_LOCATION =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "block/crystal");

    /** Flag order: UP, DOWN, EAST, WEST, NORTH, SOUTH (Forge getQuads order). */
    public static boolean[] computeFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        boolean[] flags = new boolean[6];
        if (level == null || pos == null || !(state.getBlock() instanceof BlockCrystalTC)) {
            return flags;
        }
        flags[0] = isRockOn(level, pos.above(), Direction.UP);
        flags[1] = isRockOn(level, pos.below(), Direction.DOWN);
        flags[2] = isRockOn(level, pos.east(), Direction.EAST);
        flags[3] = isRockOn(level, pos.west(), Direction.WEST);
        flags[4] = isRockOn(level, pos.north(), Direction.NORTH);
        flags[5] = isRockOn(level, pos.south(), Direction.SOUTH);
        return flags;
    }

    public static MeshModel loadMesh() {
        try {
            MeshModel sourceMesh = new MeshLoader().loadFromResource(OBJ_LOCATION);
            for (MeshPart mp : sourceMesh.parts) {
                mp.tintIndex = 0;
            }
            return sourceMesh;
        } catch (IOException e) {
            throw new RuntimeException("Could not load crystal mesh at " + OBJ_LOCATION, e);
        }
    }

    private static boolean isRockOn(BlockAndTintGetter level, BlockPos neighborPos, Direction side) {
        BlockState neighbor = level.getBlockState(neighborPos);
        return BlockCrystalTC.isValidSupport(neighbor, level, neighborPos, side.getOpposite());
    }

    /**
     * Forge getQuads() 1:1: picks growth+1 shard parts per attached face (seeded
     * shuffle), rotates the mesh copy per face, bakes quads. Empty when no face
     * (or every face) is attached, exactly as Forge.
     */
    public static List<BakedQuad> bakeFaceQuads(MeshModel sourceMesh, boolean[] flags,
                                                int growth, long baseSeed, Material.Baked baked) {
        List<BakedQuad> ret = new ArrayList<BakedQuad>();
        if (sourceMesh == null || baked == null) {
            return ret;
        }
        int m = growth + 1;
        try {
            if (!flags[0] || !flags[1] || !flags[2] || !flags[3] || !flags[4] || !flags[5]) {
                if (flags[0]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed);
                    MeshModel mod = mm.clone();
                    mod.rotate(Math.toRadians(180.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
                    ret.addAll(mod.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
                if (flags[1]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed + 5L);
                    ret.addAll(mm.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
                if (flags[2]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed + 10L);
                    MeshModel mod = mm.clone();
                    mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                    mod.rotate(Math.toRadians(270.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f));
                    ret.addAll(mod.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
                if (flags[3]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed + 15L);
                    MeshModel mod = mm.clone();
                    mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                    mod.rotate(Math.toRadians(90.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 1.0f, 1.0f));
                    ret.addAll(mod.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
                if (flags[4]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed + 20L);
                    MeshModel mod = mm.clone();
                    mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
                    ret.addAll(mod.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
                if (flags[5]) {
                    MeshModel mm = pickParts(sourceMesh, m, baseSeed + 25L);
                    MeshModel mod = mm.clone();
                    mod.rotate(Math.toRadians(90.0), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
                    mod.rotate(Math.toRadians(180.0), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f));
                    ret.addAll(mod.bakeModel(baked.sprite(), crystalInfo(baked)));
                }
            }
        } catch (Exception ex) {
        }
        return ret;
    }

    private static MeshModel pickParts(MeshModel sourceMesh, int m, long seed) {
        MeshModel mm = sourceMesh.clone();
        List<Integer> c = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        Collections.shuffle(c, new java.util.Random(seed));
        mm.parts.clear();
        for (int a = 0; a < m; ++a) {
            mm.parts.add(sourceMesh.parts.get(c.get(a)));
        }
        return mm;
    }

    private static BakedQuad.MaterialInfo crystalInfo(Material.Baked baked) {
        TextureAtlasSprite sprite = baked.sprite();
        return new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.CUTOUT,
                net.minecraft.client.renderer.Sheets.cutoutBlockItemSheet(),
                net.minecraft.client.renderer.Sheets.cutoutBlockItemGlintSheet(),
                net.minecraft.client.renderer.Sheets.cutoutBlockItemGlintSpecialSheet(),
                0, null, 0, true);
    }
}