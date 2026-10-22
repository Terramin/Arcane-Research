package arcaneresearch.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import arcaneresearch.Arcaneresearch;











public class ArcaneresearchToolMaterials {

    private static TagKey<Item> repairTag(String path) {
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, path));
    }

    public static final ToolMaterial TOOLMAT_CRIMSON_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            200, 8.0F, 3.5F, 20,
            repairTag("repairs_void"));

    public static final ToolMaterial TOOLMAT_PRIMAL_VOID = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            500, 8.0F, 4.0F, 20,
            repairTag("repairs_void"));
}
