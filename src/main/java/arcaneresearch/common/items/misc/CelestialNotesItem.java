package arcaneresearch.common.items.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.common.items.ItemTCBase;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CelestialNotesItem extends ItemTCBase {
    private final NoteType noteType;

    public CelestialNotesItem(NoteType type) {
        super(new Properties().stacksTo(64), "celestial_notes", type.getId());
        this.noteType = type;
    }

    public static CelestialNotesItem createSun() {
        return new CelestialNotesItem(NoteType.SUN);
    }

    public static CelestialNotesItem createStars1() {
        return new CelestialNotesItem(NoteType.STARS_1);
    }

    public static CelestialNotesItem createStars2() {
        return new CelestialNotesItem(NoteType.STARS_2);
    }

    public static CelestialNotesItem createStars3() {
        return new CelestialNotesItem(NoteType.STARS_3);
    }

    public static CelestialNotesItem createStars4() {
        return new CelestialNotesItem(NoteType.STARS_4);
    }

    public static CelestialNotesItem createMoon1() {
        return new CelestialNotesItem(NoteType.MOON_1);
    }

    public static CelestialNotesItem createMoon2() {
        return new CelestialNotesItem(NoteType.MOON_2);
    }

    public static CelestialNotesItem createMoon3() {
        return new CelestialNotesItem(NoteType.MOON_3);
    }

    public static CelestialNotesItem createMoon4() {
        return new CelestialNotesItem(NoteType.MOON_4);
    }

    public static CelestialNotesItem createMoon5() {
        return new CelestialNotesItem(NoteType.MOON_5);
    }

    public static CelestialNotesItem createMoon6() {
        return new CelestialNotesItem(NoteType.MOON_6);
    }

    public static CelestialNotesItem createMoon7() {
        return new CelestialNotesItem(NoteType.MOON_7);
    }

    public static CelestialNotesItem createMoon8() {
        return new CelestialNotesItem(NoteType.MOON_8);
    }

    public NoteType getNoteType() {
        return noteType;
    }

    @Override
    protected @NotNull String getOrCreateDescriptionId() {
        return "item.arcaneresearch.celestial_notes";
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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