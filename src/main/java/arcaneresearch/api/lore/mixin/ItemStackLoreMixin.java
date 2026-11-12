package arcaneresearch.api.lore.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import arcaneresearch.api.lore.LoreAPI;
import javax.annotation.Nullable;

/**
 * Вставляет лор из {@link LoreAPI} в тултип предмета сразу под его именем.
 *
 * <p>Инжект в хвост {@link ItemStack#getTooltipLines}: список строк уже собран,
 * лор добавляется на позицию 1 (строка 0 — имя предмета). Миксин находится
 * в секции {@code client} конфига, поэтому клиентские классы
 * ({@link Screen}) доступны напрямую.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackLoreMixin {

    @Inject(
            method = "getTooltipLines(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
            at = @At("TAIL")
    )
    private void arcaneresearch$loreUnderName(@Nullable Player player, TooltipFlag flag,
                                              CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (tooltip.isEmpty()) {
            return;
        }
        String loreKey = LoreAPI.getLoreKey((ItemStack) (Object) this);
        if (loreKey == null) {
            return;
        }
        if (Screen.hasShiftDown()) {
            tooltip.add(1, Component.translatable(loreKey).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(1, Component.translatable(LoreAPI.HOLD_SHIFT_KEY)
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
