package com.danoru.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LightsConfig {
    public static final BuilderCodec<LightsConfig> CODEC = BuilderCodec.builder(LightsConfig.class, LightsConfig::new)
            .append(new KeyedCodec<>("Global_Switches", new MapCodec<>(new MapCodec<>(Vector3i.CODEC, HashMap::new, false), HashMap::new, false)),
                    (data, value) -> data.globalSwitches = value,
                    (   data) -> data.globalSwitches).add()
            .append(new KeyedCodec<>("Global_Lights", new MapCodec<>(new MapCodec<>(new SetCodec<>(Vector3i.CODEC, HashSet::new, false), HashMap::new, false), HashMap::new, false)),
                    (data, value) -> data.globalLights = value,
                    (data) -> data.globalLights).add()
            .append(new KeyedCodec<>("Global_IdNetworks", new MapCodec<>(new SetCodec<>(Codec.STRING, HashSet::new, false), HashMap::new, false)),
                    (data, value) -> data.globalIdNetworks = value,
                    (data) -> data.globalIdNetworks).add()
            .build();

    private Map<String, Map<String, Vector3i>> globalSwitches = new HashMap<>();
    private Map<String, Map<String, Set<Vector3i>>> globalLights = new HashMap<>();
    private Map<String, Set<String>> globalIdNetworks = new HashMap<>();

    public Map<String, Map<String, Vector3i>> getGlobalSwitches() {
        return globalSwitches;
    }
    public Map<String, Map<String, Set<Vector3i>>> getGlobalLights() {
        return globalLights;
    }
    public Map<String, Set<String>> getGlobalIdNetworks() {
        return globalIdNetworks;
    }

    public void setGlobalNetwork(String uuID, Map<String, Vector3i> switches, Map<String, Set<Vector3i>> lights, Set<String> idNetworks) {
        globalSwitches.put(uuID, switches);
        globalLights.put(uuID, lights);
        globalIdNetworks.put(uuID, idNetworks);
    }
    public void clearGlobalNetwork(String uuID) {
        globalSwitches.remove(uuID);
        globalLights.remove(uuID);
        globalIdNetworks.remove(uuID);
    }

    public boolean containsGlobalSwitchValue(Vector3i targetBlock) {
        for(Map<String, Vector3i> switchLocal : globalSwitches.values()){
            if(switchLocal.containsValue(targetBlock)) {
                return true;
            }
        }
        return false;
    }

    public String getIdforSwitchGlobal(Vector3i posSwitch) {
        for(String id: globalSwitches.keySet()){
            Map<String, Vector3i> switchLocal = globalSwitches.get(id);

            for(String idNetwork: switchLocal.keySet()) {
                if(switchLocal.get(idNetwork).equals(posSwitch)) {
                    return idNetwork;
                }
            }
        }
        return null;
    }

    public Set<Vector3i> getLightsForID(String idNetwork) {
        for(Map<String, Set<Vector3i>> lightnetworks : globalLights.values()){
            if (lightnetworks.containsKey(idNetwork)) {
                return lightnetworks.get(idNetwork);
            }
        }
        return null;
    }
}
