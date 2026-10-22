package thaumictinker.client.gateway;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import thaumictinker.kami.gateway.PearlLocation;
import thaumictinker.kami.gateway.SkyPearlItem;

 


 
public final class PearlInfo {

    private PearlInfo() {
    }

    public static void add(ItemStack stack, ResourceKey<Level> dimension, Vec3 position,
                           List<Component> lines, boolean simple) {
        PearlLocation location = SkyPearlItem.location(stack);
        if (location == null) {
            return;
        }
        BlockPos pos = location.pos();
        lines.add(Component.literal("X: " + pos.getX()));
        if (simple) {
            lines.add(Component.literal("Dist: "
                    + Math.round(Math.sqrt(pos.distSqr(BlockPos.containing(position))))));
        } else {
            lines.add(Component.literal("Y: " + pos.getY()));
            lines.add(Component.literal("Z: " + pos.getZ()));
        }
        if (!dimension.equals(location.dimension())) {
            lines.add(Component.translatable("ttinkerer.ttmisc.differentDim")
                    .withStyle(ChatFormatting.RED));
        }
    }
}
