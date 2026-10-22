package arcaneresearch.client.models.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.renderers.block.CrystalModel;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;

import java.util.List;






public class CrystalBakedModel implements DynamicBlockStateModel {

    private final MeshModel mesh;
    private final Material.Baked particleMaterial;

    public CrystalBakedModel(MeshModel mesh, Material.Baked particleMaterial) {
        this.mesh = mesh;
        this.particleMaterial = particleMaterial;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                             RandomSource random, List<BlockStateModelPart> parts) {
        if (!(state.getBlock() instanceof BlockCrystalTC crystal)) {
            return;
        }
        boolean[] flags = CrystalModel.computeFlags(level, pos, state);
        long seed = random.nextLong();
        List<BakedQuad> quads = CrystalModel.bakeFaceQuads(mesh, flags,
                crystal.getGrowth(state), seed, particleMaterial);
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        parts.add(new SimpleModelWrapper(builder.build(), true, particleMaterial));
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return 0;
    }

    public static final class Unbaked implements CustomUnbakedBlockStateModel {
        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

        private Unbaked() {
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            MeshModel mesh = CrystalModel.loadMesh();
            Material material = new Material(CrystalModel.TEXTURE_LOCATION);
            Material.Baked baked = baker.materials().get(material,
                    () -> "arcaneresearch:crystal");
            return new CrystalBakedModel(mesh, baked);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }
    }
}