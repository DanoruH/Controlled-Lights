package com.danoru.components;

import com.danoru.ControlledLights;
import com.danoru.config.LightsConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworksComponent implements Component<EntityStore> {
    private Map<String, Vector3i> localSwitches;
    private Map<String, String> localSwitches_Remote;
    private Map<String, Set<Vector3i>> localLights;
    private Set<String> idNetworks;

    private boolean modeCreate = false;
    private boolean modeEdit = false;
    private short idDefault = 0;
    private String uuidNetwork = null;

    public static BuilderCodec<NetworksComponent> CODEC = BuilderCodec.builder(NetworksComponent.class, NetworksComponent::new)
            .append(new KeyedCodec<>("LocalSwitches", new MapCodec<>(Vector3i.CODEC, HashMap::new, false)),
                    (data, value) -> data.localSwitches = value,
                    data -> data.localSwitches).add()
            .append(new KeyedCodec<>("LocalLights", new MapCodec<>(new SetCodec<>(Vector3i.CODEC, HashSet::new, false), HashMap::new, false)),
                    (data, value) -> data.localLights = value,
                    data -> data.localLights).add()
            .append(new KeyedCodec<>("IdNetworks", new SetCodec<>(Codec.STRING, HashSet::new, false)),
                    (data, value) -> data.idNetworks = value,
                    data -> data.idNetworks).add()
            .append(new KeyedCodec<>("IDdefault", Codec.SHORT),
                    (data, value) -> data.idDefault = value,
                    data -> data.idDefault).add()
            .append(new KeyedCodec<>("UuidNetwork", Codec.STRING),
                    (data, value) -> data.uuidNetwork = value,
                    (data) -> data.uuidNetwork).add()
            .build();

    public Map<String, Vector3i> getLocalSwitches() {
        return localSwitches;
    }
    public Map<String, Set<Vector3i>> getLocalLights() {
        return localLights;
    }
    public Set<String> getIdNetworks() {
        return idNetworks;
    }
    public short getIdDefault() {
        return idDefault;
    }
    public boolean isModeCreate() {
        return modeCreate;
    }
    public void setModeCreate(boolean modeCreate) {
        this.modeCreate = modeCreate;
    }
    public boolean isModeEdit() {
        return modeEdit;
    }
    public void setModeEdit(boolean modeEdit) {
        this.modeEdit = modeEdit;
    }
    public String getUUIDNetwork() {
        return uuidNetwork;
    }
    public void setUUIDNetwork(String UUIDNetwork) {
        this.uuidNetwork = UUIDNetwork;
    }

    public void setNetwork(String id, Set<Vector3i> listLights, Vector3i switchNetwork) {
        localSwitches.put(id, switchNetwork);
        localLights.put(id, listLights);
        idNetworks.add(id);
        updateGlobalNetwork();
    }

    public void setNetwork(String id, Set<Vector3i> listLights, String switchRemoteNetwork) {
        localSwitches_Remote.put(id, switchRemoteNetwork);
        localLights.put(id, listLights);
        idNetworks.add(id);
        updateGlobalNetwork();
    }

    private void updateGlobalNetwork() {
        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();
        config.setGlobalNetwork(uuidNetwork, localSwitches, localLights, idNetworks);
        myconfig.save();
    }

    public void removeNetwork(String id) {
        localLights.remove(id);
        localSwitches.remove(id);
        idNetworks.remove(id);
        updateGlobalNetwork();
    }
    public void removeAllNetworks() {
        localSwitches.clear();
        localLights.clear();
        idNetworks.clear();
        clearGlobalNetwork();
    }
    private void clearGlobalNetwork() {
        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();
        config.clearGlobalNetwork(uuidNetwork);
        myconfig.save();
    }

    public String getIdForSwitch(Vector3i posSwitch) {
        for(String key:idNetworks) {
            if(getLocalSwitches().get(key).equals(posSwitch)){
                return key;
            }
        }
        return "null";
    }
    public String getIdForIndex(String index) {
        short ind = 0;
        for(String id : idNetworks) {
            if(String.valueOf(ind).equals(index)) {
                return id;
            }
            ind++;
        }
        return null;
    }

    public String consumeIDdefault() {
        String idConsume = String.valueOf(idDefault);
        idDefault++;
        return idConsume;
    }


    public static ComponentType getComponentType() {return ControlledLights.getNetworksComponentType();
    }

    public NetworksComponent() {
        localSwitches = new HashMap<>();
        localLights = new HashMap<>();
        idNetworks = new HashSet<>();
    }

    @Override
    public String toString() {
        return "SWITCHES: " + localSwitches.toString() +"\n LIGHTS: " + localLights.toString() +"\n IDNETWORKS: " + idNetworks.toString();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        NetworksComponent copy = new NetworksComponent();
        copy.localLights = this.localLights;
        copy.localSwitches = this.localSwitches;
        copy.idNetworks = this.idNetworks;
        copy.idDefault = this.idDefault;
        return copy;
    }
}
