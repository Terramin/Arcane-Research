package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.codec.StreamCodec;





public final class LazyCodecs {

    private LazyCodecs() {
    }

    public static <T> MapCodec<T> lazyUnit(java.util.function.Supplier<T> factory) {
        return new MapCodec<>() {
            private volatile MapCodec<T> delegate;

            private MapCodec<T> delegate() {
                MapCodec<T> d = delegate;
                if (d == null) {
                    synchronized (this) {
                        d = delegate;
                        if (d == null) {
                            d = MapCodec.unit(factory.get());
                            delegate = d;
                        }
                    }
                }
                return d;
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
                return delegate().decode(ops, input);
            }

            @Override
            public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
                return delegate().encode(input, ops, prefix);
            }

            @Override
            public <T1> java.util.stream.Stream<T1> keys(DynamicOps<T1> ops) {
                return delegate().keys(ops);
            }

            @Override
            public String toString() {
                return "LazyUnit";
            }
        };
    }

    public static <B, T> StreamCodec<B, T> lazyUnitStream(java.util.function.Supplier<T> factory) {
        return new StreamCodec<>() {
            private volatile StreamCodec<B, T> delegate;

            private StreamCodec<B, T> delegate() {
                StreamCodec<B, T> d = delegate;
                if (d == null) {
                    synchronized (this) {
                        d = delegate;
                        if (d == null) {
                            d = StreamCodec.unit(factory.get());
                            delegate = d;
                        }
                    }
                }
                return d;
            }

            @Override
            public void encode(B buffer, T value) {
                delegate().encode(buffer, value);
            }

            @Override
            public T decode(B buffer) {
                return delegate().decode(buffer);
            }
        };
    }
}
