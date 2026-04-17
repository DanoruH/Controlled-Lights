package com.danoru.codecs;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;

public class Network {
    public static final BuilderCodec<Network> CODEC;

    private String id = "";
    private Set<Vector3i> lights = new HashSet<>();
    private String idItem = "";
    private Vector3i switchBlock = new Vector3i();
    private String switchWireless = "";

    static {
        CODEC = BuilderCodec.builder(Network.class, Network::new)
                .append(new KeyedCodec<>("Id", Codec.STRING),
                        (data, value) -> data.id = value,
                        data -> data.id).add()
                .append(new KeyedCodec<>("Lights", new SetCodec<>(Vector3i.CODEC, HashSet::new, false)),
                        (data, value) -> data.lights = value,
                        data -> data.lights).add()
                .append(new KeyedCodec<>("IdItem", Codec.STRING),
                        (data, value) -> data.idItem = value,
                        data -> data.idItem).add()
                .append(new KeyedCodec<>("Switch_Block", Vector3i.CODEC),
                        (data, value) -> data.switchBlock = value,
                        data -> data.switchBlock).add()
                .append(new KeyedCodec<>("Switch_Wireless", Codec.STRING),
                        (data, value) -> data.switchWireless = value,
                        data -> data.switchWireless).add()
                .build();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Set<Vector3i> getLights() {
        return lights;
    }
    public void setLights(Set<Vector3i> lights) {
        this.lights = lights;
    }

    public String getIdItem() {
        return idItem;
    }
    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public Vector3i getSwitchBlock() {
        return switchBlock;
    }
    public void setSwitchBlock(Vector3i switchBlock) {
        this.switchBlock = switchBlock;
    }

    public String getSwitchWireless() {
        return switchWireless;
    }
    public void setSwitchWireless(String switchWireless) {
        this.switchWireless = switchWireless;
    }

    @Override
    public String toString() {
        return "Network{" +
                "id='" + id.toString() + '\'' +
                ", lights=" + lights.toString() +
                ", switchName='" + idItem.toString() + '\'' +
                ", switchBlock=" + switchBlock.toString() +
                ", switchWireless='" + switchWireless.toString() + '\'' +
                '}';
    }
}
