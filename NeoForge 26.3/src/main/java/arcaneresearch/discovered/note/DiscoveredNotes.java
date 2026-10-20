package arcaneresearch.discovered.note;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModSounds;

/**
 * Хелперы записки, порт в паритете с reforged
 * {@code thaumcraft.research.ResearchManager} (createNote/getData/updateData,
 * getResearchSlot, completeResearch) и TC3 {@code ResearchManager}
 * (createResearchNoteForPlayer/getData/updateData).
 *
 * NEOFORGE_PORT: getKnowledge возвращает @Nullable IPlayerKnowledge напрямую
 * (без Optional); Inventory.items приватны -> Container-API;
 * InteractionResultHolder.sidedSuccess удалён -> InteractionResult;
 * копирование NBT -> CUSTOM_DATA.
 */
public final class DiscoveredNotes {

    private DiscoveredNotes() {
    }

    /** Создать стак записки под ключ с требуемыми аспектами (паритет createNote). */
    public static ItemStack createNote(String key, AspectList required) {
        ItemStack stack = new ItemStack(DiscoveredNoteSetup.getNoteItem());
        DiscoveredNoteData.write(stack, DiscoveredNoteData.create(key, required));
        return stack;
    }

    public static boolean isNoteFor(ItemStack stack, String key) {
        return key != null && key.equals(DiscoveredNoteData.read(stack).key)
                && stack.getItem() instanceof ItemDiscoveredResearchNote;
    }

    /**
     * Слот инвентаря игрока с запиской под ключ (паритет reforged getResearchSlot).
     * Возвращает индекс в инвентаре или -1.
     *
     * @deprecated стол работает со своим слотом напрямую; никем не вызывается.
     */
    @Deprecated
    public static int findNoteSlot(Player player, String key) {
        if (key == null) {
            return -1;
        }
        // NEOFORGE_PORT: Inventory.items приватны в 26.x -> Container-API.
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isNoteFor(inventory.getItem(slot), key)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Подтверждение условий ПКМ готовой (100%) запиской: НЕ мгновенное
     * завершение, а один gated-шаг как кнопка книги — реквизиты родителей
     * + проверка текущей стадии (предметы/крафты/знания/открытия, со
     * списанием) + {@code progressResearch} один раз. Готовая записка
     * сама засчитывается открытием под свой ключ.
     * Сиблинги каскадит сам {@code progressResearch}.
     */
    public static InteractionResult tryCompleteFromNote(Level level, Player player, ItemStack stack) {
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        if (data.key == null || !data.isComplete()
                || ArcaneresearchCapabilities.isResearchComplete(player, data.key)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            // Готовая записка сама предъявляет своё открытие.
            presentDiscovery(player, data.key);
            boolean ok = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && ResearchManager.doesPlayerHaveRequisites(player, data.key)
                    && arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer
                            .checkRequisites(serverPlayer, data.key)
                    && ResearchManager.progressResearch(player, data.key);
            if (ok) {
                stack.shrink(1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            } else {
                player.sendSystemMessage(Component.translatable("tc.discoveryerror", data.key));
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    /** Локализованное имя исследования или null, если записи нет. */
    public static Component researchName(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null) {
            return entry.getLocalizedName();
        }
        return Component.translatable("tempnote." + key + ".name");
    }

    /**
     * Описание на пергамент: склейка stage-текстов; нет записи в проекте —
     * longText оригинала из lang (tempnote.KEY.text, порт reforged research.xml).
     */
    public static Component researchText(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null && entry.getStages() != null) {
            List<Component> parts = new ArrayList<>();
            for (arcaneresearch.api.research.ResearchStage stage : entry.getStages()) {
                if (stage != null && stage.getText() != null && !stage.getText().isEmpty()) {
                    parts.add(Component.translatable(stage.getText()));
                }
            }
            if (!parts.isEmpty()) {
                net.minecraft.network.chat.MutableComponent out = Component.empty();
                for (int i = 0; i < parts.size(); i++) {
                    if (i > 0) {
                        out.append(" ");
                    }
                    out.append(parts.get(i));
                }
                return out;
            }
        }
        return Component.translatable("tempnote." + key + ".text");
    }

    /**
     * Короткое описание (popupText оригинала из lang).
     * Возвращает null, если перевода нет (чистые project-ключи без записи в xml).
     */
    public static Component researchPopup(String key) {
        if (key == null) {
            return null;
        }
        String langKey = "tempnote." + key + ".popup";
        String resolved = Component.translatable(langKey).getString();
        if (resolved == null || resolved.isEmpty() || resolved.equals(langKey)) {
            return null;
        }
        return Component.translatable(langKey);
    }

    /**
     * Превращение готовой записки в открытие (паритет reforged toDiscovery
     * через transmuteCopy: тот же класс, другой id, NBT сохраняется).
     */
    public static ItemStack toDiscovery(ItemStack note) {
        if (DiscoveredNoteSetup.getDiscoveryItem() == null) {
            return note;
        }
        ItemStack out = new ItemStack(DiscoveredNoteSetup.getDiscoveryItem(), note.getCount());
        // NEOFORGE_PORT: hasTag/setTag удалены -> CUSTOM_DATA целиком.
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(note);
        if (custom != null && !custom.isEmpty()) {
            ArcaneresearchInvHelper.setCustomTag(out, custom.copy());
        }
        // NEOFORGE_PORT: открытие всегда RARE (см. getRarity-порт в write).
        out.set(net.minecraft.core.component.DataComponents.RARITY, net.minecraft.world.item.Rarity.RARE);
        return out;
    }

    /** Открытие temp с ключом (предмет discovery + NBT-ключ). */
    public static boolean isDiscoveryFor(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || key == null
                || DiscoveredNoteSetup.getDiscoveryItem() == null) {
            return false;
        }
        if (stack.getItem() != DiscoveredNoteSetup.getDiscoveryItem()) {
            return false;
        }
        return key.equals(DiscoveredNoteData.read(stack).key);
    }

    /**
     * Есть ли у игрока открытие под ключ (для required_discovery).
     * Только предъявленное ПКМ (метка); держать в руках/инвентаре мало.
     */
    public static boolean hasDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        return knowledge != null && knowledge.hasPresentedDiscovery(key);
    }

    /**
     * Предъявить открытие ПКМ (предмет остаётся в инвентаре).
     * Принимаются открытие и готовая (100%) записка под ключ.
     * Сразу тост (флаг DISCOVERY) + синк, чтобы книга видела метку.
     */
    public static boolean presentDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return false;
        }
        boolean added = knowledge.addPresentedDiscovery(key);
        if (added && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String target = arcaneresearch.api.research.ResearchCategories.getResearch(key) != null
                    ? key
                    : null;
            if (target == null) {
                arcaneresearch.api.research.ResearchEntry req =
                        arcaneresearch.discovered.table.DiscoveredTableResearch.requiringResearch(key);
                target = req == null ? null : req.getKey();
            }
            if (target != null && knowledge.isResearchKnown(target)) {
                knowledge.setResearchFlag(target,
                        arcaneresearch.api.capabilities.IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
            }
            knowledge.sync(serverPlayer);
        }
        return added;
    }

    /**
     * Готовая (100%) записка под ключ — тоже доказательство открытия.
     *
     * @deprecated проверка идёт по метке предъявления, не по рукам; никем не вызывается.
     */
    @Deprecated
    public static boolean isCompleteNoteFor(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || key == null
                || DiscoveredNoteSetup.getNoteItem() == null) {
            return false;
        }
        if (stack.getItem() != DiscoveredNoteSetup.getNoteItem()) {
            return false;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        return key.equals(data.key) && data.isComplete();
    }

    /** Списать предъявленное открытие (предмет не трогаем). */
    public static boolean consumeDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        return knowledge != null && knowledge.consumePresentedDiscovery(key);
    }

    /** Стак для отображения требования (пустое открытие с ключом). */
    public static ItemStack discoveryStack(String key) {
        if (DiscoveredNoteSetup.getDiscoveryItem() == null || key == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(DiscoveredNoteSetup.getDiscoveryItem());
        DiscoveredNoteData.write(stack, DiscoveredNoteData.create(key, new AspectList()));
        return stack;
    }

    /**
     * Убрать у игрока все записки и открытия temp.
     *
     * @deprecated сброс командой чистит только книгу, предметы остаются;
     *             никем не вызывается.
     */
    @Deprecated
    public static int clearNotes(Player player) {
        if (player == null) {
            return 0;
        }
        int removed = 0;
        // NEOFORGE_PORT: Inventory.items приватны в 26.x -> Container-API.
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemDiscoveredResearchNote) {
                inventory.setItem(i, ItemStack.EMPTY);
                removed++;
            }
        }
        if (!player.getOffhandItem().isEmpty()
                && player.getOffhandItem().getItem() instanceof ItemDiscoveredResearchNote) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            removed++;
        }
        return removed;
    }

    /**
     * Чернила для записки (паритет reforged consumeInk): первый IScribeTools
     * с прочностью в основном инвентаре. doIt=false — только проверка.
     *
     * @deprecated стол бумагу/чернила не тратит из инвентаря; никем не вызывается.
     */
    @Deprecated
    public static boolean consumeInk(Player player, boolean doIt) {
        // NEOFORGE_PORT: итерация по Container-API (items приватны в 26.x).
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof IScribeTools
                    && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doIt && !player.getAbilities().instabuild) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Бумага для записки (паритет reforged consumePaper).
     *
     * @deprecated стол берёт бумагу из своего слота; никем не вызывается.
     */
    @Deprecated
    public static boolean consumePaper(Player player) {
        // NEOFORGE_PORT: итерация по Container-API (items приватны в 26.x).
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    /**
     * Создать записку из припасов игрока (паритет reforged createNoteFromSupplies):
     * проверка чернил → трата бумаги → трата чернил → записка.
     *
     * @deprecated записки создают стол и вкладка креатива; никем не вызывается.
     */
    @Deprecated
    public static ItemStack createNoteFromSupplies(Player player, String key, AspectList required) {
        if (consumeInk(player, false) && consumePaper(player)) {
            consumeInk(player, true);
            return createNote(key, required);
        }
        return null;
    }
}
