package net.gegy1000.psf.server.block.remote.visual;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.client.IVisualProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualData implements IVisualData {
    private final ImmutableMap<IVisualProperty<?>, Object> values;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeByte(values.size() & 0xFF);

        for (Map.Entry<IVisualProperty<?>, Object> entry : values.entrySet()) {
            ResourceLocation id = VisualProperties.getId(entry.getKey());
            if (id == null) throw new IllegalStateException("Cannot serialize unregistered property");

            ByteBufUtils.writeUTF8String(buf, id.toString());
            serializeProperty(entry.getKey(), entry.getValue(), buf);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void serializeProperty(IVisualProperty<T> property, Object value, ByteBuf buf) {
        property.serialize((T) value, buf);
    }

    public static VisualData deserialize(ByteBuf buf) {
        ImmutableMap.Builder<IVisualProperty<?>, Object> properties = ImmutableMap.builder();

        int count = buf.readUnsignedByte();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
            IVisualProperty<?> property = VisualProperties.byId(id);
            if (property == null) {
                PracticalSpaceFireworks.LOGGER.warn("Received invalid visual property id {}", id);
                continue;
            }

            properties.put(property, property.deserialize(buf));
        }

        return new VisualData(properties.build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(IVisualProperty<T> property) {
        T value = (T) values.get(property);
        return Optional.ofNullable(value);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final ImmutableMap.Builder<IVisualProperty<?>, Object> values = ImmutableMap.builder();

        public <T> Builder with(IVisualProperty<T> property, T value) {
            if (!VisualProperties.isRegistered(property)) {
                PracticalSpaceFireworks.LOGGER.warn("Tried to build unregistered property `{}` with value `{}`", property, value);
                return this;
            }
            values.put(property, value);
            return this;
        }

        public VisualData build() {
            return new VisualData(values.build());
        }
    }
}
