package arcaneresearch.client.renderers.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.items.casters.ItemFocus;
import org.jspecify.annotations.Nullable;





@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class FocusGolemTintSources {

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus"), FocusTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "golem_material"), GolemMaterialTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "caster_focus_color"), CasterFocusTint.MAP_CODEC);
    }

    
    
    
    public record CasterFocusTint() implements ItemTintSource {
        public static final MapCodec<CasterFocusTint> MAP_CODEC = MapCodec.unit(new CasterFocusTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            int color = arcaneresearch.helper.TintHelper.casterFocusColor(stack, 1);
            return color == arcaneresearch.helper.TintHelper.NONE ? 0xFFFFFFFF : 0xFF000000 | color;
        }

        @Override
        public MapCodec<CasterFocusTint> type() {
            return MAP_CODEC;
        }
    }

    public record FocusTint() implements ItemTintSource {
        public static final MapCodec<FocusTint> MAP_CODEC = MapCodec.unit(new FocusTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.getItem() instanceof ItemFocus focus) {
                try {
                    return 0xFF000000 | focus.getFocusColor(stack);
                } catch (Exception ignored) {
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<FocusTint> type() {
            return MAP_CODEC;
        }
    }

    public record GolemMaterialTint() implements ItemTintSource {
        public static final MapCodec<GolemMaterialTint> MAP_CODEC = MapCodec.unit(new GolemMaterialTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            CompoundTag tag = ArcaneresearchInvHelper.getCustomTag(stack);
            if (tag != null && tag.contains("props")) {
                try {
                    IGolemProperties props = GolemProperties.fromLong(tag.getLongOr("props", 0L));
                    if (props != null && props.getMaterial() != null) {
                        return 0xFF000000 | props.getMaterial().itemColor;
                    }
                } catch (Exception ignored) {
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<GolemMaterialTint> type() {
            return MAP_CODEC;
        }
    }
}
