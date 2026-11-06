package arcaneresearch.common.golems.seals;

import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSealPlacer extends Item implements ISealDisplayer {

    private static final Logger LOGGER = LogManager.getLogger(ItemSealPlacer.class);
    private static final String SEAL_KEY_BLANK = "blank";

    private final String sealKey;
    private final boolean isBlank;

    public ItemSealPlacer(String sealKey) {
        super(new Item.Properties().stacksTo(64));
        this.sealKey = sealKey;
        this.isBlank = SEAL_KEY_BLANK.equals(sealKey);
    }

    public static ItemSealPlacer createBlank() {
        return new ItemSealPlacer(SEAL_KEY_BLANK);
    }

    public static ItemSealPlacer create(String sealKey) {
        return new ItemSealPlacer(sealKey);
    }

    private static String stripNamespace(String sealKey) {
        int colon = sealKey.indexOf(':');
        return colon >= 0 ? sealKey.substring(colon + 1) : sealKey;
    }

    public static ItemStack getSealStack(String sealKey) {
        String key = stripNamespace(sealKey);
        return switch (key) {
            case "pickup" -> new ItemStack(ModItems.SEAL_PICKUP.get());
            case "pickup_advanced" -> new ItemStack(ModItems.SEAL_PICKUP_ADVANCED.get());
            case "empty" -> new ItemStack(ModItems.SEAL_EMPTY.get());
            case "empty_advanced" -> new ItemStack(ModItems.SEAL_EMPTY_ADVANCED.get());
            case "fill" -> new ItemStack(ModItems.SEAL_FILL.get());
            case "fill_advanced" -> new ItemStack(ModItems.SEAL_FILL_ADVANCED.get());
            case "guard" -> new ItemStack(ModItems.SEAL_GUARD.get());
            case "guard_advanced" -> new ItemStack(ModItems.SEAL_GUARD_ADVANCED.get());
            case "butcher" -> new ItemStack(ModItems.SEAL_BUTCHER.get());
            case "harvest" -> new ItemStack(ModItems.SEAL_HARVEST.get());
            case "lumber" -> new ItemStack(ModItems.SEAL_LUMBER.get());
            case "breaker" -> new ItemStack(ModItems.SEAL_BREAKER.get());
            case "breaker_advanced" -> new ItemStack(ModItems.SEAL_BREAKER_ADVANCED.get());
            case "provider" -> new ItemStack(ModItems.SEAL_PROVIDE.get());
            case "stock" -> new ItemStack(ModItems.SEAL_STOCK.get());
            case "use" -> new ItemStack(ModItems.SEAL_USE.get());
            case SEAL_KEY_BLANK -> new ItemStack(ModItems.SEAL_BLANK.get());
            default -> ItemStack.EMPTY;
        };
    }

    public String getSealKey() {
        return sealKey;
    }

    private InteractionResult tryPlaceSeal(Level level, BlockPos pos, Direction side, Player player, ItemStack stack) {
        if (!player.mayUseItemAt(pos, side, stack)) {
            return InteractionResult.FAIL;
        }
        ISeal sealTemplate = SealHandler.getSeal(sealKey);
        if (sealTemplate == null) {
            return InteractionResult.FAIL;
        }
        if (!sealTemplate.canPlaceAt(level, pos, side)) {
            return InteractionResult.FAIL;
        }
        ISeal seal;
        try {
            seal = sealTemplate.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to instantiate seal '{}'", sealKey, e);
            return InteractionResult.FAIL;
        }
        if (!SealHandler.addSealEntity(level, pos, side, seal, player)) {
            return InteractionResult.FAIL;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (isBlank) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        return tryPlaceSeal(level, context.getClickedPos(), context.getClickedFace(),
                player, context.getItemInHand());
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (isBlank || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        return tryPlaceSeal(level, context.getClickedPos(), context.getClickedFace(), player, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (isBlank) {
            return Component.translatable("item.arcaneresearch.seal.blank");
        }
        return Component.translatable("item.arcaneresearch.seal." + stripNamespace(sealKey));
    }
}