package arcaneresearch.helper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

 










 
@OnlyIn(Dist.CLIENT)
public final class TintHelper {

      
    public static final int WHITE = 0xFFFFFF;

     


 
    public static final int NONE = -1;

    private TintHelper() {
    }

      
    public static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

      
    public static int phialAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.consumables.ItemPhial) {
            try {
                AspectList aspects = ((arcaneresearch.common.items.consumables.ItemPhial)
                        stack.getItem()).getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int jarAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.blocks.essentia.BlockJarItem jarItem) {
            try {
                AspectList aspects = jarItem.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int crystalEssenceAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.resources.ItemCrystalEssence crystal) {
            try {
                AspectList aspects = crystal.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int labelAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.consumables.ItemLabel label) {
            try {
                Aspect aspect = label.getAspect(stack);
                if (aspect != null) {
                    return aspect.getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int candleMapColor(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && blockItem.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockCandle candle) {
            try {
                return candle.getColor().getMapColor().col;
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

     


 
    public static int wispEssenceAspect(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.resources.ItemWispEssence) {
            try {
                AspectList aspects =
                        ((arcaneresearch.common.items.resources.ItemWispEssence) stack.getItem())
                                .getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    return aspects.getAspects()[0].getColor();
                }
            } catch (Exception ignored) {
            }
            try {
                Object[] all = Aspect.aspects.values().toArray();
                if (all.length > 0) {
                    int idx = (int) (System.currentTimeMillis() / 500L % all.length);
                    return ((Aspect) all[idx]).getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

     


 
    public static int bannerColor(ItemStack stack, int tintIndex) {
        net.minecraft.world.item.DyeColor dye = null;
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem
                && blockItem.getBlock() instanceof arcaneresearch.common.blocks.basic.BlockBannerTC banner) {
            try {
                dye = banner.getDyeColor();
            } catch (Exception ignored) {
            }
        }
        int dyeColor = dye != null ? dye.getFireworkColor()
                : net.minecraft.world.level.material.MapColor.COLOR_RED.col;
        if (tintIndex == 1) {
            return dyeColor;
        }
        if (tintIndex == 2) {
            
            
            net.minecraft.nbt.CompoundTag tag = stack != null
                    ? arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack) : null;
            if (tag != null && tag.contains("aspect")) {
                try {
                    Aspect aspect = Aspect.getAspect(tag.getStringOr("aspect", ""));
                    if (aspect != null) {
                        return aspect.getColor();
                    }
                } catch (Exception ignored) {
                }
            }
            return dyeColor;
        }
        return WHITE;
    }

      
    public static int casterFocusColor(ItemStack stack, int tintIndex) {
        if (tintIndex > 0 && stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.casters.ItemCaster caster) {
            try {
                ItemStack focusStack = caster.getFocusStack(stack);
                if (caster.getFocus(stack) instanceof arcaneresearch.common.items.casters.ItemFocus focus
                        && focusStack != null) {
                    return focus.getFocusColor(focusStack);
                }
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

      
    public static int focusColor(ItemStack stack) {
        if (stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.common.items.casters.ItemFocus focus) {
            try {
                return focus.getFocusColor(stack);
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int wandFocusColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack != null && !stack.isEmpty()
                && stack.getItem() instanceof arcaneresearch.api.wands.IWand wand) {
            try {
                arcaneresearch.api.wands.ItemFocusBasic focus = wand.getFocus(stack);
                if (focus != null) {
                    return focus.getFocusColor(wand.getFocusStack(stack));
                }
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

      
    public static int golemMaterialColor(ItemStack stack) {
        
        
        net.minecraft.nbt.CompoundTag tag = stack != null
                ? arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack) : null;
        if (tag != null && tag.contains("props")) {
            try {
                arcaneresearch.api.golems.IGolemProperties props =
                        arcaneresearch.common.golems.GolemProperties.fromLong(
                                tag.getLongOr("props", 0L));
                if (props != null && props.getMaterial() != null) {
                    return props.getMaterial().itemColor;
                }
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

     


 
    public static int dyeableColor(ItemStack stack, int tintIndex) {
        
        
        if (stack != null && !stack.isEmpty()
                && stack.has(net.minecraft.core.component.DataComponents.DYED_COLOR)) {
            try {
                return tintIndex > 0 ? NONE : net.minecraft.world.item.component.DyedItemColor
                        .getOrDefault(stack, 0xFFFFFF);
            } catch (Exception ignored) {
            }
        }
        return NONE;
    }

      
    public static int nitorBlockColor(net.minecraft.world.level.block.state.BlockState state) {
        if (state != null && state.getBlock()
                instanceof arcaneresearch.common.blocks.misc.BlockNitor nitor) {
            try {
                return nitor.getColorValue();
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

      
    public static int inlayChargeColor(net.minecraft.world.level.block.state.BlockState state, int tintIndex) {
        if (tintIndex == 0 && state != null
                && state.getBlock() instanceof arcaneresearch.common.blocks.devices.BlockInlay
                && state.hasProperty(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE)) {
            try {
                return arcaneresearch.common.blocks.devices.BlockInlay.colorMultiplier(
                        state.getValue(arcaneresearch.common.blocks.devices.BlockInlay.CHARGE));
            } catch (Exception ignored) {
            }
        }
        return WHITE;
    }

     




 
    public static int researchNoteColor(ItemStack stack, int tintIndex,
            arcaneresearch.discovered.note.DiscoveredNoteData data) {
        if (tintIndex != 1 || stack == null
                || !(stack.getItem() instanceof arcaneresearch.discovered.note.ItemDiscoveredResearchNote)) {
            return WHITE;
        }
        if (data == null) {
            return WHITE;
        }
        try {
            for (int i = 0; i < data.tags.length; i++) {
                Aspect aspect = data.tags[i];
                int got = i < data.have.length ? data.have[i] : 0;
                if (aspect != null && got > 0) {
                    return aspect.getColor();
                }
            }
            if (data.tags.length > 0 && data.tags[0] != null) {
                return data.tags[0].getColor();
            }
        } catch (Exception ignored) {
        }
        return WHITE;
    }
}
