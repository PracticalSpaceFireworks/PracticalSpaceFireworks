package net.gegy1000.psf.server.satellite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import lombok.val;
import net.gegy1000.psf.api.IUnique;

public class UniqueManager<T extends IUnique> {
    
    private Cache<UUID, T> satelliteCache = CacheBuilder.newBuilder()
            .weakValues()
            .removalListener(this::onRemoved)
            .<UUID, T>build();
    
    private List<Consumer<T>> addCallbacks = new ArrayList<>();
    private List<Consumer<T>> removeCallbacks = new ArrayList<>();
    
    public void register(@Nonnull T obj) {
        this.satelliteCache.put(obj.getId(), obj);
        for (val c : addCallbacks) {
            c.accept(obj);
        }
    }
    
    public void remove(@Nonnull T obj) {
        remove(obj.getId());
    }

    public void remove(@Nonnull UUID id) {
        this.satelliteCache.invalidate(id);
    }

    @Nullable
    public T get(@Nonnull UUID id) {
        return satelliteCache.getIfPresent(id);
    }
    
    public Collection<T> getAll() {
        return satelliteCache.asMap().values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public void onAdd(Consumer<T> callback) {
        this.addCallbacks.add(callback);
    }
    
    public void onRemove(Consumer<T> callback) {
        this.removeCallbacks.add(callback);
    }
    
    private void onRemoved(RemovalNotification<UUID, T> notif) {
        for (val c : removeCallbacks) {
            c.accept(notif.getValue());
        }
    }

    public void flush() {
        this.satelliteCache.invalidateAll();
    }
}
