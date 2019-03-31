package net.gegy1000.psf.api.util;

import io.netty.buffer.ByteBuf;

public interface IByteBufSerializeable {
    
    void serialize(ByteBuf buf);
    
    void deserialize(ByteBuf buf);

}
