package arcaneresearch.common.lib.utils;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BlockStateUtils {

    private static final DirectionProperty[] FACING_PROPS = {
            BlockStateProperties.FACING,
            BlockStateProperties.HORIZONTAL_FACING,
            BlockStateProperties.FACING_HOPPER
    };

    private static final String[] ENABLED_PROP_NAMES = {"enabled", "active", "lit"};

    public static Direction getFacing(BlockState state) {
        if (state == null) return Direction.NORTH;

        for (DirectionProperty prop : FACING_PROPS) {
            if (state.hasProperty(prop)) {
                try {
                    return state.getValue(prop);
                } catch (RuntimeException ignored) {
                }
            }
        }

        try {
            for (Property<?> p : state.getProperties()) {
                if (p instanceof DirectionProperty dp && state.hasProperty(dp)) {
                    return state.getValue(dp);
                }
            }
        } catch (RuntimeException ignored) {
        }
        return Direction.NORTH;
    }

    public static Direction getFacing(int meta) {
        return Direction.from2DDataValue(meta & 0x7);
    }

    public static boolean isEnabled(BlockState state) {
        if (state == null) return true;

        for (String name : ENABLED_PROP_NAMES) {
            Property<?> p = getPropertyByName(state, name);
            if (p instanceof BooleanProperty bp && state.hasProperty(bp)) {
                return state.getValue(bp);
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

    public static Property<?> getPropertyByName(BlockState blockState, String propertyName) {
        if (blockState == null || propertyName == null) return null;

        for (Property<?> property : blockState.getProperties()) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

    public static boolean isValidPropertyName(BlockState blockState, String propertyName) {
        return getPropertyByName(blockState, propertyName) != null;
    }

    @SuppressWarnings("rawtypes")
    public static Comparable<?> getPropertyValueByName(BlockState blockState,
                                                       Property<? extends Comparable> property,
                                                       String valueName) {
        if (property == null || valueName == null) return null;

        Collection<?> values;
        try {
            values = property.getPossibleValues();
        } catch (RuntimeException e) {
            return null;
        }

        for (Object value : values) {
            if (value instanceof Comparable<?> comp && valueName.equals(comp.toString())) {
                return comp;
            }
        }
        return null;
    }

    public static ImmutableSet<BlockState> getValidStatesForProperties(BlockState baseState,
                                                                       Property<?>... properties) {
        if (baseState == null || properties == null || properties.length == 0) {
            return ImmutableSet.of();
        }

        Set<BlockState> validStates = new HashSet<>();
        PropertyIndexer propertyIndexer = new PropertyIndexer(properties);

        do {
            BlockState currentState = baseState;
            for (Property<?> property : properties) {
                if (property == null) continue;

                IndexedProperty indexedProperty = propertyIndexer.getIndexedProperty(property);
                if (indexedProperty == null) continue;

                Comparable<?> value = indexedProperty.getCurrentValue();
                if (value == null) continue;

                currentState = setPropertyUnsafe(currentState, property, value);
            }
            validStates.add(currentState);
        } while (propertyIndexer.increment());

        return ImmutableSet.copyOf(validStates);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState setPropertyUnsafe(BlockState state, Property property, Comparable value) {
        try {
            return state.setValue(property, value);
        } catch (RuntimeException e) {
            return state;
        }
    }

    private static final class PropertyIndexer {
        private final HashMap<Property<?>, IndexedProperty> indexedProperties;
        private final Property<?> finalProperty;

        private PropertyIndexer(Property<?>[] properties) {
            indexedProperties = new HashMap<>(properties.length);
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
            IndexedProperty last = indexedProperties.get(finalProperty);
            return last != null && last.increment();
        }

        public IndexedProperty getIndexedProperty(Property<?> property) {
            return indexedProperties.get(property);
        }
    }

    private static final class IndexedProperty {
        private final ArrayList<Comparable<?>> validValues;
        private final int maxCount;
        private int counter;
        private IndexedProperty parent;
        private IndexedProperty child;

        private IndexedProperty(Property<?> property) {
            validValues = new ArrayList<>();
            if (property != null) {
                Collection<?> values;
                try {
                    values = property.getPossibleValues();
                } catch (RuntimeException e) {
                    values = null;
                }
                if (values != null) {
                    for (Object v : values) {
                        if (v instanceof Comparable<?> comp) {
                            validValues.add(comp);
                        }
                    }
                }
            }
            maxCount = validValues.size() - 1;
        }

        public boolean increment() {
            if (counter < maxCount) {
                counter++;
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
            if (counter < 0 || counter >= validValues.size()) return null;
            return validValues.get(counter);
        }
    }
}