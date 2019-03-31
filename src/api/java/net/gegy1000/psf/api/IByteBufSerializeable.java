package net.gegy1000.psf.api;

import io.netty.buffer.ByteBuf;

public interface IByteBufSerializeable {
    
    void serialize(ByteBuf buf);
    
    void deserialize(ByteBuf buf);

}
