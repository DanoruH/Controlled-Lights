package com.danoru.interactions;

import com.danoru.ControlledLights;
import com.danoru.components.NetworksComponent;
import com.danoru.config.LightsConfig;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Set;

public class SwitchWirelessInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<SwitchWirelessInteraction> CODEC = BuilderCodec.builder(SwitchWirelessInteraction.class, SwitchWirelessInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();
        World world = commandBuffer.getExternalData().getWorld();
        Player player = store.getComponent(ref, Player.getComponentType());
        ItemStack itemStack = interactionContext.getHeldItem();

        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        if(networksComponent == null) return;

        if(itemStack.getItem().getId().startsWith("Switch_") && itemStack != null){
            if(networksComponent.containsuuIdMetadata(itemStack.getMetadata())) {
                String id = networksComponent.getIdForSwitch(itemStack.getMetadata());
                Set<Vector3i> lights = networksComponent.getNetworkForId(id).getLights();
                SwitchInteraction.switchLights(lights, world);
            } else if(config.containsGlobalSwitchValue(itemStack.getMetadata())) {
                String id = config.getIdforSwitchLocal(itemStack.getMetadata());
                if(id != null) {
                    Set<Vector3i> lights = config.getLightsForID(id);
                    SwitchInteraction.switchLights(lights, world);
                }

            }
        }
    }
}
