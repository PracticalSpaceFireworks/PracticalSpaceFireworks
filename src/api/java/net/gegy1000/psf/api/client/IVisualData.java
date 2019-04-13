package net.gegy1000.psf.api.client;

import io.netty.buffer.ByteBuf;

import java.util.Optional;

public interface IVisualData {
    void serialize(ByteBuf buf);

    <T> Optional<T> get(IVisualProperty<T> property);
}
