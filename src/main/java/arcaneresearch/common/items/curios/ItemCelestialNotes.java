package arcaneresearch.common.items.curios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.common.items.ItemTCBase;
import javax.annotation.Nullable;
import java.util.List;
public class ItemCelestialNotes extends ItemTCBase {
    private final NoteType noteType;
    public ItemCelestialNotes(NoteType type) {
        super(new Properties().stacksTo(64), "celestial_notes", type.getId());
        this.noteType = type;
    }
    public static ItemCelestialNotes createSun() {
        return new ItemCelestialNotes(NoteType.SUN);
    }
    public static ItemCelestialNotes createStars1() {
        return new ItemCelestialNotes(NoteType.STARS_1);
    }
    public static ItemCelestialNotes createStars2() {
        return new ItemCelestialNotes(NoteType.STARS_2);
    }
    public static ItemCelestialNotes createStars3() {
        return new ItemCelestialNotes(NoteType.STARS_3);
    }
    public static ItemCelestialNotes createStars4() {
        return new ItemCelestialNotes(NoteType.STARS_4);
    }
    public static ItemCelestialNotes createMoon1() {
        return new ItemCelestialNotes(NoteType.MOON_1);
    }
    public static ItemCelestialNotes createMoon2() {
        return new ItemCelestialNotes(NoteType.MOON_2);
    }
    public static ItemCelestialNotes createMoon3() {
        return new ItemCelestialNotes(NoteType.MOON_3);
    }
    public static ItemCelestialNotes createMoon4() {
        return new ItemCelestialNotes(NoteType.MOON_4);
    }
    public static ItemCelestialNotes createMoon5() {
        return new ItemCelestialNotes(NoteType.MOON_5);
    }
    public static ItemCelestialNotes createMoon6() {
        return new ItemCelestialNotes(NoteType.MOON_6);
    }
    public static ItemCelestialNotes createMoon7() {
        return new ItemCelestialNotes(NoteType.MOON_7);
    }
    public static ItemCelestialNotes createMoon8() {
        return new ItemCelestialNotes(NoteType.MOON_8);
    }
    public NoteType getNoteType() {
        return noteType;
    }
    @Override
    protected String getOrCreateDescriptionId() {
        return "item.arcaneresearch.celestial_notes";
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.celestial_notes." + noteType.getId() + ".text")
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