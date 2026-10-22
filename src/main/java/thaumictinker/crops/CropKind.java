package thaumictinker.crops;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

import arcaneresearch.api.aspects.Aspect;

 


 
public enum CropKind implements StringRepresentable {
    AER("aer"),
    IGNIS("ignis"),
    AQUA("aqua"),
    TERRA("terra"),
    ORDO("ordo"),
    PERDITIO("perditio"),
    GENERIC("generic");

    private final String name;

    CropKind(String name) {
        this.name = name;
    }

    public static CropKind of(Aspect aspect) {
        if (aspect == Aspect.AIR) {
            return AER;
        }
        if (aspect == Aspect.FIRE) {
            return IGNIS;
        }
        if (aspect == Aspect.WATER) {
            return AQUA;
        }
        if (aspect == Aspect.EARTH) {
            return TERRA;
        }
        if (aspect == Aspect.ORDER) {
            return ORDO;
        }
        if (aspect == Aspect.ENTROPY) {
            return PERDITIO;
        }
        return GENERIC;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name.toLowerCase(Locale.ROOT);
    }
}
