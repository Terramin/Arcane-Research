package thaumictinker.foci;

import net.minecraft.resources.ResourceLocation;

import arcaneresearch.api.wands.ItemFocusBasic;

 




 
public abstract class TtFocusBase extends ItemFocusBasic {

    protected TtFocusBase(String texture, int renderColor) {
        super(texture, new ResourceLocation("thaumictinker", "item/" + texture), renderColor);
    }
}
