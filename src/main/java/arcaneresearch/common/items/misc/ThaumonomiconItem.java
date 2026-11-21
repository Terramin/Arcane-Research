package arcaneresearch.common.items.misc;

import net.minecraft.world.InteractionResult;

import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.items.ItemTC;
import arcaneresearch.common.lib.CommandArcaneresearch;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ThaumonomiconItem extends ItemTC {

    protected final boolean cheat;
    protected final boolean creative;

    public ThaumonomiconItem(Item.Properties props) {
        this(props, false);
    }

    public ThaumonomiconItem(Item.Properties props, boolean cheat) {
        this(props, cheat, false);
    }

    public ThaumonomiconItem(Item.Properties props, boolean cheat, boolean creative) {
        super(props
                .stacksTo(1)
                .rarity(cheat || creative ? Rarity.EPIC : Rarity.UNCOMMON));
        this.cheat = cheat;
        this.creative = creative;
    }

    public boolean isCheat() {
        return cheat;
    }

    public boolean isCreative() {
        return creative;
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            SoundEvent page = ModSounds.PAGE.get();
            level.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    page,
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f,
                    false
            );
            openThaumonomiconGui();
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            var categories = ResearchCategories.researchCategories;

            if (categories != null) {
                if (creative) {
                    for (ResearchCategory cat : categories.values()) {
                        if (cat == null || cat.research == null) {
                            continue;
                        }
                        for (ResearchEntry ri : cat.research.values()) {
                            if (ri != null && ri.getKey() != null) {
                                CommandArcaneresearch.giveRecursiveResearch(serverPlayer, ri.getKey());
                            }
                        }
                    }
                } else if (cheat && ModConfig.allowCheatSheet) {
                    for (ResearchCategory cat : categories.values()) {
                        if (cat == null || cat.research == null) {
                            continue;
                        }

                        for (ResearchEntry ri : cat.research.values()) {
                            if (ri == null) {
                                continue;
                            }

                            String key = ri.getKey();
                            if (key != null) {
                                CommandArcaneresearch.giveRecursiveResearch(serverPlayer, key);
                            }
                        }
                    }
                } else {
                    for (ResearchCategory cat : categories.values()) {
                        if (cat == null || cat.research == null) {
                            continue;
                        }

                        for (ResearchEntry ri : cat.research.values()) {
                            if (ri == null) {
                                continue;
                            }

                            String key = ri.getKey();
                            if (key == null || !ArcaneresearchCapabilities.knowsResearch(player, key)) {
                                continue;
                            }

                            String[] siblings = ri.getSiblings();
                            if (siblings == null) {
                                continue;
                            }

                            for (String sib : siblings) {
                                if (sib != null && !ArcaneresearchCapabilities.knowsResearch(player, sib)) {
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

        return InteractionResult.SUCCESS;
    }
    private void openThaumonomiconGui() {
        // Kept reflective so client-only classes never enter this class's constant pool
        // (a dedicated server would fail to verify them).
        try {
            Class.forName("arcaneresearch.client.gui.ThaumonomiconGuiOpener")
                    .getMethod("open").invoke(null);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        if (cheat || creative) {
            tooltip.accept(Component.literal("Creative only")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return Component.translatable(creative
                ? "item.arcaneresearch.thaumonomicon_creative"
                : cheat
                ? "item.arcaneresearch.thaumonomicon.cheat"
                : "item.arcaneresearch.thaumonomicon");
    }
}