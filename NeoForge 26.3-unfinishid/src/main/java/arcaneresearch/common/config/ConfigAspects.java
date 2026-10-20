package arcaneresearch.common.config;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.registries.BuiltInRegistries; // NEOFORGE_PORT: was ForgeRegistries
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.AspectRegistryEvent;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModFluids;
import arcaneresearch.common.items.wands.WandItems;
import arcaneresearch.init.ModItems;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ConfigAspects {

    public static void init() {
        registerItemAspects();
        registerEntityAspects();
        registerArcaneresearchItems();

        AspectRegistryEvent are = new AspectRegistryEvent();
        NeoForge.EVENT_BUS.post(are);
    }

    private static void registerEntityAspects() {
        ArcaneresearchApi.registerEntityTag("minecraft:zombie", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:husk", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.FIRE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:giant", new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 15).add(Aspect.EARTH, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:skeleton", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:wither_skeleton", new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 5).add(Aspect.ENTROPY, 10));
        // NEOFORGE_PORT (26.3): Forge order - NBT variant first (scan takes the first match, so the
        // generic entry first meant a charged creeper never gave ENERGY).
        ArcaneresearchApi.registerEntityTag("minecraft:creeper", new AspectList().add(Aspect.PLANT, 15).add(Aspect.FIRE, 15).add(Aspect.ENERGY, 15), new ArcaneresearchApi.EntityTagsNBT("powered", (byte) 1));
        ArcaneresearchApi.registerEntityTag("minecraft:creeper", new AspectList().add(Aspect.PLANT, 15).add(Aspect.FIRE, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:horse", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:donkey", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:mule", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:skeleton_horse", new AspectList().add(Aspect.BEAST, 5).add(Aspect.UNDEAD, 10).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:zombie_horse", new AspectList().add(Aspect.BEAST, 10).add(Aspect.UNDEAD, 5).add(Aspect.EARTH, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:pig", new AspectList().add(Aspect.BEAST, 10).add(Aspect.EARTH, 10).add(Aspect.DESIRE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:experience_orb", new AspectList().add(Aspect.MIND, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:sheep", new AspectList().add(Aspect.BEAST, 10).add(Aspect.EARTH, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:cow", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:mooshroom", new AspectList().add(Aspect.BEAST, 15).add(Aspect.PLANT, 15).add(Aspect.EARTH, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:snow_golem", new AspectList().add(Aspect.COLD, 10).add(Aspect.MAN, 5).add(Aspect.MECHANISM, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:ocelot", new AspectList().add(Aspect.BEAST, 10).add(Aspect.ENTROPY, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:chicken", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:squid", new AspectList().add(Aspect.BEAST, 5).add(Aspect.WATER, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:wolf", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 10).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:bat", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:spider", new AspectList().add(Aspect.BEAST, 10).add(Aspect.ENTROPY, 10).add(Aspect.TRAP, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:slime", new AspectList().add(Aspect.LIFE, 10).add(Aspect.WATER, 10).add(Aspect.ALCHEMY, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:ghast", new AspectList().add(Aspect.UNDEAD, 15).add(Aspect.FIRE, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:zombified_piglin", new AspectList().add(Aspect.UNDEAD, 15).add(Aspect.FIRE, 15).add(Aspect.BEAST, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:enderman", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MOTION, 15).add(Aspect.DESIRE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:cave_spider", new AspectList().add(Aspect.BEAST, 5).add(Aspect.DEATH, 10).add(Aspect.TRAP, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:silverfish", new AspectList().add(Aspect.BEAST, 5).add(Aspect.EARTH, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:blaze", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.FIRE, 15).add(Aspect.FLIGHT, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:magma_cube", new AspectList().add(Aspect.WATER, 5).add(Aspect.FIRE, 10).add(Aspect.ALCHEMY, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:ender_dragon", new AspectList().add(Aspect.ELDRITCH, 50).add(Aspect.BEAST, 30).add(Aspect.ENTROPY, 50).add(Aspect.FLIGHT, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:wither", new AspectList().add(Aspect.UNDEAD, 50).add(Aspect.ENTROPY, 25).add(Aspect.FIRE, 25));
        ArcaneresearchApi.registerEntityTag("minecraft:witch", new AspectList().add(Aspect.MAN, 15).add(Aspect.MAGIC, 5).add(Aspect.ALCHEMY, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:villager", new AspectList().add(Aspect.MAN, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:iron_golem", new AspectList().add(Aspect.METAL, 15).add(Aspect.MAN, 5).add(Aspect.MECHANISM, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:end_crystal", new AspectList().add(Aspect.ELDRITCH, 15).add(Aspect.AURA, 15).add(Aspect.LIFE, 15));

        ArcaneresearchApi.registerEntityTag("minecraft:guardian", new AspectList().add(Aspect.BEAST, 10).add(Aspect.ELDRITCH, 10).add(Aspect.WATER, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:elder_guardian", new AspectList().add(Aspect.BEAST, 10).add(Aspect.ELDRITCH, 15).add(Aspect.WATER, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:rabbit", new AspectList().add(Aspect.BEAST, 5).add(Aspect.EARTH, 5).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:endermite", new AspectList().add(Aspect.BEAST, 5).add(Aspect.ELDRITCH, 5).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:polar_bear", new AspectList().add(Aspect.BEAST, 15).add(Aspect.COLD, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:shulker", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.TRAP, 5).add(Aspect.FLIGHT, 5).add(Aspect.PROTECT, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:evoker", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:vindicator", new AspectList().add(Aspect.AVERSION, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:illusioner", new AspectList().add(Aspect.SENSES, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:llama", new AspectList().add(Aspect.BEAST, 15).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:parrot", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:stray", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 5).add(Aspect.TRAP, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:vex", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.FLIGHT, 5).add(Aspect.MAGIC, 5).add(Aspect.MAN, 5));

        ArcaneresearchApi.registerEntityTag("arcaneresearch:flux_rift", new AspectList().add(Aspect.FLUX, 20).add(Aspect.ELDRITCH, 20).add(Aspect.AURA, 20));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:fire_bat", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.FIRE, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:spell_bat", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.MAGIC, 10).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:wand_hellbat", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.FIRE, 10).add(Aspect.MAGIC, 5));
        // NBT-варианты раньше generic: иначе первое совпадение всегда generic.
        ArcaneresearchApi.registerEntityTag("arcaneresearch:pech", new AspectList().add(Aspect.MAN, 10).add(Aspect.AURA, 5).add(Aspect.EXCHANGE, 10).add(Aspect.DESIRE, 5), new ArcaneresearchApi.EntityTagsNBT("PechType", (byte) 0));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:pech", new AspectList().add(Aspect.MAN, 10).add(Aspect.AURA, 5).add(Aspect.EXCHANGE, 10).add(Aspect.AVERSION, 5), new ArcaneresearchApi.EntityTagsNBT("PechType", (byte) 1));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:pech", new AspectList().add(Aspect.MAN, 10).add(Aspect.AURA, 5).add(Aspect.EXCHANGE, 10).add(Aspect.MAGIC, 5), new ArcaneresearchApi.EntityTagsNBT("PechType", (byte) 2));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:pech", new AspectList().add(Aspect.MAN, 10).add(Aspect.AURA, 5).add(Aspect.EXCHANGE, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:thaumic_slime", new AspectList().add(Aspect.LIFE, 5).add(Aspect.WATER, 5).add(Aspect.FLUX, 5).add(Aspect.ALCHEMY, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:brainy_zombie", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.MIND, 5).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:giant_brainy_zombie", new AspectList().add(Aspect.UNDEAD, 25).add(Aspect.MAN, 15).add(Aspect.MIND, 5).add(Aspect.AVERSION, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taintacle", new AspectList().add(Aspect.FLUX, 15).add(Aspect.BEAST, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taint_seed", new AspectList().add(Aspect.FLUX, 20).add(Aspect.AURA, 10).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taint_seed_prime", new AspectList().add(Aspect.FLUX, 25).add(Aspect.AURA, 15).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taintacle_small", new AspectList().add(Aspect.FLUX, 5).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taint_swarm", new AspectList().add(Aspect.FLUX, 15).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:mind_spider", new AspectList().add(Aspect.FLUX, 5).add(Aspect.FIRE, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:eldritch_guardian", new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DEATH, 20).add(Aspect.UNDEAD, 20));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist_knight", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.MAN, 15).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist_cleric", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.MAN, 15).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist", new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.MAN, 15).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist_portal_lesser", new AspectList().add(Aspect.ELDRITCH, 15).add(Aspect.DARKNESS, 10).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist_portal_greater", new AspectList().add(Aspect.ELDRITCH, 30).add(Aspect.DARKNESS, 20).add(Aspect.MOTION, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taint_crawler", new AspectList().add(Aspect.FLUX, 10).add(Aspect.BEAST, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:turret_crossbow", new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MOTION, 5).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:turret_crossbow_advanced", new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.MOTION, 10).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:arcane_bore", new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.EARTH, 10).add(Aspect.MOTION, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:eldritch_crab", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.BEAST, 10).add(Aspect.TRAP, 10));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:inhabited_zombie", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.UNDEAD, 10).add(Aspect.MAN, 5));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:eldritch_warden", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.DEATH, 40).add(Aspect.UNDEAD, 40));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:eldritch_golem", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.ENERGY, 40).add(Aspect.MECHANISM, 40));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:cultist_leader", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.AVERSION, 40).add(Aspect.MAN, 40));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:taintacle_giant", new AspectList().add(Aspect.ELDRITCH, 40).add(Aspect.BEAST, 40).add(Aspect.FLUX, 40));
        ArcaneresearchApi.registerEntityTag("arcaneresearch:arcaneresearch_golem", new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MAN, 10).add(Aspect.MOTION, 10));
        for (Aspect tag : Aspect.aspects.values()) {
            ArcaneresearchApi.registerEntityTag("arcaneresearch:wisp", new AspectList().add(tag, 5).add(Aspect.AURA, 5).add(Aspect.FLIGHT, 5), new ArcaneresearchApi.EntityTagsNBT("WispType", tag.getTag()));
        }
        ArcaneresearchApi.registerEntityTag("arcaneresearch:wisp", new AspectList().add(Aspect.AURA, 10).add(Aspect.FLIGHT, 5));
    }

    // NEOFORGE_PORT: PotionUtils removed -> PotionContents component (Holder comparison).
    // The no-manager overload is setup-safe (static potion values only); the manager overload
    // additionally traverses datapack BrewingRecipes for reagent aspects (runtime).
    public static AspectList getPotionAspects(ItemStack itemstack) {
        // NEOFORGE_PORT (26.3): Forge read the static PotionBrewing mixes, so reagent aspects were
        // always available; 26.x brewing is datapack recipes -> use the running server's
        // RecipeManager when there is one (was always null -> every potion got the
        // MAGIC 5 / ALCHEMY 5 fallback, incl. TilePotionSprayer costs).
        RecipeManager recipeManager = null;
        net.minecraft.server.MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            recipeManager = server.getRecipeManager();
        }
        return getPotionAspects(itemstack, recipeManager);
    }

    public static AspectList getPotionAspects(ItemStack itemstack, RecipeManager recipeManager) {
        return getPotionAspects(itemstack, recipeManager.getRecipes());
    }

    // NEOFORGE_PORT (26.3) FIX: 1.20.1 Forge had static PotionBrewing on BOTH sides, so a
    // client of a dedicated/LAN server always resolved real reagent aspects. In 26.x brewing
    // is datapack-only and a remote client has no RecipeManager -> it fell back to
    // MAGIC 5 / ALCHEMY 5 (thaumometer scan/JEI aspect display divergence from Forge).
    // This overload runs on the client with the recipe map synced by RecipesReceivedEvent
    // (see ClientRecipeCache) - same traversal, same result values as the server.
    public static AspectList getPotionAspects(ItemStack itemstack,
            java.util.Collection<RecipeHolder<?>> recipes) {
        AspectList tmp = new AspectList();
        boolean didit = false;
        PotionContents contents = itemstack.get(DataComponents.POTION_CONTENTS);
        Holder<Potion> potion = contents != null ? contents.potion().orElse(null) : null;
        if (potion == null || !potion.equals(Potions.WATER)) {
            HashSet<ItemStack> hs = new HashSet<>();
            if (potion != null) {
                getPotionReagentsRecursive(potion, hs, recipes);
            }
            for (ItemStack his : hs) {
                AspectList tmp2 = ArcaneresearchCraftingManager.getObjectTags(his);
                for (Aspect a : tmp2.getAspects()) {
                    tmp.add(a, tmp2.getAmount(a));
                }
                didit = true;
                tmp.add(Aspect.ALCHEMY, 3);
            }
            try {
                for (Aspect a : tmp.getAspects()) {
                    int amt = tmp.getAmount(a);
                    tmp.remove(a, (int) (amt * 0.66D));
                }
            } catch (Exception ignored) {
            }
            if (!didit) {
                tmp.add(Aspect.MAGIC, 5);
                tmp.add(Aspect.ALCHEMY, 5);
            }
        } else {
            tmp.add(Aspect.WATER, 5);
        }
        return tmp;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    // NEOFORGE_PORT: PotionBrewing mix registry removed in 26.x; brewing is datapack
    // BrewingRecipe now. Same traversal semantics (reagents that produce this potion,
    // recursing into non-water inputs); without a RecipeManager only the graceful
    // empty-set path is available.
    public static void getPotionReagentsRecursive(Potion potion, HashSet<ItemStack> hashSet) {
    }

    public static void getPotionReagentsRecursive(Holder<Potion> potion, HashSet<ItemStack> hashSet,
            RecipeManager recipeManager) {
        getPotionReagentsRecursive(potion, hashSet, recipeManager.getRecipes());
    }

    // NEOFORGE_PORT (26.3) FIX: recipe-map variant for the client (synced RecipeMap) -
    // same traversal as the server RecipeManager overload.
    public static void getPotionReagentsRecursive(Holder<Potion> potion, HashSet<ItemStack> hashSet,
            java.util.Collection<RecipeHolder<?>> recipes) {
        if (potion == null || recipes == null) {
            return;
        }
        try {
            for (RecipeHolder<?> holder : recipes) {
                if (!(holder.value() instanceof BrewingRecipe brew)) {
                    continue;
                }
                PotionContents output = brew.getOutput().get(DataComponents.POTION_CONTENTS);
                Holder<Potion> outputPotion = output != null ? output.potion().orElse(null) : null;
                if (outputPotion == null || !outputPotion.equals(potion)) {
                    continue;
                }
                var inputPotions = brew.getInput().potions()
                        .flatMap(net.minecraft.core.component.predicates.PotionsPredicate::potions);
                if (inputPotions.isEmpty()) {
                    continue;
                }
                boolean foundOther = false;
                for (Holder<Potion> inputPotion : inputPotions.get()) {
                    if (inputPotion.equals(potion)) {
                        continue;
                    }
                    foundOther = true;
                    // NEOFORGE_PORT: Ingredient.getItems() removed -> items() stream of Holders.
                    brew.getReagent().ingredient().items().findFirst()
                            .map(reagentHolder -> new ItemStack(reagentHolder.value()))
                            .filter(option -> !option.isEmpty())
                            .ifPresent(hashSet::add);
                    if (!inputPotion.equals(Potions.WATER)) {
                        getPotionReagentsRecursive(inputPotion, hashSet, recipes);
                    }
                }
                if (foundOther) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object unwrapHolder(Object o) {
        if (o instanceof Holder<?> holder) {
            return holder.value();
        }
        return o;
    }

    private static void registerItemAspects() {

        ArcaneresearchApi.registerObjectTag("c:ores/lapis", new AspectList().add(Aspect.EARTH, 5).add(Aspect.SENSES, 15));
        ArcaneresearchApi.registerObjectTag("c:ores/diamond", new AspectList().add(Aspect.EARTH, 5).add(Aspect.DESIRE, 15).add(Aspect.CRYSTAL, 15));
        ArcaneresearchApi.registerObjectTag("c:gems/diamond", new AspectList().add(Aspect.CRYSTAL, 15).add(Aspect.DESIRE, 15));
        ArcaneresearchApi.registerObjectTag("c:ores/redstone", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENERGY, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.REDSTONE_ORE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENERGY, 15));
        ArcaneresearchApi.registerObjectTag("c:ores/emerald", new AspectList().add(Aspect.EARTH, 5).add(Aspect.DESIRE, 10).add(Aspect.CRYSTAL, 15));
        ArcaneresearchApi.registerObjectTag("c:gems/emerald", new AspectList().add(Aspect.CRYSTAL, 15).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag("c:ores/quartz", new AspectList().add(Aspect.EARTH, 5).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag("c:gems/quartz", new AspectList().add(Aspect.CRYSTAL, 5));
        ArcaneresearchApi.registerObjectTag("c:ores/iron", new AspectList().add(Aspect.EARTH, 5).add(Aspect.METAL, 15));
        ArcaneresearchApi.registerObjectTag("c:dusts/iron", new AspectList().add(Aspect.METAL, 15).add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag("c:ingots/iron", new AspectList().add(Aspect.METAL, 15));
        ArcaneresearchApi.registerObjectTag("c:ores/gold", new AspectList().add(Aspect.EARTH, 5).add(Aspect.METAL, 10).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag("c:dusts/gold", new AspectList().add(Aspect.METAL, 10).add(Aspect.DESIRE, 10).add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag("c:ingots/gold", new AspectList().add(Aspect.METAL, 10).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.COAL_ORE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENERGY, 15).add(Aspect.FIRE, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COAL), new AspectList().add(Aspect.ENERGY, 10).add(Aspect.FIRE, 10));
        ArcaneresearchApi.registerObjectTag("c:dusts/redstone", new AspectList().add(Aspect.ENERGY, 10));
        ArcaneresearchApi.registerObjectTag("c:dusts/glowstone", new AspectList().add(Aspect.SENSES, 5).add(Aspect.LIGHT, 10));
        ArcaneresearchApi.registerObjectTag("c:ingots/copper", new AspectList().add(Aspect.METAL, 10).add(Aspect.EXCHANGE, 5));
        ArcaneresearchApi.registerObjectTag("c:dusts/copper", new AspectList().add(Aspect.METAL, 10).add(Aspect.ENTROPY, 1).add(Aspect.EXCHANGE, 5));
        ArcaneresearchApi.registerObjectTag("c:ores/copper", new AspectList().add(Aspect.METAL, 10).add(Aspect.EARTH, 5).add(Aspect.EXCHANGE, 5));

        ArcaneresearchApi.registerObjectTag("c:stone", new AspectList().add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag("c:cobblestone", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BEDROCK), new AspectList().add(Aspect.VOID, 25).add(Aspect.ENTROPY, 25).add(Aspect.EARTH, 25).add(Aspect.DARKNESS, 25));
        ArcaneresearchApi.registerObjectTag("c:dirt", new AspectList().add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.PODZOL), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.FARMLAND), new AspectList().add(Aspect.EARTH, 5).add(Aspect.WATER, 2).add(Aspect.ORDER, 2));
        ArcaneresearchApi.registerObjectTag("c:sand", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENTROPY, 5));
        ArcaneresearchApi.registerObjectTag("minecraft:grass", new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GRASS_BLOCK), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DIRT_PATH), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 2).add(Aspect.ORDER, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.END_STONE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag("c:gravel", new AspectList().add(Aspect.EARTH, 5).add(Aspect.ENTROPY, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.MYCELIUM), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 1).add(Aspect.FLUX, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CLAY_BALL), new AspectList().add(Aspect.WATER, 5).add(Aspect.EARTH, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.NETHERRACK), new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SOUL_SAND), new AspectList().add(Aspect.EARTH, 3).add(Aspect.TRAP, 1).add(Aspect.SOUL, 3));

        ArcaneresearchApi.registerObjectTag("c:obsidian", new AspectList().add(Aspect.EARTH, 5).add(Aspect.FIRE, 5).add(Aspect.DARKNESS, 5));

        ArcaneresearchApi.registerObjectTag("minecraft:logs", new AspectList().add(Aspect.PLANT, 20));
        ArcaneresearchApi.registerObjectTag("minecraft:saplings", new AspectList().add(Aspect.PLANT, 15).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag("minecraft:leaves", new AspectList().add(Aspect.PLANT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ICE), new AspectList().add(Aspect.COLD, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.PACKED_ICE), new AspectList().add(Aspect.COLD, 15).add(Aspect.ORDER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SNOWBALL), new AspectList().add(Aspect.COLD, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SLIME_BALL), new AspectList().add(Aspect.WATER, 5).add(Aspect.LIFE, 5).add(Aspect.ALCHEMY, 1));
        ArcaneresearchApi.registerObjectTag("c:leather", new AspectList().add(Aspect.BEAST, 5).add(Aspect.PROTECT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ROTTEN_FLESH), new AspectList().add(Aspect.MAN, 5).add(Aspect.LIFE, 5).add(Aspect.ENTROPY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FEATHER), new AspectList().add(Aspect.FLIGHT, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BONE), new AspectList().add(Aspect.DEATH, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.EGG), new AspectList().add(Aspect.LIFE, 5).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GUNPOWDER), new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 10).add(Aspect.ALCHEMY, 5));

        ArcaneresearchApi.registerObjectTag("c:glass", new AspectList().add(Aspect.CRYSTAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GLASS), new AspectList().add(Aspect.CRYSTAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GLASS_PANE), new AspectList().add(Aspect.CRYSTAL, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.MOSSY_COBBLESTONE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 3).add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.MOSSY_STONE_BRICKS), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 1).add(Aspect.ORDER, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GRASS_BLOCK), new AspectList().add(Aspect.EARTH, 5).add(Aspect.PLANT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.TALL_GRASS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.AIR, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.FERN), new AspectList().add(Aspect.PLANT, 5).add(Aspect.AIR, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.LARGE_FERN), new AspectList().add(Aspect.PLANT, 5).add(Aspect.AIR, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.LILY_PAD), new AspectList().add(Aspect.PLANT, 5).add(Aspect.WATER, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DEAD_BUSH), new AspectList().add(Aspect.PLANT, 5).add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag("c:vines", new AspectList().add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.VINE), new AspectList().add(Aspect.PLANT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WHEAT_SEEDS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MELON_SEEDS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PUMPKIN_SEEDS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BEETROOT_SEEDS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.NETHER_WART), new AspectList().add(Aspect.PLANT, 1).add(Aspect.FLUX, 2).add(Aspect.ALCHEMY, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WHEAT), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.APPLE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CARROT), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.POTATO), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BEETROOT), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.DESIRE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BAKED_POTATO), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.POISONOUS_POTATO), new AspectList().add(Aspect.PLANT, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.PUMPKIN), new AspectList().add(Aspect.PLANT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MELON_SLICE), new AspectList().add(Aspect.PLANT, 1));

        ArcaneresearchApi.registerObjectTag("minecraft:small_flowers", new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag("minecraft:tall_flowers", new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.POPPY), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DANDELION), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 1).add(Aspect.SENSES, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BROWN_MUSHROOM), new AspectList().add(Aspect.PLANT, 5).add(Aspect.DARKNESS, 2).add(Aspect.EARTH, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.RED_MUSHROOM), new AspectList().add(Aspect.PLANT, 5).add(Aspect.DARKNESS, 2).add(Aspect.FIRE, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BROWN_MUSHROOM_BLOCK), new AspectList().add(Aspect.PLANT, 5).add(Aspect.DARKNESS, 2).add(Aspect.EARTH, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.RED_MUSHROOM_BLOCK), new AspectList().add(Aspect.PLANT, 5).add(Aspect.DARKNESS, 2).add(Aspect.FIRE, 2));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CACTUS), new AspectList().add(Aspect.PLANT, 5).add(Aspect.WATER, 5).add(Aspect.AVERSION, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SUGAR_CANE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.WATER, 3).add(Aspect.AIR, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SUGAR), new AspectList().add(Aspect.DESIRE, 1).add(Aspect.ENERGY, 1).add(Aspect.WATER, 1));

        ArcaneresearchApi.registerObjectTag("minecraft:wool", new AspectList().add(Aspect.BEAST, 15).add(Aspect.CRAFT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.WOOL.white()), new AspectList().add(Aspect.BEAST, 15).add(Aspect.CRAFT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BREAD), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.CRAFT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKIE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.DESIRE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CAKE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 10).add(Aspect.DESIRE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PUMPKIN_PIE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 5).add(Aspect.DESIRE, 2));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COD), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SALMON), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.TROPICAL_FISH), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.WATER, 5).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PUFFERFISH), new AspectList().add(Aspect.BEAST, 5).add(Aspect.DEATH, 5).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_COD), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_SALMON), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHICKEN), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.AIR, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_CHICKEN), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PORKCHOP), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_PORKCHOP), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BEEF), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_BEEF), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUTTON), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_MUTTON), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.RABBIT), new AspectList().add(Aspect.BEAST, 5).add(Aspect.LIFE, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COOKED_RABBIT), new AspectList().add(Aspect.CRAFT, 1).add(Aspect.BEAST, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.RABBIT_HIDE), new AspectList().add(Aspect.BEAST, 5).add(Aspect.PROTECT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.RABBIT_FOOT), new AspectList().add(Aspect.BEAST, 5).add(Aspect.PROTECT, 5).add(Aspect.MOTION, 10).add(Aspect.ALCHEMY, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.STRING), new AspectList().add(Aspect.BEAST, 5).add(Aspect.CRAFT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SPIDER_EYE), new AspectList().add(Aspect.SENSES, 5).add(Aspect.BEAST, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FERMENTED_SPIDER_EYE), new AspectList().add(Aspect.SENSES, 5).add(Aspect.ALCHEMY, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BLAZE_ROD), new AspectList().add(Aspect.FIRE, 15).add(Aspect.ENERGY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BLAZE_POWDER), new AspectList().add(Aspect.FIRE, 10).add(Aspect.ALCHEMY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MAGMA_CREAM), new AspectList().add(Aspect.FIRE, 5).add(Aspect.ALCHEMY, 5).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerObjectTag("c:ender_pearls", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MOTION, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ENDER_PEARL), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MOTION, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ENDER_EYE), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MOTION, 15).add(Aspect.SENSES, 10).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GHAST_TEAR), new AspectList().add(Aspect.UNDEAD, 5).add(Aspect.SOUL, 10).add(Aspect.ALCHEMY, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PHANTOM_MEMBRANE), new AspectList().add(Aspect.FLIGHT, 10).add(Aspect.UNDEAD, 5).add(Aspect.ALCHEMY, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SKELETON_SKULL), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.UNDEAD, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WITHER_SKELETON_SKULL), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.UNDEAD, 10).add(Aspect.ENTROPY, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ZOMBIE_HEAD), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.MAN, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PLAYER_HEAD), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.MAN, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CREEPER_HEAD), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.ENTROPY, 5).add(Aspect.FIRE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DRAGON_HEAD), new AspectList().add(Aspect.DEATH, 10).add(Aspect.SOUL, 10).add(Aspect.FIRE, 10).add(Aspect.DARKNESS, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DRAGON_BREATH), new AspectList().add(Aspect.DARKNESS, 10).add(Aspect.ENTROPY, 10).add(Aspect.FIRE, 10).add(Aspect.ALCHEMY, 10));

        ArcaneresearchApi.registerObjectTag("c:nether_stars", new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MAGIC, 20).add(Aspect.ORDER, 20).add(Aspect.AURA, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.NETHER_STAR), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.MAGIC, 20).add(Aspect.ORDER, 20).add(Aspect.AURA, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.TOTEM_OF_UNDYING), new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10).add(Aspect.LIFE, 25).add(Aspect.UNDEAD, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CHORUS_FLOWER), new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.SENSES, 5).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CHORUS_PLANT), new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHORUS_FRUIT), new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.SENSES, 5).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.POPPED_CHORUS_FRUIT), new AspectList().add(Aspect.ELDRITCH, 5).add(Aspect.SENSES, 5).add(Aspect.PLANT, 4).add(Aspect.FIRE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SHULKER_SHELL), new AspectList().add(Aspect.PROTECT, 10).add(Aspect.ELDRITCH, 5).add(Aspect.BEAST, 5).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ELYTRA), new AspectList().add(Aspect.FLIGHT, 20).add(Aspect.MOTION, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.END_ROD), new AspectList().add(Aspect.FIRE, 1).add(Aspect.LIGHT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DRAGON_EGG), new AspectList().add(Aspect.ELDRITCH, 15).add(Aspect.BEAST, 15).add(Aspect.DARKNESS, 15).add(Aspect.MOTION, 15).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.END_PORTAL_FRAME), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.ENERGY, 10).add(Aspect.MOTION, 10).add(Aspect.MAGIC, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GLOWSTONE), new AspectList().add(Aspect.SENSES, 5).add(Aspect.LIGHT, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PRISMARINE_SHARD), new AspectList().add(Aspect.WATER, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PRISMARINE_CRYSTALS), new AspectList().add(Aspect.WATER, 5).add(Aspect.CRYSTAL, 5).add(Aspect.LIGHT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.INK_SAC), new AspectList().add(Aspect.WATER, 2).add(Aspect.BEAST, 2).add(Aspect.DARKNESS, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GLOW_INK_SAC), new AspectList().add(Aspect.WATER, 2).add(Aspect.BEAST, 2).add(Aspect.LIGHT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.EXPERIENCE_BOTTLE), new AspectList().add(Aspect.MIND, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.NAME_TAG), new AspectList().add(Aspect.MIND, 10).add(Aspect.BEAST, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SADDLE), new AspectList().add(Aspect.BEAST, 10).add(Aspect.MOTION, 10).add(Aspect.ORDER, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.IRON_HORSE_ARMOR), new AspectList().add(Aspect.METAL, 15).add(Aspect.PROTECT, 10).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GOLDEN_HORSE_ARMOR), new AspectList().add(Aspect.METAL, 10).add(Aspect.PROTECT, 15).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DIAMOND_HORSE_ARMOR), new AspectList().add(Aspect.CRYSTAL, 15).add(Aspect.PROTECT, 20).add(Aspect.BEAST, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHAINMAIL_HELMET), new AspectList().add(Aspect.METAL, 42).add(Aspect.PROTECT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHAINMAIL_CHESTPLATE), new AspectList().add(Aspect.METAL, 67).add(Aspect.PROTECT, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHAINMAIL_LEGGINGS), new AspectList().add(Aspect.METAL, 58).add(Aspect.PROTECT, 12));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CHAINMAIL_BOOTS), new AspectList().add(Aspect.METAL, 33).add(Aspect.PROTECT, 8));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FLINT), new AspectList().add(Aspect.EARTH, 5).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FLINT_AND_STEEL), new AspectList().add(Aspect.FIRE, 10).add(Aspect.TOOL, 5).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COMPASS), new AspectList().add(Aspect.METAL, 5).add(Aspect.SENSES, 10).add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CLOCK), new AspectList().add(Aspect.METAL, 5).add(Aspect.SENSES, 5).add(Aspect.MECHANISM, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FISHING_ROD), new AspectList().add(Aspect.WATER, 10).add(Aspect.TOOL, 5).add(Aspect.BEAST, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.LEAD), new AspectList().add(Aspect.BEAST, 5).add(Aspect.CRAFT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SHIELD), new AspectList().add(Aspect.PROTECT, 20).add(Aspect.PLANT, 5).add(Aspect.METAL, 3));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CHEST), new AspectList().add(Aspect.VOID, 15).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.TRAPPED_CHEST), new AspectList().add(Aspect.VOID, 15).add(Aspect.TRAP, 10).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ENDER_CHEST), new AspectList().add(Aspect.VOID, 20).add(Aspect.EXCHANGE, 10).add(Aspect.MOTION, 10).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BUCKET), new AspectList().add(Aspect.VOID, 5).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WATER_BUCKET), new AspectList().add(Aspect.VOID, 5).add(Aspect.METAL, 5).add(Aspect.WATER, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.LAVA_BUCKET), new AspectList().add(Aspect.VOID, 5).add(Aspect.METAL, 5).add(Aspect.FIRE, 15).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MILK_BUCKET), new AspectList().add(Aspect.VOID, 5).add(Aspect.METAL, 5).add(Aspect.LIFE, 10).add(Aspect.BEAST, 5).add(Aspect.WATER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GLASS_BOTTLE), new AspectList().add(Aspect.VOID, 5).add(Aspect.CRYSTAL, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BOWL), new AspectList().add(Aspect.VOID, 5).add(Aspect.PLANT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.FLOWER_POT), new AspectList().add(Aspect.VOID, 5).add(Aspect.PLANT, 5).add(Aspect.EARTH, 3));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.PAPER), new AspectList().add(Aspect.MIND, 2).add(Aspect.PLANT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BOOK), new AspectList().add(Aspect.MIND, 8).add(Aspect.BEAST, 5).add(Aspect.PLANT, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ENCHANTED_BOOK), new AspectList().add(Aspect.MIND, 8).add(Aspect.MAGIC, 15).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BOOKSHELF), new AspectList().add(Aspect.MIND, 20).add(Aspect.PLANT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WRITABLE_BOOK), new AspectList().add(Aspect.MIND, 8).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.WRITTEN_BOOK), new AspectList().add(Aspect.MIND, 15).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MAP), new AspectList().add(Aspect.MIND, 5).add(Aspect.SENSES, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.LEVER), new AspectList().add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.STONE_BUTTON), new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.EARTH, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.PISTON), new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MOTION, 10).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.STICKY_PISTON), new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.MOTION, 10).add(Aspect.METAL, 5).add(Aspect.LIFE, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.REPEATER), new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.ENERGY, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.COMPARATOR), new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.ORDER, 5).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.HOPPER), new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.EXCHANGE, 10).add(Aspect.VOID, 5).add(Aspect.METAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DROPPER), new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.EXCHANGE, 10).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DISPENSER), new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.EXCHANGE, 10).add(Aspect.VOID, 5).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.OBSERVER), new AspectList().add(Aspect.MECHANISM, 5).add(Aspect.SENSES, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DAYLIGHT_DETECTOR), new AspectList().add(Aspect.SENSES, 10).add(Aspect.LIGHT, 10).add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.TRIPWIRE_HOOK), new AspectList().add(Aspect.SENSES, 5).add(Aspect.MECHANISM, 5).add(Aspect.TRAP, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.RAIL), new AspectList().add(Aspect.MOTION, 10).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.POWERED_RAIL), new AspectList().add(Aspect.MOTION, 10).add(Aspect.METAL, 5).add(Aspect.ENERGY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.DETECTOR_RAIL), new AspectList().add(Aspect.MOTION, 10).add(Aspect.METAL, 5).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ACTIVATOR_RAIL), new AspectList().add(Aspect.MOTION, 10).add(Aspect.METAL, 5).add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MINECART), new AspectList().add(Aspect.MOTION, 15).add(Aspect.METAL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CRAFTING_TABLE), new AspectList().add(Aspect.CRAFT, 20).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.FURNACE), new AspectList().add(Aspect.FIRE, 10).add(Aspect.CRAFT, 5).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BLAST_FURNACE), new AspectList().add(Aspect.FIRE, 15).add(Aspect.CRAFT, 5).add(Aspect.METAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SMOKER), new AspectList().add(Aspect.FIRE, 15).add(Aspect.CRAFT, 5).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.BREWING_STAND), new AspectList().add(Aspect.CRAFT, 15).add(Aspect.ALCHEMY, 25).add(Aspect.FIRE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.CAULDRON), new AspectList().add(Aspect.VOID, 10).add(Aspect.ALCHEMY, 15).add(Aspect.METAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ENCHANTING_TABLE), new AspectList().add(Aspect.MAGIC, 25).add(Aspect.CRAFT, 15).add(Aspect.MIND, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ANVIL), new AspectList().add(Aspect.METAL, 40).add(Aspect.CRAFT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.GRINDSTONE), new AspectList().add(Aspect.CRAFT, 10).add(Aspect.TOOL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SMITHING_TABLE), new AspectList().add(Aspect.CRAFT, 15).add(Aspect.TOOL, 10).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.LOOM), new AspectList().add(Aspect.CRAFT, 10).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.STONECUTTER), new AspectList().add(Aspect.CRAFT, 10).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.CARTOGRAPHY_TABLE), new AspectList().add(Aspect.CRAFT, 10).add(Aspect.SENSES, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.FLETCHING_TABLE), new AspectList().add(Aspect.CRAFT, 10).add(Aspect.AVERSION, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.JUKEBOX), new AspectList().add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 10).add(Aspect.AIR, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.NOTE_BLOCK), new AspectList().add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 10).add(Aspect.AIR, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_13), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.DESIRE, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_CAT), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.BEAST, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_BLOCKS), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.TOOL, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_CHIRP), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.EARTH, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_FAR), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.ELDRITCH, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_MALL), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.MAN, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_MELLOHI), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.CRAFT, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_STAL), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.DARKNESS, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_STRAD), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.ENERGY, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_WARD), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.LIFE, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_11), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.DESIRE, 15).add(Aspect.ENTROPY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_WAIT), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.TRAP, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.MUSIC_DISC_PIGSTEP), new AspectList().add(Aspect.SENSES, 15).add(Aspect.AIR, 5).add(Aspect.FIRE, 5).add(Aspect.DESIRE, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ARROW), new AspectList().add(Aspect.AVERSION, 5).add(Aspect.FLIGHT, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SPECTRAL_ARROW), new AspectList().add(Aspect.AVERSION, 5).add(Aspect.FLIGHT, 3).add(Aspect.SENSES, 10).add(Aspect.MAGIC, 5));
        // NEOFORGE_PORT (26.3): Forge primal-arrow aspects were missing in the port.
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_AIR.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_FIRE.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_WATER.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_EARTH.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_ORDER.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMAL_ARROW_ENTROPY.get()), new AspectList().add(Aspect.AVERSION, 1).add(Aspect.FLIGHT, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GOLDEN_APPLE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 10).add(Aspect.DESIRE, 15).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), new AspectList().add(Aspect.PLANT, 5).add(Aspect.LIFE, 15).add(Aspect.PROTECT, 15).add(Aspect.MAGIC, 10).add(Aspect.DESIRE, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GOLDEN_CARROT), new AspectList().add(Aspect.PLANT, 5).add(Aspect.SENSES, 10).add(Aspect.ALCHEMY, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.GLISTERING_MELON_SLICE), new AspectList().add(Aspect.PLANT, 1).add(Aspect.ALCHEMY, 5).add(Aspect.DESIRE, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SPONGE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.TRAP, 5).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.WET_SPONGE), new AspectList().add(Aspect.EARTH, 5).add(Aspect.TRAP, 5).add(Aspect.WATER, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.COBWEB), new AspectList().add(Aspect.TRAP, 5).add(Aspect.BEAST, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.TORCH), new AspectList().add(Aspect.LIGHT, 5).add(Aspect.FIRE, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SOUL_TORCH), new AspectList().add(Aspect.LIGHT, 5).add(Aspect.SOUL, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.LANTERN), new AspectList().add(Aspect.LIGHT, 10).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SOUL_LANTERN), new AspectList().add(Aspect.LIGHT, 10).add(Aspect.METAL, 5).add(Aspect.SOUL, 3));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BEACON), new AspectList().add(Aspect.AURA, 10).add(Aspect.MAGIC, 10).add(Aspect.EXCHANGE, 10).add(Aspect.LIGHT, 15));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.black()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.DARKNESS, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.white()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.LIGHT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.red()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.FIRE, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.blue()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.WATER, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.yellow()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.LIGHT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.green()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.PLANT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.brown()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.EARTH, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.cyan()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.WATER, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.purple()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.MAGIC, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.orange()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.FIRE, 1).add(Aspect.LIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.pink()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.LIFE, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.lime()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.PLANT, 1).add(Aspect.LIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.magenta()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.MAGIC, 1).add(Aspect.SENSES, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.lightBlue()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.AIR, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.gray()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.ENTROPY, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.DYE.lightGray()), new AspectList().add(Aspect.SENSES, 5).add(Aspect.ORDER, 2));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.NETHERITE_SCRAP), new AspectList().add(Aspect.METAL, 15).add(Aspect.FIRE, 10).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.NETHERITE_INGOT), new AspectList().add(Aspect.METAL, 25).add(Aspect.FIRE, 15).add(Aspect.VOID, 10).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.NETHERITE_BLOCK), new AspectList().add(Aspect.METAL, 200).add(Aspect.FIRE, 120).add(Aspect.VOID, 80).add(Aspect.DESIRE, 80));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.ANCIENT_DEBRIS), new AspectList().add(Aspect.METAL, 15).add(Aspect.FIRE, 5).add(Aspect.EARTH, 5).add(Aspect.VOID, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.AMETHYST_SHARD), new AspectList().add(Aspect.CRYSTAL, 10).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.AMETHYST_BLOCK), new AspectList().add(Aspect.CRYSTAL, 25).add(Aspect.AURA, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BUDDING_AMETHYST), new AspectList().add(Aspect.CRYSTAL, 20).add(Aspect.AURA, 10).add(Aspect.LIFE, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.HONEYCOMB), new AspectList().add(Aspect.LIFE, 5).add(Aspect.BEAST, 5).add(Aspect.ORDER, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.HONEY_BOTTLE), new AspectList().add(Aspect.LIFE, 10).add(Aspect.BEAST, 5).add(Aspect.DESIRE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.HONEYCOMB_BLOCK), new AspectList().add(Aspect.LIFE, 15).add(Aspect.BEAST, 15).add(Aspect.ORDER, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.HONEY_BLOCK), new AspectList().add(Aspect.LIFE, 20).add(Aspect.BEAST, 10).add(Aspect.TRAP, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BEE_NEST), new AspectList().add(Aspect.LIFE, 10).add(Aspect.BEAST, 15).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.BEEHIVE), new AspectList().add(Aspect.LIFE, 10).add(Aspect.BEAST, 15).add(Aspect.CRAFT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.RAW_COPPER), new AspectList().add(Aspect.METAL, 10).add(Aspect.EARTH, 5).add(Aspect.EXCHANGE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.COPPER_BLOCK.weathering().unaffected()), new AspectList().add(Aspect.METAL, 80).add(Aspect.EXCHANGE, 40));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.RAW_COPPER_BLOCK), new AspectList().add(Aspect.METAL, 80).add(Aspect.EARTH, 40).add(Aspect.EXCHANGE, 40));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.SPYGLASS), new AspectList().add(Aspect.SENSES, 20).add(Aspect.METAL, 5).add(Aspect.CRYSTAL, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SCULK), new AspectList().add(Aspect.SOUL, 10).add(Aspect.DARKNESS, 10).add(Aspect.ENTROPY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SCULK_SENSOR), new AspectList().add(Aspect.SOUL, 10).add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SCULK_CATALYST), new AspectList().add(Aspect.SOUL, 20).add(Aspect.LIFE, 10).add(Aspect.DEATH, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Blocks.SCULK_SHRIEKER), new AspectList().add(Aspect.SOUL, 20).add(Aspect.SENSES, 15).add(Aspect.DARKNESS, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.ECHO_SHARD), new AspectList().add(Aspect.SOUL, 15).add(Aspect.SENSES, 15).add(Aspect.DARKNESS, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.RECOVERY_COMPASS), new AspectList().add(Aspect.SOUL, 20).add(Aspect.SENSES, 15).add(Aspect.DEATH, 10));

        ArcaneresearchApi.registerEntityTag("minecraft:camel", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:sniffer", new AspectList().add(Aspect.BEAST, 15).add(Aspect.SENSES, 10).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:warden", new AspectList().add(Aspect.DARKNESS, 50).add(Aspect.SOUL, 50).add(Aspect.DEATH, 30));
        ArcaneresearchApi.registerEntityTag("minecraft:allay", new AspectList().add(Aspect.FLIGHT, 10).add(Aspect.MAGIC, 10).add(Aspect.EXCHANGE, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:frog", new AspectList().add(Aspect.BEAST, 5).add(Aspect.WATER, 5).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:tadpole", new AspectList().add(Aspect.BEAST, 2).add(Aspect.WATER, 5).add(Aspect.LIFE, 3));
        ArcaneresearchApi.registerEntityTag("minecraft:goat", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EARTH, 5).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:axolotl", new AspectList().add(Aspect.BEAST, 10).add(Aspect.WATER, 10).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:glow_squid", new AspectList().add(Aspect.BEAST, 5).add(Aspect.WATER, 10).add(Aspect.LIGHT, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:piglin", new AspectList().add(Aspect.BEAST, 10).add(Aspect.FIRE, 10).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:piglin_brute", new AspectList().add(Aspect.BEAST, 15).add(Aspect.FIRE, 10).add(Aspect.AVERSION, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:hoglin", new AspectList().add(Aspect.BEAST, 20).add(Aspect.FIRE, 10).add(Aspect.AVERSION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:zoglin", new AspectList().add(Aspect.BEAST, 20).add(Aspect.UNDEAD, 15).add(Aspect.AVERSION, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:strider", new AspectList().add(Aspect.BEAST, 10).add(Aspect.FIRE, 15).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:drowned", new AspectList().add(Aspect.UNDEAD, 20).add(Aspect.MAN, 10).add(Aspect.WATER, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:phantom", new AspectList().add(Aspect.UNDEAD, 15).add(Aspect.FLIGHT, 15).add(Aspect.DARKNESS, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:turtle", new AspectList().add(Aspect.BEAST, 10).add(Aspect.WATER, 10).add(Aspect.PROTECT, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:dolphin", new AspectList().add(Aspect.BEAST, 15).add(Aspect.WATER, 15).add(Aspect.MOTION, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:panda", new AspectList().add(Aspect.BEAST, 15).add(Aspect.PLANT, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:fox", new AspectList().add(Aspect.BEAST, 10).add(Aspect.EARTH, 5).add(Aspect.DESIRE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:bee", new AspectList().add(Aspect.BEAST, 5).add(Aspect.FLIGHT, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:cat", new AspectList().add(Aspect.BEAST, 10).add(Aspect.ENTROPY, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:ravager", new AspectList().add(Aspect.BEAST, 30).add(Aspect.AVERSION, 20).add(Aspect.MAN, 5));
        ArcaneresearchApi.registerEntityTag("minecraft:pillager", new AspectList().add(Aspect.AVERSION, 10).add(Aspect.MAN, 15));
        ArcaneresearchApi.registerEntityTag("minecraft:wandering_trader", new AspectList().add(Aspect.MAN, 15).add(Aspect.EXCHANGE, 10));
        ArcaneresearchApi.registerEntityTag("minecraft:trader_llama", new AspectList().add(Aspect.BEAST, 15).add(Aspect.EXCHANGE, 5));

        // NEOFORGE_PORT: ForgeRegistries.POTIONS removed; BuiltInRegistries.POTION is Iterable
        // (static values, setup-safe on both sides). Stacks built via PotionContents; reagent
        // aspects resolve at runtime through the manager overload (datapack BrewingRecipes).
        registerPotionAspects();
    }

    // NEOFORGE_PORT (26.3): Forge registered these once in registerItemAspects with brewing data
    // already present. Here the content-setup pass runs before any server exists (fallback
    // aspects), so Arcaneresearch.onServerStarting calls this again with the server's brewing
    // recipes available (same stacks/keys -> entries are overwritten with the Forge values).
    public static void registerPotionAspects() {
        for (Potion potion : BuiltInRegistries.POTION) {
            Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
            ItemStack stack = PotionContents.createItemStack(Items.POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack, getPotionAspects(stack).add(Aspect.WATER, 5));
            ItemStack stack2 = PotionContents.createItemStack(Items.TIPPED_ARROW, holder);
            ArcaneresearchApi.registerObjectTag(stack2, getPotionAspects(stack2).add(Aspect.AVERSION, 5));
            ItemStack stack3 = PotionContents.createItemStack(Items.SPLASH_POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack3, getPotionAspects(stack3).add(Aspect.ENERGY, 5));
            ItemStack stack4 = PotionContents.createItemStack(Items.LINGERING_POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack4, getPotionAspects(stack4).add(Aspect.TRAP, 5));
        }
    }

    // NEOFORGE_PORT (26.3) FIX: client half (TilePotionSprayer costs are server-side, but
    // thaumometer scan tooltips / JEI aspect accessors run on the client). Runs on the client
    // when the synced recipe map arrives (ClientRecipeCache); a client of an integrated server
    // already got correct values via the running-server path - re-registering here is an
    // idempotent map overwrite, so no duplication.
    public static void registerPotionAspects(java.util.Collection<RecipeHolder<?>> recipes) {
        for (Potion potion : BuiltInRegistries.POTION) {
            Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
            ItemStack stack = PotionContents.createItemStack(Items.POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack, getPotionAspects(stack, recipes).add(Aspect.WATER, 5));
            ItemStack stack2 = PotionContents.createItemStack(Items.TIPPED_ARROW, holder);
            ArcaneresearchApi.registerObjectTag(stack2, getPotionAspects(stack2, recipes).add(Aspect.AVERSION, 5));
            ItemStack stack3 = PotionContents.createItemStack(Items.SPLASH_POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack3, getPotionAspects(stack3, recipes).add(Aspect.ENERGY, 5));
            ItemStack stack4 = PotionContents.createItemStack(Items.LINGERING_POTION, holder);
            ArcaneresearchApi.registerObjectTag(stack4, getPotionAspects(stack4, recipes).add(Aspect.TRAP, 5));
        }
    }

    private static void registerArcaneresearchItems() {

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_AIR.get()), new AspectList().add(Aspect.AIR, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_FIRE.get()), new AspectList().add(Aspect.FIRE, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_WATER.get()), new AspectList().add(Aspect.WATER, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_EARTH.get()), new AspectList().add(Aspect.EARTH, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_ORDER.get()), new AspectList().add(Aspect.ORDER, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_ENTROPY.get()), new AspectList().add(Aspect.ENTROPY, 15).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRYSTAL_FLUX.get()), new AspectList().add(Aspect.FLUX, 15).add(Aspect.CRYSTAL, 10));

        ArcaneresearchApi.registerObjectTag("arcaneresearch:ores/cinnabar", new AspectList().add(Aspect.EARTH, 5).add(Aspect.METAL, 10).add(Aspect.ALCHEMY, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag("arcaneresearch:ores/amber", new AspectList().add(Aspect.EARTH, 5).add(Aspect.TRAP, 10).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CINNABAR_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.METAL, 10).add(Aspect.ALCHEMY, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.AMBER_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.TRAP, 10).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.QUARTZ_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.CRYSTAL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.DEEPSLATE_CINNABAR_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.METAL, 10).add(Aspect.ALCHEMY, 5).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.DEEPSLATE_AMBER_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.TRAP, 10).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.DEEPSLATE_QUARTZ_ORE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.CRYSTAL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.QUICKSILVER.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.DEATH, 5).add(Aspect.ALCHEMY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.AMBER.get()), new AspectList().add(Aspect.TRAP, 10).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_INGOT.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.MAGIC, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_METAL_INGOT.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.VOID, 10).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.BRASS_INGOT.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.SALIS_MUNDUS.get()), new AspectList().add(Aspect.MAGIC, 5).add(Aspect.ENERGY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.TALLOW.get()), new AspectList().add(Aspect.LIFE, 5).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.ALUMENTUM.get()), new AspectList().add(Aspect.FIRE, 15).add(Aspect.ENERGY, 15).add(Aspect.ALCHEMY, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PLATE_BRASS.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.TOOL, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PLATE_IRON.get()), new AspectList().add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PLATE_THAUMIUM.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PLATE_VOID.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.VOID, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_NUGGET.get()), new AspectList().add(Aspect.METAL, 1).add(Aspect.MAGIC, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_METAL_NUGGET.get()), new AspectList().add(Aspect.METAL, 1).add(Aspect.VOID, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.BRASS_NUGGET.get()), new AspectList().add(Aspect.METAL, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMONOMICON.get()), new AspectList().add(Aspect.MIND, 20).add(Aspect.MAGIC, 10).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMOMETER.get()), new AspectList().add(Aspect.SENSES, 10).add(Aspect.AURA, 10).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.SCRIBING_TOOLS.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.CRAFT, 5));
        // NEOFORGE_PORT (26.3): Forge curio/taint/notes/wand-foci/note aspects were missing in the port.
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIOSITY.get()), new AspectList().add(Aspect.MIND, 10).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_ARCANE.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_PRESERVED.get()), new AspectList().add(Aspect.LIFE, 10).add(Aspect.DEATH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_ANCIENT.get()), new AspectList().add(Aspect.EARTH, 10).add(Aspect.ORDER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_ELDRITCH.get()), new AspectList().add(Aspect.ELDRITCH, 10).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_KNOWLEDGE.get()), new AspectList().add(Aspect.MIND, 15).add(Aspect.SENSES, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_TWISTED.get()), new AspectList().add(Aspect.ENTROPY, 10).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CURIO_RITES.get()), new AspectList().add(Aspect.DEATH, 10).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.TAINT_SLIME.get()), new AspectList().add(Aspect.FLUX, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.TAINT_TENDRIL.get()), new AspectList().add(Aspect.FLUX, 5).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModFluids.PURIFYING_FLUID_BUCKET.get()), new AspectList().add(Aspect.WATER, 10).add(Aspect.LIFE, 5).add(Aspect.ORDER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PECH_WAND.get()), new AspectList().add(Aspect.MAN, 5).add(Aspect.MAGIC, 10).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_SUN.get()), new AspectList().add(Aspect.MIND, 10).add(Aspect.LIGHT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_STARS_1.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_STARS_2.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_STARS_3.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_STARS_4.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_1.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_2.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_3.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_4.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_5.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_6.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_7.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_MOON_8.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_CAP.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.CRAFT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_POUCH.get()), new AspectList().add(Aspect.CRAFT, 5).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_FIRE.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.FIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_FROST.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.COLD, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_SHOCK.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.ENERGY, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_EXCAVATION.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.EARTH, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_BUILDER.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.CRAFT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_TRADE.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.EXCHANGE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_HOLE.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.MOTION, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_GRAPPLE.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.MOTION, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_SHARD.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.CRYSTAL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_PRIMAL.get()), new AspectList().add(Aspect.MAGIC, 15).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_HELLBAT.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.FIRE, 10).add(Aspect.BEAST, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_FOCUS_PECH.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.EXCHANGE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(WandItems.WAND_PECH_BLAST.get()), new AspectList().add(Aspect.ENERGY, 10).add(Aspect.ENTROPY, 5));
        try {
            if (arcaneresearch.discovered.note.DiscoveredNoteSetup.getNoteItem() != null) {
                ArcaneresearchApi.registerObjectTag(
                        new ItemStack(arcaneresearch.discovered.note.DiscoveredNoteSetup.getNoteItem()),
                        new AspectList().add(Aspect.MAGIC, 5).add(Aspect.MIND, 10));
            }
        } catch (Exception ignored) {
        }

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PHIAL_EMPTY.get()), new AspectList().add(Aspect.VOID, 3).add(Aspect.CRYSTAL, 3));
        // NEOFORGE_PORT (26.3): Forge entry missing in the port.
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.WISP_ESSENCE.get()), new AspectList().add(Aspect.AURA, 2));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.LOOT_BAG_COMMON.get()), new AspectList().add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.LOOT_BAG_UNCOMMON.get()), new AspectList().add(Aspect.DESIRE, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.LOOT_BAG_RARE.get()), new AspectList().add(Aspect.DESIRE, 30));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.PRIMORDIAL_PEARL.get()), new AspectList()
                .add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10)
                .add(Aspect.EARTH, 10).add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CELESTIAL_NOTES_SUN.get()), new AspectList().add(Aspect.MIND, 5).add(Aspect.DARKNESS, 5).add(Aspect.LIGHT, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get()), new AspectList().add(Aspect.LIFE, 10).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.ZOMBIE_BRAIN.get()), new AspectList().add(Aspect.LIFE, 5).add(Aspect.MIND, 20).add(Aspect.UNDEAD, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.TAINT_FIBRE.get()), new AspectList().add(Aspect.PLANT, 5).add(Aspect.FLUX, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.GREATWOOD_LOG.get()), new AspectList().add(Aspect.PLANT, 20).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.SILVERWOOD_LOG.get()), new AspectList().add(Aspect.PLANT, 20).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.GREATWOOD_LEAVES.get()), new AspectList().add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.SILVERWOOD_LEAVES.get()), new AspectList().add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.GREATWOOD_SAPLING.get()), new AspectList().add(Aspect.PLANT, 15).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.SILVERWOOD_SAPLING.get()), new AspectList().add(Aspect.PLANT, 15).add(Aspect.AURA, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.SHIMMERLEAF.get()), new AspectList().add(Aspect.PLANT, 5).add(Aspect.AURA, 10).add(Aspect.ENERGY, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CINDERPEARL.get()), new AspectList().add(Aspect.PLANT, 5).add(Aspect.AURA, 5).add(Aspect.FIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.VISHROOM.get()), new AspectList().add(Aspect.PLANT, 2).add(Aspect.DEATH, 1).add(Aspect.MAGIC, 1).add(Aspect.ENTROPY, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ANCIENT_STONE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ANCIENT_STONE_TILE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ANCIENT_STONE_ROCK.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ANCIENT_STONE_DOORWAY.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 5).add(Aspect.TRAP, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ANCIENT_STONE_GLYPHED.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.ELDRITCH, 5).add(Aspect.MIND, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ELDRITCH_STONE_TILE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.ELDRITCH, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ARCANE_STONE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.MAGIC, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ARCANE_STONE_BRICK.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.MAGIC, 3).add(Aspect.ORDER, 1));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.THAUMIUM_BLOCK.get()), new AspectList().add(Aspect.METAL, 80).add(Aspect.MAGIC, 80));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.VOID_METAL_BLOCK.get()), new AspectList().add(Aspect.METAL, 80).add(Aspect.VOID, 80).add(Aspect.DARKNESS, 40));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BRASS_BLOCK.get()), new AspectList().add(Aspect.METAL, 80).add(Aspect.TOOL, 40));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.POROUS_STONE.get()), new AspectList().add(Aspect.EARTH, 5).add(Aspect.VOID, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ARCANE_WORKBENCH.get()), new AspectList().add(Aspect.CRAFT, 20).add(Aspect.MAGIC, 5).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CRUCIBLE.get()), new AspectList().add(Aspect.VOID, 10).add(Aspect.CRAFT, 20).add(Aspect.ALCHEMY, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.RESEARCH_TABLE.get()), new AspectList().add(Aspect.MIND, 10).add(Aspect.CRAFT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.INFUSION_MATRIX.get()), new AspectList().add(Aspect.MAGIC, 20).add(Aspect.CRAFT, 15).add(Aspect.ELDRITCH, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.PEDESTAL_ARCANE.get()), new AspectList().add(Aspect.MAGIC, 3).add(Aspect.AIR, 3).add(Aspect.EARTH, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.PEDESTAL_ANCIENT.get()), new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ELDRITCH, 3).add(Aspect.EARTH, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.PEDESTAL_ELDRITCH.get()), new AspectList().add(Aspect.MAGIC, 3).add(Aspect.ELDRITCH, 5).add(Aspect.VOID, 3));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.GOGGLES.get()), new AspectList().add(Aspect.SENSES, 10).add(Aspect.AURA, 10).add(Aspect.PROTECT, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_HELM.get()), new AspectList().add(Aspect.METAL, 20).add(Aspect.MAGIC, 20).add(Aspect.PROTECT, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_CHEST.get()), new AspectList().add(Aspect.METAL, 35).add(Aspect.MAGIC, 35).add(Aspect.PROTECT, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_LEGS.get()), new AspectList().add(Aspect.METAL, 30).add(Aspect.MAGIC, 30).add(Aspect.PROTECT, 12));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_BOOTS.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.MAGIC, 15).add(Aspect.PROTECT, 8));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_HELM.get()), new AspectList().add(Aspect.METAL, 20).add(Aspect.VOID, 20).add(Aspect.PROTECT, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_CHEST.get()), new AspectList().add(Aspect.METAL, 35).add(Aspect.VOID, 35).add(Aspect.PROTECT, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_LEGS.get()), new AspectList().add(Aspect.METAL, 30).add(Aspect.VOID, 30).add(Aspect.PROTECT, 18));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_BOOTS.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.VOID, 15).add(Aspect.PROTECT, 12));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_SWORD.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.MAGIC, 10).add(Aspect.AVERSION, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_PICK.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.MAGIC, 15).add(Aspect.TOOL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_AXE.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.MAGIC, 15).add(Aspect.TOOL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_SHOVEL.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.MAGIC, 5).add(Aspect.TOOL, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.THAUMIUM_HOE.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.MAGIC, 10).add(Aspect.TOOL, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_SWORD.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.VOID, 10).add(Aspect.AVERSION, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_PICK.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.VOID, 15).add(Aspect.TOOL, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_AXE.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.VOID, 15).add(Aspect.TOOL, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_SHOVEL.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.VOID, 5).add(Aspect.TOOL, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_HOE.get()), new AspectList().add(Aspect.METAL, 10).add(Aspect.VOID, 10).add(Aspect.TOOL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_BLADE.get()), new AspectList().add(Aspect.METAL,
                15).add(Aspect.ELDRITCH, 10).add(Aspect.DEATH, 10).add(Aspect.AVERSION, 15));

        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.IRON_HELMET), new AspectList().add(Aspect.METAL, 42));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.IRON_CHESTPLATE), new AspectList().add(Aspect.METAL, 67));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.IRON_LEGGINGS), new AspectList().add(Aspect.METAL, 58));
        ArcaneresearchApi.registerObjectTag(new ItemStack(Items.IRON_BOOTS), new AspectList().add(Aspect.METAL, 33));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PLATE_CHEST.get()), new AspectList().add(Aspect.METAL, 67).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PRAETOR_CHEST.get()), new AspectList().add(Aspect.METAL, 67).add(Aspect.ELDRITCH, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_ROBE_CHEST.get()), new AspectList().add(Aspect.BEAST, 22).add(Aspect.PROTECT, 22).add(Aspect.MAGIC, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PLATE_LEGS.get()), new AspectList().add(Aspect.METAL, 58).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PRAETOR_LEGS.get()), new AspectList().add(Aspect.METAL, 58).add(Aspect.ELDRITCH, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_ROBE_LEGS.get()), new AspectList().add(Aspect.BEAST, 20).add(Aspect.PROTECT, 20).add(Aspect.MAGIC, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PLATE_HELM.get()), new AspectList().add(Aspect.METAL, 42).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_PRAETOR_HELM.get()), new AspectList().add(Aspect.METAL, 42).add(Aspect.ELDRITCH, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_ROBE_HELM.get()), new AspectList().add(Aspect.BEAST, 14).add(Aspect.PROTECT, 14).add(Aspect.MAGIC, 5).add(Aspect.ELDRITCH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CRIMSON_BOOTS.get()), new AspectList().add(Aspect.METAL, 33).add(Aspect.ELDRITCH, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CASTER_BASIC.get()), new AspectList().add(Aspect.MAGIC, 20).add(Aspect.AURA, 10).add(Aspect.TOOL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.FOCUS_BLANK.get()), new AspectList().add(Aspect.MAGIC, 10).add(Aspect.AURA, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.FOCUS_ADVANCED.get()), new AspectList().add(Aspect.MAGIC, 20).add(Aspect.AURA, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.AMULET_VIS_FOUND.get()), new AspectList().add(Aspect.AURA, 20).add(Aspect.METAL, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.AMULET_VIS_CRAFTED.get()), new AspectList().add(Aspect.AURA, 20).add(Aspect.METAL, 5).add(Aspect.MAGIC, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.BAUBLE_RING_APPRENTICE.get()), new AspectList().add(Aspect.AURA, 5).add(Aspect.METAL, 5).add(Aspect.MAGIC, 20));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLOUD_RING.get()), new AspectList().add(Aspect.FLIGHT, 15).add(Aspect.AIR, 15).add(Aspect.METAL, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.JAR_NORMAL.get()), new AspectList().add(Aspect.VOID, 10).add(Aspect.CRYSTAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.JAR_VOID.get()), new AspectList().add(Aspect.VOID, 20).add(Aspect.CRYSTAL, 5).add(Aspect.DARKNESS, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.JAR_BRAIN.get()), new AspectList().add(Aspect.VOID, 10).add(Aspect.CRYSTAL, 5).add(Aspect.MIND, 20));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ALEMBIC.get()), new AspectList().add(Aspect.METAL, 15).add(Aspect.ALCHEMY, 10).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.CENTRIFUGE.get()), new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.ALCHEMY, 10).add(Aspect.MOTION, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.TUBE_NORMAL.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.VOID, 3));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.SMELTER.get()), new AspectList().add(Aspect.FIRE, 15).add(Aspect.METAL, 10).add(Aspect.ALCHEMY, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.HUNGRY_CHEST.get()), new AspectList().add(Aspect.VOID, 20).add(Aspect.DESIRE, 10).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.ARCANE_EAR.get()), new AspectList().add(Aspect.SENSES, 20).add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BELLOWS.get()), new AspectList().add(Aspect.AIR, 10).add(Aspect.MECHANISM, 5).add(Aspect.BEAST, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLUSTER_IRON.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.METAL, 15).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLUSTER_GOLD.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.METAL, 15).add(Aspect.EARTH, 5).add(Aspect.DESIRE, 10));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLUSTER_COPPER.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.METAL, 15).add(Aspect.EARTH, 5).add(Aspect.EXCHANGE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLUSTER_CINNABAR.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.METAL, 15).add(Aspect.EARTH, 5).add(Aspect.ALCHEMY, 5).add(Aspect.DEATH, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.CLUSTER_QUARTZ.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.CRYSTAL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.ENCHANTED_FABRIC.get()), new AspectList().add(Aspect.CRAFT, 5).add(Aspect.MAGIC, 5).add(Aspect.BEAST, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.MIRRORED_GLASS.get()), new AspectList().add(Aspect.CRYSTAL, 5).add(Aspect.MOTION, 5).add(Aspect.VOID, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VOID_SEED.get()), new AspectList().add(Aspect.VOID, 10).add(Aspect.ELDRITCH, 5).add(Aspect.LIFE, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.FILTER.get()), new AspectList().add(Aspect.ORDER, 5).add(Aspect.VOID, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.MORPHIC_RESONATOR.get()), new AspectList().add(Aspect.AURA, 5).add(Aspect.EXCHANGE, 5).add(Aspect.MECHANISM, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.VIS_RESONATOR.get()), new AspectList().add(Aspect.AURA, 10).add(Aspect.CRYSTAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.JAR_BRACE.get()), new AspectList().add(Aspect.METAL, 5).add(Aspect.ORDER, 3));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.MECHANISM_SIMPLE.get()), new AspectList().add(Aspect.MECHANISM, 10).add(Aspect.METAL, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.MECHANISM_COMPLEX.get()), new AspectList().add(Aspect.MECHANISM, 20).add(Aspect.METAL, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.BRAIN_CLOCKWORK.get()), new AspectList().add(Aspect.MECHANISM, 15).add(Aspect.MIND, 20).add(Aspect.ORDER, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModItems.BRAIN_CURIOUS.get()), new AspectList().add(Aspect.LIFE, 5).add(Aspect.MIND, 25).add(Aspect.SENSES, 10));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_CRATE_COMMON.get()), new AspectList().add(Aspect.DESIRE, 10).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_CRATE_UNCOMMON.get()), new AspectList().add(Aspect.DESIRE, 20).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_CRATE_RARE.get()), new AspectList().add(Aspect.DESIRE, 30).add(Aspect.PLANT, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_URN_COMMON.get()), new AspectList().add(Aspect.DESIRE, 10).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_URN_UNCOMMON.get()), new AspectList().add(Aspect.DESIRE, 20).add(Aspect.EARTH, 5));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.LOOT_URN_RARE.get()), new AspectList().add(Aspect.DESIRE, 30).add(Aspect.EARTH, 5));

        ArcaneresearchApi.registerObjectTag(new ItemStack(ModFluids.LIQUID_DEATH_BUCKET.get()), new AspectList(new ItemStack(Items.BUCKET)).add(Aspect.DEATH, 15).add(Aspect.ENTROPY, 15));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.PURIFYING_FLUID.get()), new AspectList().add(Aspect.LIFE, 20).add(Aspect.WATER, 10).add(Aspect.MAGIC, 10).add(Aspect.ORDER, 5));

        AspectList bannerAspects = new AspectList().add(Aspect.CRAFT, 5).add(Aspect.SENSES, 5).add(Aspect.BEAST, 3);
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_WHITE.get()), bannerAspects.copy().add(Aspect.LIGHT, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_ORANGE.get()), bannerAspects.copy().add(Aspect.FIRE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_MAGENTA.get()), bannerAspects.copy().add(Aspect.MAGIC, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_LIGHT_BLUE.get()), bannerAspects.copy().add(Aspect.AIR, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_YELLOW.get()), bannerAspects.copy().add(Aspect.LIGHT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_LIME.get()), bannerAspects.copy().add(Aspect.PLANT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_PINK.get()), bannerAspects.copy().add(Aspect.LIFE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_GRAY.get()), bannerAspects.copy().add(Aspect.ENTROPY, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_LIGHT_GRAY.get()), bannerAspects.copy().add(Aspect.ORDER, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_CYAN.get()), bannerAspects.copy().add(Aspect.WATER, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_PURPLE.get()), bannerAspects.copy().add(Aspect.MAGIC, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_BLUE.get()), bannerAspects.copy().add(Aspect.WATER, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_BROWN.get()), bannerAspects.copy().add(Aspect.EARTH, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_GREEN.get()), bannerAspects.copy().add(Aspect.PLANT, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_RED.get()), bannerAspects.copy().add(Aspect.FIRE, 1));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_BLACK.get()), bannerAspects.copy().add(Aspect.DARKNESS, 2));
        ArcaneresearchApi.registerObjectTag(new ItemStack(ModBlocks.BANNER_CRIMSON_CULT.get()), new AspectList().add(Aspect.CRAFT, 5).add(Aspect.SENSES, 5).add(Aspect.ELDRITCH, 10).add(Aspect.DARKNESS, 5));
    }
}