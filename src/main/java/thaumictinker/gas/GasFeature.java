package thaumictinker.gas;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import thaumictinker.registry.TtRegistry;

 


 
public final class GasFeature {

    public static final String GASEOUS_LIGHT_RESEARCH = "GASEOUS_LIGHT";
    public static final String GASEOUS_SHADOW_RESEARCH = "GASEOUS_SHADOW";
    public static final String GAS_REMOVER_RESEARCH = "GAS_REMOVER";
    public static final String BRIGHT_NITOR_RESEARCH = "BRIGHT_NITOR";

    private static final List<Predicate<Player>> LIGHT_CARRIERS = new ArrayList<>();

    public static final GaseousLightBlock GASEOUS_LIGHT = TtRegistry.hiddenBlock("gaseous_light",
            new GaseousLightBlock(GasBlock.gas(BlockBehaviour.Properties.of())
                    .lightLevel(state -> GaseousLightBlock.LIGHT)));
    public static final GaseousShadowBlock GASEOUS_SHADOW = TtRegistry.hiddenBlock("gaseous_shadow",
            new GaseousShadowBlock(GasBlock.gas(BlockBehaviour.Properties.of())));
    public static final NitorGasBlock NITOR_GAS = TtRegistry.hiddenBlock("nitor_gas",
            new NitorGasBlock(GasBlock.gas(BlockBehaviour.Properties.of())
                    .lightLevel(NitorGasBlock::lightLevel)));

    public static final Item GASEOUS_ILLUMINAE = TtRegistry.itemWithTab("gaseous_illuminae",
            () -> new GasBottleItem(() -> GASEOUS_LIGHT, new Item.Properties()));
    public static final Item GASEOUS_TENEBRAE = TtRegistry.itemWithTab("gaseous_tenebrae",
            () -> new GasBottleItem(() -> GASEOUS_SHADOW, new Item.Properties()));
    public static final Item FUME_DISSIPATOR = TtRegistry.itemWithTab("fume_dissipator",
            () -> new FumeDissipatorItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final Item BRIGHT_NITOR = TtRegistry.itemWithTab("bright_nitor",
            () -> new BrightNitorItem(new Item.Properties().stacksTo(1)));

    private GasFeature() {
    }

    public static void load() {
    }

      
    public static void registerLightCarrier(Predicate<Player> carrier) {
        LIGHT_CARRIERS.add(carrier);
    }

    public static boolean carriesLight(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(BRIGHT_NITOR)) {
                return true;
            }
        }
        for (Predicate<Player> carrier : LIGHT_CARRIERS) {
            if (carrier.test(player)) {
                return true;
            }
        }
        return false;
    }
}
