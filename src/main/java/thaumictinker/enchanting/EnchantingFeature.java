package thaumictinker.enchanting;

import thaumictinker.net.TtNet;

 





 
public final class EnchantingFeature {

    public static final String ENCHANT_ASCENT_BOOST = "TTENCH_ASCENT_BOOST";
    public static final String ENCHANT_SLOW_FALL = "TTENCH_SLOW_FALL";
    public static final String ENCHANT_AUTO_SMELT = "TTENCH_AUTO_SMELT";
    public static final String ENCHANT_DESINTEGRATE = "TTENCH_DESINTEGRATE";
    public static final String ENCHANT_QUICK_DRAW = "TTENCH_QUICK_DRAW";
    public static final String ENCHANT_VAMPIRISM = "TTENCH_VAMPIRISM";
    public static final String ENCHANT_DISPERSED_STRIKE = "TTENCH_DISPERSED";
    public static final String ENCHANT_FINAL_STRIKE = "TTENCH_FINAL";
    public static final String ENCHANT_FOCUSED_STRIKE = "TTENCH_FOCUSED";
    public static final String ENCHANT_POUNCE = "TTENCH_POUNCE";
    public static final String ENCHANT_TUNNEL = "TTENCH_TUNNEL";
    public static final String ENCHANT_SHOCKWAVE = "TTENCH_SHOCKWAVE";
    public static final String ENCHANT_SHATTER = "TTENCH_SHATTER";
    public static final String ENCHANT_VALIANCE = "TTENCH_VALIANCE";

    public static final net.minecraft.world.item.Item SPELL_CLOTH =
            thaumictinker.registry.TtRegistry.itemWithTab("spellbinding_cloth",
                    () -> new SpellClothItem(new net.minecraft.world.item.Item.Properties()
                            .durability(SpellClothItem.USES).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

      
    public static final net.minecraft.world.level.block.Block ENCHANTER =
            thaumictinker.registry.TtRegistry.blockWithItem("osmotic_enchanter", new EnchanterBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties
                            .of()
                            .mapColor(net.minecraft.world.level.material.MapColor.STONE)
                            .strength(5.0F, 2000.0F)
                            .requiresCorrectToolForDrops()
                            .sound(net.minecraft.world.level.block.SoundType.STONE)
                            .noOcclusion()));

      
    public static final net.minecraft.world.level.block.Block OBSIDIAN_TOTEM =
            thaumictinker.registry.TtRegistry.blockWithItem("obsidian_totem", new ObsidianTotemBlock(
                    net.minecraft.world.level.block.state.BlockBehaviour.Properties
                            .of()
                            .mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK)
                            .strength(25.0F, 1200.0F)
                            .requiresCorrectToolForDrops()
                            .sound(net.minecraft.world.level.block.SoundType.STONE)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<EnchanterBlockEntity> ENCHANTER_ENTITY =
            thaumictinker.registry.TtRegistry.blockEntity("osmotic_enchanter",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(EnchanterBlockEntity::new, ENCHANTER)
                            .build(null));

    public static final net.minecraft.world.inventory.MenuType<EnchanterMenu> ENCHANTER_MENU =
            thaumictinker.registry.TtRegistry.menu("osmotic_enchanter",
                    net.minecraftforge.common.extensions.IForgeMenuType.create(EnchanterMenu::new));

    public static final net.minecraft.world.item.crafting.RecipeSerializer<SpellClothRecipe> SPELL_CLOTH_SERIALIZER =
            thaumictinker.registry.TtRegistry.recipeSerializer("spell_cloth",
                    new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(SpellClothRecipe::new));

    private EnchantingFeature() {
    }

    public static void load() {
        TtNet.register();
    }
}
