package arcaneresearch.api.golems;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public enum EnumGolemTrait {
    SMART(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_smart.png")),
    DEFT(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_deft.png")),
    CLUMSY(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_clumsy.png")),
    FIGHTER(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fighter.png")),
    WHEELED(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_wheeled.png")),
    FLYER(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_flyer.png")),
    CLIMBER(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_climber.png")),
    HEAVY(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_heavy.png")),
    LIGHT(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_light.png")),
    FRAGILE(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fragile.png")),
    REPAIR(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_repair.png")),
    SCOUT(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_scout.png")),
    ARMORED(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_armored.png")),
    BRUTAL(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_brutal.png")),
    FIREPROOF(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_fireproof.png")),
    BREAKER(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_breaker.png")),
    HAULER(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_hauler.png")),
    RANGED(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_ranged.png")),
    BLASTPROOF(Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/golem/tag_blastproof.png"));

    static {
        CLUMSY.opposite = DEFT;
        DEFT.opposite = CLUMSY;

        HEAVY.opposite = LIGHT;
        LIGHT.opposite = HEAVY;

        FRAGILE.opposite = ARMORED;
        ARMORED.opposite = FRAGILE;
    }

    public Identifier icon;
    public EnumGolemTrait opposite;

    EnumGolemTrait(Identifier icon) {
        this.icon = icon;
    }

    public Component getLocalizedName() {
        return Component.translatable("golem.trait." + name().toLowerCase());
    }

    public Component getLocalizedDescription() {
        return Component.translatable("golem.trait.text." + name().toLowerCase());
    }
}
