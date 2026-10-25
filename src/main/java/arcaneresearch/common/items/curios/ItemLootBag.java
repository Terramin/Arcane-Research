package arcaneresearch.common.items.curios;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class ItemLootBag extends Item {
    private final LootTier tier;
    public ItemLootBag(LootTier tier) {
        super(new Item.Properties()
                .stacksTo(16)
                .rarity(tier.getRarity()));
        this.tier = tier;
    }
    public static ItemLootBag createCommon() {
        return new ItemLootBag(LootTier.COMMON);
    }
    public static ItemLootBag createUncommon() {
        return new ItemLootBag(LootTier.UNCOMMON);
    }
    public static ItemLootBag createRare() {
        return new ItemLootBag(LootTier.RARE);
    }
    public LootTier getTier() {
        return tier;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int itemCount = 8 + level.random.nextInt(5);
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
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    private ItemStack generateLoot(Level level, LootTier tier) {
        return arcaneresearch.common.lib.utils.Utils.generateLoot(tier.ordinal(), level.random);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tc.lootbag").withStyle(style -> style.withColor(0x808080)));
        super.appendHoverText(stack, level, tooltip, flag);
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
