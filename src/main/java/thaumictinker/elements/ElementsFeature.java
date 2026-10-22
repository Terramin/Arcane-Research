package thaumictinker.elements;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import arcaneresearch.api.aspects.Aspect;
import thaumictinker.registry.TtRegistry;

 




 
public final class ElementsFeature {

    public static final String POTIONS_RESEARCH = "INFUSED_POTIONS";

    public static final ImbuedEffect FIRE_IMBUED = TtRegistry.effect("fire_imbued", new ImbuedEffect(0xE04A17));
    public static final ImbuedEffect WATER_IMBUED = TtRegistry.effect("water_imbued", new ImbuedEffect(0x39A9C7));
    public static final ImbuedEffect EARTH_IMBUED = TtRegistry.effect("earth_imbued", new ImbuedEffect(0x4F7C2A));
    public static final ImbuedEffect AIR_IMBUED = TtRegistry.effect("air_imbued", new ImbuedEffect(0xE8E7DE));

    public static final OrderFireTransformation ORDER_TRANSFORMATION = new OrderFireTransformation();

    public static final ImbuedFireBlock AER_FIRE = fire("aer_imbued_fire", 200, new MapFireTransformation(FireMaps::air));
    public static final ImbuedFireBlock PERDITIO_FIRE = fire("perditio_imbued_fire", 1, new MapFireTransformation(FireMaps::chaos));
    public static final ImbuedFireBlock TERRA_FIRE = fire("terra_imbued_fire", 200, new MapFireTransformation(FireMaps::earth));
    public static final ImbuedFireBlock IGNIS_FIRE = fire("ignis_imbued_fire", 200, new MapFireTransformation(FireMaps::fire));
    public static final ImbuedFireBlock ORDO_FIRE = fire("ordo_imbued_fire", 200, ORDER_TRANSFORMATION);
    public static final ImbuedFireBlock AQUA_FIRE = fire("aqua_imbued_fire", 200, new MapFireTransformation(FireMaps::water));

    public static final ForcefieldBlock FORCEFIELD = TtRegistry.hiddenBlock("forcefield",
            new ForcefieldBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .noOcclusion()
                    .replaceable()
                    .instabreak()
                    .noLootTable()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    public static final net.minecraft.world.item.Item AER_FIRE_ITEM = fireItem("aer_imbued_fire", AER_FIRE);
    public static final net.minecraft.world.item.Item PERDITIO_FIRE_ITEM = fireItem("perditio_imbued_fire", PERDITIO_FIRE);
    public static final net.minecraft.world.item.Item TERRA_FIRE_ITEM = fireItem("terra_imbued_fire", TERRA_FIRE);
    public static final net.minecraft.world.item.Item IGNIS_FIRE_ITEM = fireItem("ignis_imbued_fire", IGNIS_FIRE);
    public static final net.minecraft.world.item.Item ORDO_FIRE_ITEM = fireItem("ordo_imbued_fire", ORDO_FIRE);
    public static final net.minecraft.world.item.Item AQUA_FIRE_ITEM = fireItem("aqua_imbued_fire", AQUA_FIRE);

    public static final net.minecraft.world.item.Item AER_POTION = potion("aer_infused_potion", Aspect.AIR, AIR_IMBUED);
    public static final net.minecraft.world.item.Item IGNIS_POTION = potion("ignis_infused_potion", Aspect.FIRE, FIRE_IMBUED);
    public static final net.minecraft.world.item.Item TERRA_POTION = potion("terra_infused_potion", Aspect.EARTH, EARTH_IMBUED);
    public static final net.minecraft.world.item.Item AQUA_POTION = potion("aqua_infused_potion", Aspect.WATER, WATER_IMBUED);

    private ElementsFeature() {
    }

    private static ImbuedFireBlock fire(String name, int tickDelay, FireTransformation transformation) {
        return TtRegistry.hiddenBlock(name, new ImbuedFireBlock(fireProperties(), tickDelay, transformation));
    }

    private static BlockBehaviour.Properties fireProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .replaceable()
                .noCollission()
                .instabreak()
                .lightLevel(state -> 15)
                .sound(SoundType.WOOL)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable();
    }

    private static net.minecraft.world.item.Item fireItem(String name, ImbuedFireBlock block) {
        return TtRegistry.itemWithTab(name, () -> new ImbuedFireItem(block, new net.minecraft.world.item.Item.Properties()));
    }

    private static net.minecraft.world.item.Item potion(String name, Aspect aspect, ImbuedEffect effect) {
        return TtRegistry.itemWithTab(name, () -> new InfusedPotionItem(aspect, effect,
                new net.minecraft.world.item.Item.Properties().stacksTo(4)));
    }

      
    public static void load() {
    }
}
