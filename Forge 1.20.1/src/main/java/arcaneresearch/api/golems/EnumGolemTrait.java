package arcaneresearch.api.golems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
public enum EnumGolemTrait {
    SMART(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_smart.png")),
    DEFT(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_deft.png")),
    CLUMSY(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_clumsy.png")),
    FIGHTER(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_fighter.png")),
    WHEELED(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_wheeled.png")),
    FLYER(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_flyer.png")),
    CLIMBER(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_climber.png")),
    HEAVY(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_heavy.png")),
    LIGHT(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_light.png")),
    FRAGILE(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_fragile.png")),
    REPAIR(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_repair.png")),
    SCOUT(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_scout.png")),
    ARMORED(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_armored.png")),
    BRUTAL(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_brutal.png")),
    FIREPROOF(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_fireproof.png")),
    BREAKER(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_breaker.png")),
    HAULER(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_hauler.png")),
    RANGED(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_ranged.png")),
    BLASTPROOF(new ResourceLocation("arcaneresearch", "textures/misc/golem/tag_blastproof.png"));
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
