package arcaneresearch.debug;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Logical subsystems that can be profiled independently by the debug command.
 *
 * <p>{@link #ALL} is a wildcard that matches every category.</p>
 */
public enum ProfileCategory {
    ALL("Every subsystem"),
    CORE("Startup, registration and lifecycle"),
    WORLD("World generation, structures, dimensions and chunks"),
    AURA("Aura, essentia, aspects and nodes"),
    NETWORK("Packets, synchronization and messages"),
    ENTITIES("Entities, AI and spawning"),
    RENDER("Client rendering, models and tile entity renderers"),
    RESEARCH("Research, scanning, theorycraft and lore"),
    ITEMS("Items, wands and foci"),
    BLOCKS("Blocks, block entities and multiblocks"),
    COMPAT("Cross-mod compatibility (JEI, Curios, IMC)"),
    PERFORMANCE("Timing and profiling measurements"),
    MEMORY("Memory and garbage collection diagnostics");

    private final String description;

    ProfileCategory(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    /** True when this selection includes {@code other}. */
    public boolean matches(ProfileCategory other) {
        return this == ALL || this == other;
    }

    public static ProfileCategory parse(String token) {
        if (token == null) {
            return null;
        }
        String value = token.trim().toUpperCase(Locale.ROOT);
        for (ProfileCategory category : values()) {
            if (category.name().equals(value)) {
                return category;
            }
        }
        return null;
    }

    public static String names() {
        return Stream.of(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
