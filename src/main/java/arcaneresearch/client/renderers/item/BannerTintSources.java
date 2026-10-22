package arcaneresearch.client.renderers.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.basic.BlockBannerTC;
import org.jspecify.annotations.Nullable;











@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class BannerTintSources {

    public static final Identifier BANNER_DYE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "banner_dye");
    public static final Identifier BANNER_ASPECT_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "banner_aspect");

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(BANNER_DYE_ID, DyeTint.MAP_CODEC);
        event.register(BANNER_ASPECT_ID, AspectTint.MAP_CODEC);
    }

    static int dyeColor(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof BlockBannerTC banner) {
            DyeColor dye = banner.getDyeColor();
            return dye != null ? dye.getFireworkColor() : MapColor.COLOR_RED.col;
        }
        return 0xFFFFFF;
    }

    public record DyeTint() implements ItemTintSource {
        public static final MapCodec<DyeTint> MAP_CODEC = MapCodec.unit(new DyeTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return 0xFF000000 | dyeColor(stack);
        }

        @Override
        public MapCodec<DyeTint> type() {
            return MAP_CODEC;
        }
    }

    public record AspectTint() implements ItemTintSource {
        public static final MapCodec<AspectTint> MAP_CODEC = MapCodec.unit(new AspectTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            net.minecraft.nbt.CompoundTag tag =
                    arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
            if (tag != null && tag.contains("aspect")) {
                Aspect aspect = Aspect.getAspect(tag.getStringOr("aspect", ""));
                if (aspect != null) {
                    return 0xFF000000 | aspect.getColor();
                }
            }
            return 0xFF000000 | dyeColor(stack);
        }

        @Override
        public MapCodec<AspectTint> type() {
            return MAP_CODEC;
        }
    }
}
