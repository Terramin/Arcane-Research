package arcaneresearch.common.items.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.init.ModSounds;

import java.util.function.Consumer;

public class ItemLootBag extends Item {

    private final LootTier tier;

    public ItemLootBag(Item.Properties props, LootTier tier) {
        super(props
                .stacksTo(16)
                .rarity(tier.getRarity()));
        this.tier = tier;
    }

    public static ItemLootBag createCommon(Item.Properties props) {
        return new ItemLootBag(props, LootTier.COMMON);
    }

    public static ItemLootBag createUncommon(Item.Properties props) {
        return new ItemLootBag(props, LootTier.UNCOMMON);
    }

    public static ItemLootBag createRare(Item.Properties props) {
        return new ItemLootBag(props, LootTier.RARE);
    }

    public LootTier getTier() {
        return tier;
    }

    // NEOFORGE_PORT: use() now returns InteractionResult (holder removed);
    // InteractionResultHolder.sidedSuccess(stack, client) -> InteractionResult.SUCCESS.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {

            int itemCount = 8 + level.getRandom().nextInt(5);

            for (int i = 0; i < itemCount; i++) {
                ItemStack loot = generateLoot(level, tier);
                if (loot != null && !loot.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level,
                            player.getX(), player.getY(), player.getZ(),
                            loot.copy());
                    level.addFreshEntity(itemEntity);
                }
            }

            player.playSound(ModSounds.COINS.get(), 0.5f, 1.0f);
        }

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private ItemStack generateLoot(Level level, LootTier tier) {
        return arcaneresearch.common.lib.utils.Utils.generateLoot(tier.ordinal(), level.getRandom(), level.registryAccess());
    }

    // NEOFORGE_PORT: OnlyIn/Dist moved to net.neoforged.api.distmarker; appendHoverText signature
    // changed in 26.x (ItemStack, Level, List) -> (ItemStack, TooltipContext, TooltipDisplay,
    // Consumer, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tc.lootbag").withStyle(style -> style.withColor(0x808080)));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    public enum LootTier {
        COMMON(Rarity.COMMON),
        UNCOMMON(Rarity.UNCOMMON),
        RARE(Rarity.RARE);

        private final Rarity rarity;

        LootTier(Rarity rarity) {
            this.rarity = rarity;
        }

        public Rarity getRarity() {
            return rarity;
        }
    }
}