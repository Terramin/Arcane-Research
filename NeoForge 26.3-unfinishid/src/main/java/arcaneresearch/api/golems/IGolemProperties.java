package arcaneresearch.api.golems;

import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.golems.parts.GolemAddon;
import arcaneresearch.api.golems.parts.GolemArm;
import arcaneresearch.api.golems.parts.GolemHead;
import arcaneresearch.api.golems.parts.GolemLeg;
import arcaneresearch.api.golems.parts.GolemMaterial;

import java.util.Set;

public interface IGolemProperties {

    Set<EnumGolemTrait> getTraits();

    boolean hasTrait(EnumGolemTrait trait);

    long toLong();

    ItemStack[] generateComponents();

    void setMaterial(GolemMaterial mat);
    GolemMaterial getMaterial();

    void setHead(GolemHead head);
    GolemHead getHead();

    void setArms(GolemArm arms);
    GolemArm getArms();

    void setLegs(GolemLeg legs);
    GolemLeg getLegs();

    void setAddon(GolemAddon addon);
    GolemAddon getAddon();

    void setRank(int rank);
    int getRank();
}
