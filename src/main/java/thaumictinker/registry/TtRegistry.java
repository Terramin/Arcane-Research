package thaumictinker.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import thaumictinker.TtCore;
import thaumictinker.TtFeatures;

 




 
public final class TtRegistry {

    private static final Map<ResourceLocation, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<EntityType<?>>> ENTITIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<BlockEntityType<?>>> BLOCK_ENTITIES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<net.minecraft.world.item.enchantment.Enchantment>> ENCHANTMENTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<net.minecraft.world.inventory.MenuType<?>>> MENUS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<net.minecraft.world.item.crafting.RecipeSerializer<?>>> RECIPE_SERIALIZERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<net.minecraft.world.effect.MobEffect>> EFFECTS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Supplier<Item>> ITEMS = new LinkedHashMap<>();
      
    private static final List<TabVariants> TAB_VARIANTS = new ArrayList<>();
    private static final List<Supplier<? extends ItemLike>> TAB_ITEMS = new ArrayList<>();
    private static Supplier<? extends ItemLike> tabIcon = () -> net.minecraft.world.item.Items.AIR;
    private static boolean loaded;

    private TtRegistry() {
    }

      
    public static <B extends Block> B block(String path, B block) {
        BLOCKS.put(TtCore.id(path), block);
        TAB_ITEMS.add(() -> block);
        return block;
    }

     


 
    public static <B extends Block> B hiddenBlock(String path, B block) {
        BLOCKS.put(TtCore.id(path), block);
        return block;
    }

      
    public static Item item(String path, Supplier<Item> supplier) {
        Item item = supplier.get();
        ITEMS.put(TtCore.id(path), () -> item);
        return item;
    }

      
    public static <B extends Block> B blockWithItem(String path, B block) {
        BLOCKS.put(TtCore.id(path), block);
        itemWithTab(path, () -> new BlockItem(block, new Item.Properties()));
        return block;
    }

     



 
    public static Item itemWithTab(String path, Supplier<Item> supplier) {
        Item item = supplier.get();
        ITEMS.put(TtCore.id(path), () -> item);
        TAB_ITEMS.add(() -> item);
        return item;
    }

     


 
    public static Item itemWithVariants(String path, Item item, TabVariants variants) {
        ITEMS.put(TtCore.id(path), () -> item);
        TAB_VARIANTS.add(variants);
        return item;
    }

      
    public interface TabVariants {
        void addVariants(net.minecraft.world.item.CreativeModeTab.Output output);
    }

     


 
    public static <T extends BlockEntityType<?>> T blockEntity(String path, T type) {
        BLOCK_ENTITIES.put(TtCore.id(path), () -> type);
        return type;
    }

      
    public static <E extends net.minecraft.world.item.enchantment.Enchantment> E enchantment(String path, E enchantment) {
        ENCHANTMENTS.put(TtCore.id(path), () -> enchantment);
        return enchantment;
    }

      
    public static <T extends EntityType<?>> T entity(String path, T type) {
        ENTITIES.put(TtCore.id(path), () -> type);
        return type;
    }

      
    public static <T extends net.minecraft.world.inventory.MenuType<?>> T menu(String path, T type) {
        MENUS.put(TtCore.id(path), () -> type);
        return type;
    }

      
    public static <T extends net.minecraft.world.item.crafting.RecipeSerializer<?>> T recipeSerializer(String path, T serializer) {
        RECIPE_SERIALIZERS.put(TtCore.id(path), () -> serializer);
        return serializer;
    }

      
    public static <T extends net.minecraft.world.effect.MobEffect> T effect(String path, T effect) {
        EFFECTS.put(TtCore.id(path), () -> effect);
        return effect;
    }

      
    public static void tabIcon(Supplier<? extends ItemLike> icon) {
        tabIcon = icon;
    }

     



 
    @Mod.EventBusSubscriber(modid = TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationEvents {

        @SubscribeEvent
        public static void onRegister(RegisterEvent event) {
            if (!loaded && event.getRegistryKey() == ForgeRegistries.BLOCKS.getRegistryKey()) {
                loaded = true;
                TtFeatures.load();
            }
            ResourceKeyDispatch.of(event);
        }
    }

      
    private static final class ResourceKeyDispatch {
        static void of(RegisterEvent event) {
            if (event.getRegistryKey() == ForgeRegistries.BLOCKS.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Block> entry : BLOCKS.entrySet()) {
                    event.register(ForgeRegistries.BLOCKS.getRegistryKey(), entry.getKey(), () -> entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.ENTITY_TYPES.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<EntityType<?>>> entry : ENTITIES.entrySet()) {
                    event.register(ForgeRegistries.ENTITY_TYPES.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == Registries.BLOCK_ENTITY_TYPE) {
                for (Map.Entry<ResourceLocation, Supplier<BlockEntityType<?>>> entry : BLOCK_ENTITIES.entrySet()) {
                    event.register(Registries.BLOCK_ENTITY_TYPE, entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.ENCHANTMENTS.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<net.minecraft.world.item.enchantment.Enchantment>> entry : ENCHANTMENTS.entrySet()) {
                    event.register(ForgeRegistries.ENCHANTMENTS.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.MENU_TYPES.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<net.minecraft.world.inventory.MenuType<?>>> entry : MENUS.entrySet()) {
                    event.register(ForgeRegistries.MENU_TYPES.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.RECIPE_SERIALIZERS.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<net.minecraft.world.item.crafting.RecipeSerializer<?>>> entry : RECIPE_SERIALIZERS.entrySet()) {
                    event.register(ForgeRegistries.RECIPE_SERIALIZERS.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.MOB_EFFECTS.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<net.minecraft.world.effect.MobEffect>> entry : EFFECTS.entrySet()) {
                    event.register(ForgeRegistries.MOB_EFFECTS.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == ForgeRegistries.ITEMS.getRegistryKey()) {
                for (Map.Entry<ResourceLocation, Supplier<Item>> entry : ITEMS.entrySet()) {
                    event.register(ForgeRegistries.ITEMS.getRegistryKey(), entry.getKey(), entry.getValue());
                }
            } else if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB
                    && !TAB_ITEMS.isEmpty()) {
                ResourceLocation tabId = TtCore.id("tab");
                event.register(Registries.CREATIVE_MODE_TAB, tabId, () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.thaumictinker"))
                        .icon(() -> new ItemStack(tabIcon.get()))
                        .displayItems((parameters, output) -> {
                            for (Supplier<? extends ItemLike> item : TAB_ITEMS) {
                                output.accept(item.get());
                            }
                            for (TabVariants variants : TAB_VARIANTS) {
                                variants.addVariants(output);
                            }
                        })
                        .build());
            }
        }
    }
}
