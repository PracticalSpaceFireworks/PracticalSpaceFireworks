package net.gegy1000.psf.server.block.remote.visual;

import io.netty.buffer.ByteBuf;
import net.gegy1000.psf.api.client.IVisualProperty;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SimpleVisualProperty<T> implements IVisualProperty<T> {
    private final BiConsumer<T, ByteBuf> serializer;
    private final Function<ByteBuf, T> deserializer;

    private SimpleVisualProperty(BiConsumer<T, ByteBuf> serializer, Function<ByteBuf, T> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public static <T> SimpleVisualProperty<T> of(BiConsumer<T, ByteBuf> serializer, Function<ByteBuf, T> deserializer) {
        return new SimpleVisualProperty<>(serializer, deserializer);
    }

    public static SimpleVisualProperty<Double> ofDouble() {
        return new SimpleVisualProperty<>((v, buf) -> buf.writeDouble(v), ByteBuf::readDouble);
    }

    @Override
    public void serialize(T value, ByteBuf buf) {
        serializer.accept(value, buf);
    }

    @Override
    public T deserialize(ByteBuf buf) {
        return deserializer.apply(buf);
    }
}
