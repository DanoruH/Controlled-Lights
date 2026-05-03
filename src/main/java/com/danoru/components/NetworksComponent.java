package com.danoru.components;

import com.danoru.ControlledLights;
import com.danoru.codecs.Network;
import com.danoru.config.LightsConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.bson.BsonDocument;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworksComponent implements Component<EntityStore> {
    private Set<Network> networks;

    private boolean modeCreate = false;
    private boolean modeEdit = false;
    private short idDefault = 0;
    private String uuidNetwork = null;

    public static BuilderCodec<NetworksComponent> CODEC = BuilderCodec.builder(NetworksComponent.class, NetworksComponent::new)
            .append(new KeyedCodec<>("Networks", new SetCodec<>(Network.CODEC, HashSet::new, false)),
                    (data, value) -> data.networks = value,
                    data -> data.networks).add()
            .append(new KeyedCodec<>("IDdefault", Codec.SHORT),
                    (data, value) -> data.idDefault = value,
                    data -> data.idDefault).add()
            .append(new KeyedCodec<>("UuidNetwork", Codec.STRING),
                    (data, value) -> data.uuidNetwork = value,
                    (data) -> data.uuidNetwork).add()
            .build();

    public Set<Network> getNetworks() {
        return networks;
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

    //FUNCIÓN PARA SWITCH TIPO BLOQUE
    public void setNetwork(String id, Set<Vector3i> listLights, Vector3i switchNetwork, String itemID) {
        Network network = new Network();
        network.setId(id);
        network.setLights(listLights);
        network.setSwitchBlock(switchNetwork);
        network.setIdItem(itemID);

        this.networks.add(network);
        updateGlobalNetwork();
    }

    //FUNCIÓN PARA SWITCH TIPO ITEM
    public void setNetwork(String id, Set<Vector3i> listLights, String switchWirelessUUID, String itemID) {
        Network network = new Network();
        network.setId(id);
        network.setLights(listLights);
        network.setSwitchWireless(switchWirelessUUID);
        network.setIdItem(itemID);

        this.networks.add(network);
        updateGlobalNetwork();
    }

    private void updateGlobalNetwork() {
        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();
        config.setGlobalNetwork(uuidNetwork, networks);
        myconfig.save();
    }

    public void removeNetwork(String id) {
        for(Network local : networks) {
            if(local.getId().equals(id)) {
                networks.remove(local);
                break;
            }
        }
        updateGlobalNetwork();
    }
    public void removeAllNetworks() {
        networks.clear();
        clearGlobalNetwork();
    }
    private void clearGlobalNetwork() {
        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();
        config.clearGlobalNetwork(uuidNetwork);
        myconfig.save();
    }

    public boolean containsSwitchBlock(Vector3i switchPos) {
        for(Network local : networks) {
            if(local.getSwitchBlock().equals(switchPos)) {
                return true;
            }
        }
        return false;
    }
    public boolean containsIds(String id) {
        for(Network local : networks) {
            if(local.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
    public boolean containsuuIdMetadata(BsonDocument metadata) {
        if(metadata == null) {
            return false;
        }

        String meta = metadata.get("Uuid").asString().getValue();
        if(meta != null) {
            for(Network local : networks) {
                if(local.getSwitchWireless().equals(meta)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getIdForSwitch(Vector3i posSwitch) {
        for(Network local : networks) {
            if(local.getSwitchBlock().equals(posSwitch)) {
                return local.getId();
            }
        }
        return "null";
    }
    public String getIdForSwitch(BsonDocument metadata) {
        for(Network local : networks) {
            String uuid = metadata.get("Uuid").asString().getValue();
            if(local.getSwitchWireless().equals(uuid)) {
                return local.getId();
            }
        }
        return "null";
    }

//    public String getIdForSwitchWireless(String uuidSwitch) {
//        for(String key:idNetworks) {
//            if(getLocalSwitchesWireless().get(key).equals(uuidSwitch)){
//                return key;
//            }
//        }
//        return "null";
//    }
//
    public Network getNetworkForId(String id) {
        for(Network local : networks) {
            if(local.getId().equals(id)) {
                return local;
            }
        }
        return null;
    }

    public String getIdForIndex(String index) {
        short ind = 0;
        for(Network local : networks) {
            String id = local.getId();
            if(String.valueOf(ind).equals(index)) {
                return id;
            }
            ind++;
        }
        return null;
    }

//    public String getIDforItem(ItemStack itemStack) {
//        BsonDocument metadata = itemStack.getMetadata();
//        if(metadata != null) {
//            return metadata.get("Uuid").asString().getValue();
//        }
//        return null;
//    }

    public String consumeIDdefault() {
        String idConsume = String.valueOf(idDefault);
        idDefault++;
        return idConsume;
    }


    public static ComponentType getComponentType() {return ControlledLights.getNetworksComponentType();
    }

    public NetworksComponent() {
        networks = new HashSet<>();
    }

    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        NetworksComponent copy = new NetworksComponent();
        copy.idDefault = this.idDefault;
        copy.networks = this.networks;
        copy.uuidNetwork = this.uuidNetwork;
        return copy;
    }
}
