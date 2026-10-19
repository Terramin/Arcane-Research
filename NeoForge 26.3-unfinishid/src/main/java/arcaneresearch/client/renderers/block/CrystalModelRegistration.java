package arcaneresearch.client.renderers.block;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.CrystalBakedModel;

// NEOFORGE_PORT (26.3 BlockStateModel rework, Thaumaturge
// CrystalModelRegistration pattern): registers the "arcaneresearch:crystal"
// custom model type used by the crystal_* blockstate JSONs. Replaces the Forge
// ModifyBakingResult swap (ProxyBlock.crystalModelPaths + CrystalBakedModel).
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class CrystalModelRegistration {
    public static final Identifier CRYSTAL_MODEL_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crystal");

    private CrystalModelRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(CRYSTAL_MODEL_ID, CrystalBakedModel.Unbaked.CODEC);
    }
}
