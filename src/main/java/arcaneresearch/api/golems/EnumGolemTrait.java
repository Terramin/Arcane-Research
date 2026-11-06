package arcaneresearch.api.golems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
public enum EnumGolemTrait {
    SMART(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_smart.png")),
    DEFT(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_deft.png")),
    CLUMSY(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_clumsy.png")),
    FIGHTER(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fighter.png")),
    WHEELED(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_wheeled.png")),
    FLYER(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_flyer.png")),
    CLIMBER(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_climber.png")),
    HEAVY(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_heavy.png")),
    LIGHT(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_light.png")),
    FRAGILE(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fragile.png")),
    REPAIR(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_repair.png")),
    SCOUT(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_scout.png")),
    ARMORED(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_armored.png")),
    BRUTAL(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_brutal.png")),
    FIREPROOF(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fireproof.png")),
    BREAKER(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_breaker.png")),
    HAULER(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_hauler.png")),
    RANGED(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_ranged.png")),
    BLASTPROOF(ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_blastproof.png"));
    static {
        CLUMSY.opposite = DEFT;
        DEFT.opposite = CLUMSY;
        HEAVY.opposite = LIGHT;
        LIGHT.opposite = HEAVY;
        FRAGILE.opposite = ARMORED;
        ARMORED.opposite = FRAGILE;
    }
    public ResourceLocation icon;
    public EnumGolemTrait opposite;
    EnumGolemTrait(ResourceLocation icon) {
        this.icon = icon;
    }
    public Component getLocalizedName() {
        return Component.translatable("golem.trait." + name().toLowerCase());
    }
    public Component getLocalizedDescription() {
        return Component.translatable("golem.trait.text." + name().toLowerCase());
    }
}
