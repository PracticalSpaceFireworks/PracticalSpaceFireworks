package net.gegy1000.psf.api.client;

import io.netty.buffer.ByteBuf;

public interface IVisualProperty<T> {
    void serialize(T value, ByteBuf buf);

    T deserialize(ByteBuf buf);
}
