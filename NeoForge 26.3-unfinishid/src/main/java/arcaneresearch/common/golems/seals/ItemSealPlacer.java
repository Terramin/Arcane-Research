package arcaneresearch.common.golems.seals;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.seals.ISeal;
import arcaneresearch.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSealPlacer extends Item implements ISealDisplayer {

    private final String sealKey;

    public ItemSealPlacer(Item.Properties props, String sealKey) {
        super(props
                .stacksTo(64));
        this.sealKey = sealKey;
    }

    public static ItemSealPlacer createBlank(Item.Properties props) {
        return new ItemSealPlacer(props, "blank");
    }

    public static ItemSealPlacer create(Item.Properties props, String sealKey) {
        return new ItemSealPlacer(props, sealKey);
    }

    public static ItemStack getSealStack(String sealKey) {
        return switch (sealKey) {
            case "arcaneresearch:pickup" -> new ItemStack(ModItems.SEAL_PICKUP.get());
            case "arcaneresearch:pickup_advanced" -> new ItemStack(ModItems.SEAL_PICKUP_ADVANCED.get());
            case "arcaneresearch:empty" -> new ItemStack(ModItems.SEAL_EMPTY.get());
            case "arcaneresearch:empty_advanced" -> new ItemStack(ModItems.SEAL_EMPTY_ADVANCED.get());
            case "arcaneresearch:fill" -> new ItemStack(ModItems.SEAL_FILL.get());
            case "arcaneresearch:fill_advanced" -> new ItemStack(ModItems.SEAL_FILL_ADVANCED.get());
            case "arcaneresearch:guard" -> new ItemStack(ModItems.SEAL_GUARD.get());
            case "arcaneresearch:guard_advanced" -> new ItemStack(ModItems.SEAL_GUARD_ADVANCED.get());
            case "arcaneresearch:butcher" -> new ItemStack(ModItems.SEAL_BUTCHER.get());
            case "arcaneresearch:harvest" -> new ItemStack(ModItems.SEAL_HARVEST.get());
            case "arcaneresearch:lumber" -> new ItemStack(ModItems.SEAL_LUMBER.get());
            case "arcaneresearch:breaker" -> new ItemStack(ModItems.SEAL_BREAKER.get());
            case "arcaneresearch:breaker_advanced" -> new ItemStack(ModItems.SEAL_BREAKER_ADVANCED.get());
            case "arcaneresearch:provider" -> new ItemStack(ModItems.SEAL_PROVIDE.get());
            case "arcaneresearch:stock" -> new ItemStack(ModItems.SEAL_STOCK.get());
            case "arcaneresearch:use" -> new ItemStack(ModItems.SEAL_USE.get());
            case "blank" -> new ItemStack(ModItems.SEAL_BLANK.get());
            default -> ItemStack.EMPTY;
        };
    }

    public String getSealKey() {
        return sealKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.FAIL;
        }

        if (sealKey.equals("blank")) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

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
        } catch (Exception e) {
            e.printStackTrace();
            return InteractionResult.FAIL;
        }

        if (SealHandler.addSealEntity(level, pos, side, seal, player)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide() || sealKey.equals("blank") || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (!player.mayUseItemAt(pos, side, stack)) {
            return InteractionResult.FAIL;
        }
        ISeal sealTemplate = SealHandler.getSeal(sealKey);
        if (sealTemplate == null) {
            return InteractionResult.FAIL;
        }
        ISeal seal;
        try {
            seal = sealTemplate.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return InteractionResult.FAIL;
        }
        if (seal == null || !seal.canPlaceAt(level, pos, side)) {
            return InteractionResult.FAIL;
        }
        if (SealHandler.addSealEntity(level, pos, side, seal, player)
                && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    // NEOFORGE_PORT: Forge override only forwarded to super (no custom lines);
    // dead override deleted (Item default behaves identically).

    @Override
    public Component getName(ItemStack stack) {
        if (sealKey.equals("blank")) {
            return Component.translatable("item.arcaneresearch.seal.blank");
        }
        String shortKey = sealKey.contains(":") ? sealKey.substring(sealKey.indexOf(':') + 1) : sealKey;
        return Component.translatable("item.arcaneresearch.seal." + shortKey);
    }
}
