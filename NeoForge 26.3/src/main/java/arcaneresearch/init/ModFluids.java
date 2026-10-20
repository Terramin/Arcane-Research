package arcaneresearch.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModFluids.java (same order, same ids, same params).
// REQUIRED changes only:
//   DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES / ForgeRegistries.FLUIDS)
//     -> create(NeoForgeRegistries.FLUID_TYPES / Registries.FLUID)
//   RegistryObject -> DeferredHolder / DeferredItem (buckets live in ModItems.ITEMS)
//   ForgeFlowingFluid -> BaseFlowingFluid (identical Properties shape)
//   ResourceLocation -> Identifier (texture ids preserved verbatim)
//   initializeClient(IClientFluidTypeExtensions with getStill/getFlowing/getTintColor) REMOVED in 26.3:
//     still/flow textures + tints are preserved below as constants and wired in
//     client/ClientFluidModels (RegisterFluidModelsEvent with FluidModel.Unbaked +
//     BlockTintSources.constant) — same textures, same tint values, same fluids.
// Gameplay params (density/viscosity/rarity/lightLevel/sounds/slope/level/tickRate,
// blocks/buckets) preserved verbatim.
public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Arcaneresearch.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Arcaneresearch.MODID);

    // NEOFORGE_PORT: client visuals preserved (were initializeClient overrides in Forge).
    // Same textures + same tint values, wired in client/ClientFluidModels.
    public static final Identifier LIQUID_DEATH_STILL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "block/animatedglow");
    public static final int LIQUID_DEATH_TINT = -263978855;

    public static final Identifier PURIFYING_STILL =
            Identifier.withDefaultNamespace("block/water_still");
    public static final Identifier PURIFYING_FLOWING =
            Identifier.withDefaultNamespace("block/water_flow");
    public static final int PURIFYING_TINT = 2013252778;

    public static final Identifier FLUX_GAS_STILL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "block/fluxgas");
    public static final int FLUX_GAS_TINT = -1;

    public static final DeferredHolder<FluidType, FluidType> LIQUID_DEATH_TYPE = FLUID_TYPES.register("liquid_death",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1500)
                    .rarity(Rarity.RARE)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_DEATH = FLUIDS.register("liquid_death",
            () -> new BaseFlowingFluid.Source(ModFluids.LIQUID_DEATH_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_DEATH_FLOWING = FLUIDS.register("liquid_death_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.LIQUID_DEATH_PROPERTIES));

    public static final DeferredHolder<Item, Item> LIQUID_DEATH_BUCKET = ModItems.ITEMS.registerItem("bucket_death",
            props -> new BucketItem(ModFluids.LIQUID_DEATH.get(),
                    props.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties LIQUID_DEATH_PROPERTIES =
            new BaseFlowingFluid.Properties(ModFluids.LIQUID_DEATH_TYPE,
                    ModFluids.LIQUID_DEATH, ModFluids.LIQUID_DEATH_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.LIQUID_DEATH.get())
                    .bucket(() -> (BucketItem) ModFluids.LIQUID_DEATH_BUCKET.get())
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(10);

    public static final DeferredHolder<FluidType, FluidType> PURIFYING_FLUID_TYPE = FLUID_TYPES.register("purifying_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1000)
                    .rarity(Rarity.RARE)
                    .lightLevel(5)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    public static final DeferredHolder<Fluid, FlowingFluid> PURIFYING_FLUID = FLUIDS.register("purifying_fluid",
            () -> new BaseFlowingFluid.Source(ModFluids.PURIFYING_FLUID_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> PURIFYING_FLUID_FLOWING = FLUIDS.register("purifying_fluid_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.PURIFYING_FLUID_PROPERTIES));

    public static final DeferredHolder<Item, Item> PURIFYING_FLUID_BUCKET = ModItems.ITEMS.registerItem("bucket_pure",
            props -> new BucketItem(ModFluids.PURIFYING_FLUID.get(),
                    props.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties PURIFYING_FLUID_PROPERTIES =
            new BaseFlowingFluid.Properties(ModFluids.PURIFYING_FLUID_TYPE,
                    ModFluids.PURIFYING_FLUID, ModFluids.PURIFYING_FLUID_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.PURIFYING_FLUID.get())
                    .bucket(() -> (BucketItem) ModFluids.PURIFYING_FLUID_BUCKET.get())
                    .slopeFindDistance(4)
                    .levelDecreasePerBlock(1)
                    .tickRate(10);

    public static final DeferredHolder<FluidType, FluidType> FLUX_GAS_TYPE = FLUID_TYPES.register("flux_gas",
            () -> new FluidType(FluidType.Properties.create()
                    .density(-1000)
                    .viscosity(500)
                    .rarity(Rarity.UNCOMMON)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    public static final DeferredHolder<Fluid, FlowingFluid> FLUX_GAS = FLUIDS.register("flux_gas",
            () -> new BaseFlowingFluid.Source(ModFluids.FLUX_GAS_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> FLUX_GAS_FLOWING = FLUIDS.register("flux_gas_flowing",
            () -> new BaseFlowingFluid.Flowing(ModFluids.FLUX_GAS_PROPERTIES));

    public static final BaseFlowingFluid.Properties FLUX_GAS_PROPERTIES =
            new BaseFlowingFluid.Properties(ModFluids.FLUX_GAS_TYPE,
                    ModFluids.FLUX_GAS, ModFluids.FLUX_GAS_FLOWING)
                    .block(() -> (LiquidBlock) ModBlocks.FLUX_GAS.get())
                    .slopeFindDistance(2)
                    .levelDecreasePerBlock(2)
                    .tickRate(20);

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }
}
