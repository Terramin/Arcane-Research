package arcaneresearch.common.items.misc;

import arcaneresearch.common.lib.utils.Utils;
import arcaneresearch.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
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

import java.util.Objects;
import java.util.function.Consumer;

public class LootBagItem extends Item {

    private final LootTier tier;

    public LootBagItem(Item.Properties props, LootTier tier) {
        super(props
                .stacksTo(16)
                .rarity(Objects.requireNonNull(tier, "tier").getRarity()));
        this.tier = tier;
    }

    public static LootBagItem createCommon(Item.Properties props) {
        return new LootBagItem(props, LootTier.COMMON);
    }

    public static LootBagItem createUncommon(Item.Properties props) {
        return new LootBagItem(props, LootTier.UNCOMMON);
    }

    public static LootBagItem createRare(Item.Properties props) {
        return new LootBagItem(props, LootTier.RARE);
    }

    public LootTier getTier() {
        return tier;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide();

        if (!clientSide) {
            RandomSource random = level.getRandom();
            int itemCount = 8 + random.nextInt(5);

            for (int i = 0; i < itemCount; i++) {
                ItemStack loot = generateLoot(level, tier);
                if (loot != null && !loot.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), loot.copy());
                    level.addFreshEntity(itemEntity);
                }
            }

            SoundEvent coins = ModSounds.COINS.get();
            player.playSound(coins, 0.5f, 1.0f);
        }

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private ItemStack generateLoot(Level level, LootTier tier) {
        return Utils.generateLoot(tier.ordinal(), level.getRandom(), level.registryAccess());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
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
