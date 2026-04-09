package com.danoru;

import com.danoru.components.NetworksComponent;
import com.danoru.config.LightsConfig;
import com.danoru.events.BreakEvent;
import com.danoru.interactions.ControllerInteraction;
import com.danoru.interactions.InterfaceInteraction;
import com.danoru.interactions.SwitchInteraction;
import com.danoru.systems.PlayerReady;
import com.danoru.watcher.PlayerHotbarWatcher;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;

public class ControlledLights extends JavaPlugin {
    public static ControlledLights instance;
    public ComponentType networksComponentType;
    private PacketFilter packetFilter;
    private final Config<LightsConfig> config = this.withConfig("LightsConfig", LightsConfig.CODEC);
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ControlledLights(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        instance = this;
    }

    @Override
    protected void setup() {
        this.config.save();
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.networksComponentType = this.getEntityStoreRegistry().registerComponent(NetworksComponent.class, "NetworksComponent", NetworksComponent.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ControllerLights", ControllerInteraction.class, ControllerInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("SwitchLights", SwitchInteraction.class, SwitchInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("InterfaceLights", InterfaceInteraction.class, InterfaceInteraction.CODEC);
        this.getEntityStoreRegistry().registerSystem(new PlayerReady(config));
        this.getEntityStoreRegistry().registerSystem(new BreakEvent());

        this.packetFilter = PacketAdapters.registerInbound(new PlayerHotbarWatcher());
    }

    @Override
    protected void shutdown() {
        if(this.packetFilter != null) {
            PacketAdapters.deregisterInbound(this.packetFilter);
        }
    }

    public static ComponentType getNetworksComponentType() {
        return instance.networksComponentType;
    }
    public Config<LightsConfig> getConfig() {
        return config;
    }
}