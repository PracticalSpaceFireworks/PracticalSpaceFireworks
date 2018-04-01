package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IModuleData;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public abstract class ConnectableModule extends EmptyModule {
    private final Map<Capability<? extends IModuleData>, Set<IModule>> connectedModules = new HashMap<>();

    public ConnectableModule(String name) {
        super(name);
    }

    public boolean addConnection(IModule module, Capability<? extends IModuleData> connectionType) {
        Set<IModule> connected = this.connectedModules.computeIfAbsent(connectionType, t -> new HashSet<>());
        if (this.canConnect(module, connectionType)) {
            connected.add(module);
            return true;
        } else if (connected.isEmpty()) {
            this.connectedModules.remove(connectionType);
        }
        return false;
    }

    public boolean removeConnection(IModule module, Capability<? extends IModuleData> connectionType) {
        Set<IModule> connected = this.connectedModules.get(connectionType);
        if (connected != null) {
            boolean result = connected.remove(module);
            if (connected.isEmpty()) {
                this.connectedModules.remove(connectionType);
            }
            return result;
        }
        return false;
    }

    public boolean canConnect(IModule module, Capability<? extends IModuleData> connectionType) {
        Set<IModule> connected = this.connectedModules.getOrDefault(connectionType, Collections.emptySet());
        return !connected.contains(module) && this.canConnect(connectionType, connected);
    }

    @Override
    public <T extends IModuleData> Collection<T> getConnectedCaps(ISatellite satellite, Capability<T> capability) {
        Set<IModule> connected = this.connectedModules.get(capability);
        if (!connected.isEmpty()) {
            return connected.stream().filter(module -> module.hasCapability(capability, null))
                    .map(module -> module.getCapability(capability, null))
                    .collect(Collectors.toList());
        }
        return super.getConnectedCaps(satellite, capability);
    }

    protected abstract <T extends IModuleData> boolean canConnect(Capability<T> capability, Set<IModule> connected);
}
