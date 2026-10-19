package arcaneresearch.common.lib.utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
public class BlockStateUtils {
    public static Direction getFacing(BlockState state) {
        if (state == null) {
            return Direction.NORTH;
        }
        for (DirectionProperty prop : new DirectionProperty[]{
                BlockStateProperties.FACING, BlockStateProperties.HORIZONTAL_FACING,
                BlockStateProperties.FACING_HOPPER}) {
            try {
                if (state.hasProperty(prop)) {
                    return state.getValue(prop);
                }
            } catch (Exception ignored) {
            }
        }
        try {
            for (Property<?> p : state.getProperties()) {
                if (p instanceof DirectionProperty dp && state.hasProperty(dp)) {
                    return state.getValue(dp);
                }
            }
        } catch (Exception ignored) {
        }
        return Direction.NORTH;
    }
    public static Direction getFacing(int meta) {
        return Direction.from2DDataValue(meta & 0x7);
    }
    public static boolean isEnabled(BlockState state) {
        if (state == null) {
            return true;
        }
        for (String name : new String[]{"enabled", "active", "lit"}) {
            Property<?> p = getPropertyByName(state, name);
            if (p instanceof BooleanProperty bp && state.hasProperty(bp)) {
                boolean v = state.getValue(bp);
                return name.equals("enabled") || name.equals("active") ? v : v;
            }
        }
        if (state.hasProperty(BlockStateProperties.POWERED)) {
            return !state.getValue(BlockStateProperties.POWERED);
        }
        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return true;
    }
    public static boolean isEnabled(int meta) {
        return (meta & 0x8) != 0x8;
    }
    @SuppressWarnings("rawtypes")
    public static Property<?> getPropertyByName(BlockState blockState, String propertyName) {
        if (blockState == null) {
            return null;
        }
        for (Property<?> property : blockState.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }
    public static boolean isValidPropertyName(BlockState blockState, String propertyName) {
        return getPropertyByName(blockState, propertyName) != null;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Comparable<?> getPropertyValueByName(BlockState blockState, Property<? extends Comparable> property, String valueName) {
        for (Comparable value : (Collection<Comparable>) property.getPossibleValues()) {
            if (value.toString().equals(valueName)) {
                return value;
            }
        }
        return null;
    }
    public static ImmutableSet<BlockState> getValidStatesForProperties(BlockState baseState, Property<?>... properties) {
        if (properties == null) {
            return null;
        }
        Set<BlockState> validStates = Sets.newHashSet();
        PropertyIndexer propertyIndexer = new PropertyIndexer(properties);
        do {
            BlockState currentState = baseState;
            for (Property<?> property : properties) {
                IndexedProperty indexedProperty = propertyIndexer.getIndexedProperty(property);
                currentState = setPropertyUnsafe(currentState, property, indexedProperty.getCurrentValue());
            }
            validStates.add(currentState);
        } while (propertyIndexer.increment());
        return ImmutableSet.copyOf(validStates);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState setPropertyUnsafe(BlockState state, Property property, Comparable value) {
        try {
            return state.setValue(property, value);
        } catch (Exception e) {
            return state;
        }
    }
    private static class PropertyIndexer {
        private final HashMap<Property<?>, IndexedProperty> indexedProperties = new HashMap<>();
        private final Property<?> finalProperty;
        private PropertyIndexer(Property<?>... properties) {
            finalProperty = properties[properties.length - 1];
            IndexedProperty previous = null;
            for (Property<?> property : properties) {
                IndexedProperty indexed = new IndexedProperty(property);
                if (previous != null) {
                    indexed.parent = previous;
                    previous.child = indexed;
                }
                indexedProperties.put(property, indexed);
                previous = indexed;
            }
        }
        public boolean increment() {
            return indexedProperties.get(finalProperty).increment();
        }
        public IndexedProperty getIndexedProperty(Property<?> property) {
            return indexedProperties.get(property);
        }
    }
    private static class IndexedProperty {
        private final ArrayList<Comparable<?>> validValues = new ArrayList<>();
        private final int maxCount;
        private int counter;
        private IndexedProperty parent;
        private IndexedProperty child;
        @SuppressWarnings({"rawtypes", "unchecked"})
        private IndexedProperty(Property<?> property) {
            for (Comparable v : (Collection<Comparable>) (Collection) property.getPossibleValues()) {
                validValues.add(v);
            }
            maxCount = validValues.size() - 1;
        }
        public boolean increment() {
            if (counter < maxCount) {
                ++counter;
                return true;
            }
            if (hasParent()) {
                resetSelfAndChildren();
                return parent.increment();
            }
            return false;
        }
        public void resetSelfAndChildren() {
            counter = 0;
            if (hasChild()) {
                child.resetSelfAndChildren();
            }
        }
        public boolean hasParent() {
            return parent != null;
        }
        public boolean hasChild() {
            return child != null;
        }
        public Comparable<?> getCurrentValue() {
            return validValues.get(counter);
        }
    }
}
