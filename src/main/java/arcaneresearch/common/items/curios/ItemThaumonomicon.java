package arcaneresearch.common.items.curios;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.client.gui.screens.ResearchBrowserScreen;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.items.ItemTC;
import arcaneresearch.common.lib.CommandArcaneresearch;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModSounds;
import javax.annotation.Nullable;
import java.util.List;
public class ItemThaumonomicon extends ItemTC {
    protected final boolean cheat;
    public ItemThaumonomicon() {
        this(false);
    }
    public ItemThaumonomicon(boolean cheat) {
        super(new Properties()
                .stacksTo(1)
                .rarity(cheat ? Rarity.EPIC : Rarity.UNCOMMON));
        this.cheat = cheat;
    }
    public boolean isCheat() {
        return cheat;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (cheat && ModConfig.allowCheatSheet) {
                for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                    for (ResearchEntry ri : cat.research.values()) {
                        CommandArcaneresearch.giveRecursiveResearch(serverPlayer, ri.getKey());
                    }
                }
            } else {
                for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                    for (ResearchEntry ri : cat.research.values()) {
                        if (ArcaneresearchCapabilities.knowsResearch(player, ri.getKey())
                                && ri.getSiblings() != null) {
                            for (String sib : ri.getSiblings()) {
                                if (!ArcaneresearchCapabilities.knowsResearch(player, sib)) {
                                    ResearchManager.completeResearch(player, sib);
                                }
                            }
                        }
                    }
                }
            }
            IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {
                knowledge.sync(serverPlayer);
            }
        }
        if (level.isClientSide) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    ModSounds.PAGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f, false);
            openThaumonomiconGui();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @OnlyIn(Dist.CLIENT)
    private void openThaumonomiconGui() {
        Minecraft.getInstance().setScreen(new ResearchBrowserScreen());
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (cheat) {
            tooltip.add(Component.literal("Creative only")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(cheat
                ? "item.arcaneresearch.thaumonomicon.cheat"
                : "item.arcaneresearch.thaumonomicon");
    }
}
