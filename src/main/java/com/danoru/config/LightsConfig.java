package com.danoru.config;

import com.danoru.codecs.Network;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import org.bson.BsonDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LightsConfig {
    public static final BuilderCodec<LightsConfig> CODEC = BuilderCodec.builder(LightsConfig.class, LightsConfig::new)
            .append(new KeyedCodec<>("Global_Networks", new MapCodec<>(new SetCodec<>(Network.CODEC, HashSet::new, false), HashMap::new, false)),
                    (data, value) -> data.globalNetworks = value,
                    data -> data.globalNetworks).add()
            .build();

    private Map<String, Set<Network>> globalNetworks = new HashMap<>();

    public void setGlobalNetwork(String uuId,Set<Network> localNetworks) {
        globalNetworks.put(uuId, localNetworks);
    }
    public void clearGlobalNetwork(String uuId) {
        globalNetworks.remove(uuId);
    }

    //POR ARREGLAR
    public boolean containsGlobalSwitchValue(Vector3i targetBlock) {
        for(Set<Network> networks : globalNetworks.values()) {
            for(Network local : networks) {
                if(local.getSwitchBlock().equals(targetBlock)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean containsGlobalSwitchValue(BsonDocument metadata) {
        for(Set<Network> networks : globalNetworks.values()) {
            for(Network local : networks) {
                String id = metadata.get("Uuid").asString().getValue();
                if(local.getSwitchWireless().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getIdforSwitchGlobal(Vector3i posSwitch) {
        for(Set<Network> networks : globalNetworks.values()) {
            for(Network local : networks) {
                if(local.getSwitchBlock().equals(posSwitch)) {
                    return local.getId();
                }
            }
        }
        return null;
    }
    public String getIdforSwitchLocal(BsonDocument metadata) {
        for(Set<Network> networks : globalNetworks.values()) {
            for(Network local : networks) {
                String id = metadata.get("Uuid").asString().getValue();
                if(local.getSwitchWireless().equals(id)) {
                    return local.getId();
                }
            }
        }
        return null;
    }

    public Set<Vector3i> getLightsForID(String idNetwork) {
        for(Set<Network> networks : globalNetworks.values()) {
            for(Network local : networks) {
                if(local.getId().equals(idNetwork)) {
                    return local.getLights();
                }
            }
        }
        return null;
    }
}
