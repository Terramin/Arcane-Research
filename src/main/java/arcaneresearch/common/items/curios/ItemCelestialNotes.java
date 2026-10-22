package arcaneresearch.common.items.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import arcaneresearch.common.items.ItemTCBase;

import java.util.function.Consumer;

public class ItemCelestialNotes extends ItemTCBase {

    private final NoteType noteType;

    public ItemCelestialNotes(Item.Properties props, NoteType type) {
        super(props.stacksTo(64), "celestial_notes", type.getId());
        this.noteType = type;
    }

    public static ItemCelestialNotes createSun(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.SUN);
    }

    public static ItemCelestialNotes createStars1(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.STARS_1);
    }

    public static ItemCelestialNotes createStars2(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.STARS_2);
    }

    public static ItemCelestialNotes createStars3(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.STARS_3);
    }

    public static ItemCelestialNotes createStars4(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.STARS_4);
    }

    public static ItemCelestialNotes createMoon1(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_1);
    }

    public static ItemCelestialNotes createMoon2(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_2);
    }

    public static ItemCelestialNotes createMoon3(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_3);
    }

    public static ItemCelestialNotes createMoon4(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_4);
    }

    public static ItemCelestialNotes createMoon5(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_5);
    }

    public static ItemCelestialNotes createMoon6(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_6);
    }

    public static ItemCelestialNotes createMoon7(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_7);
    }

    public static ItemCelestialNotes createMoon8(Item.Properties props) {
        return new ItemCelestialNotes(props, NoteType.MOON_8);
    }

    public NoteType getNoteType() {
        return noteType;
    }

    
    
    
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.arcaneresearch.celestial_notes");
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.celestial_notes." + noteType.getId() + ".text")
                .withStyle(ChatFormatting.AQUA));
    }

    public enum NoteType {
        SUN("sun"),
        STARS_1("stars_1"),
        STARS_2("stars_2"),
        STARS_3("stars_3"),
        STARS_4("stars_4"),
        MOON_1("moon_1"),
        MOON_2("moon_2"),
        MOON_3("moon_3"),
        MOON_4("moon_4"),
        MOON_5("moon_5"),
        MOON_6("moon_6"),
        MOON_7("moon_7"),
        MOON_8("moon_8");

        private final String id;

        NoteType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
