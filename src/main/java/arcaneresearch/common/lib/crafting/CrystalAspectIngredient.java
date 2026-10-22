package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.stream.Stream;










public class CrystalAspectIngredient implements ICustomIngredient {

    public static final Identifier TYPE_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crystal");

    private static final Codec<Aspect> ASPECT_CODEC = Codec.STRING.comapFlatMap(tag -> {
        Aspect aspect = Aspect.getAspect(tag);
        return aspect != null
                ? DataResult.success(aspect)
                : DataResult.error(() -> "Unknown aspect '" + tag + "' in arcaneresearch:crystal ingredient");
    }, Aspect::getTag);

    public static final MapCodec<CrystalAspectIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ASPECT_CODEC.fieldOf("aspect").forGetter(ing -> ing.aspect)
    ).apply(inst, CrystalAspectIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrystalAspectIngredient> STREAM_CODEC =
            StreamCodec.ofMember(CrystalAspectIngredient::write, CrystalAspectIngredient::read);

    public static final IngredientType<CrystalAspectIngredient> TYPE =
            new IngredientType<>(CODEC, STREAM_CODEC);

    private final Aspect aspect;

    public CrystalAspectIngredient(Aspect aspect) {
        if (aspect == null) {
            throw new IllegalArgumentException("CrystalAspectIngredient aspect must not be null");
        }
        this.aspect = aspect;
    }

    private static ItemStack displayStack(Aspect aspect) {
        ItemStack stack = ArcaneresearchApiHelper.makeCrystal(aspect);
        return stack.isEmpty() ? new ItemStack(net.minecraft.world.item.Items.AIR) : stack;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.STRING_UTF8.encode(buf, aspect.getTag());
    }

    private static CrystalAspectIngredient read(RegistryFriendlyByteBuf buffer) {
        Aspect aspect = Aspect.getAspect(ByteBufCodecs.STRING_UTF8.decode(buffer));
        if (aspect == null) {
            throw new IllegalArgumentException("Unknown aspect in arcaneresearch:crystal ingredient");
        }
        return new CrystalAspectIngredient(aspect);
    }

    public Aspect getAspect() {
        return aspect;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemCrystalEssence crystal)) {
            return false;
        }
        AspectList contained = crystal.getAspects(stack);
        if (contained == null || contained.size() == 0) {
            return false;
        }
        return aspect.getTag().equals(contained.getAspects()[0].getTag());
    }

    @Override
    public Stream<Holder<Item>> items() {
        
        
        
        
        
        Item crystalItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crystal_essence"));
        if (crystalItem == null) {
            return Stream.empty();
        }
        return Stream.of(crystalItem.builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromStack(displayStack(aspect)));
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE;
    }
}
