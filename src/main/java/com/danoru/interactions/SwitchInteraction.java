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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Set;

public class SwitchInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<SwitchInteraction> CODEC = BuilderCodec.builder(SwitchInteraction.class, SwitchInteraction::new, SimpleBlockInteraction.CODEC).build();
    @Override
    protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl Vector3i targetBlock, @NonNullDecl CooldownHandler cooldownHandler) {
        BlockType blockType = world.getBlockType(targetBlock);
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();
        Player player = store.getComponent(ref, Player.getComponentType());

        Config<LightsConfig> myconfig = ControlledLights.instance.getConfig();
        LightsConfig config = myconfig.get();

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());

        if(networksComponent == null) return;

        if(blockType.getItem().getId().startsWith("Switch_")){

            if(networksComponent.getLocalSwitches().containsValue(targetBlock) ){
                String keyNetwork = networksComponent.getIdForSwitch(targetBlock);
                Set<Vector3i> lights = networksComponent.getLocalLights().get(keyNetwork);
                switchLights(lights, world);
            } else if(config.containsGlobalSwitchValue(targetBlock)) {
                player.sendMessage(Message.raw("PASE POR AQUI"));
                String keyNetwork = config.getIdforSwitchGlobal(targetBlock);
                Set<Vector3i> lights = config.getLightsForID(keyNetwork);
                switchLights(lights, world);
            }
        }
    }

    private void switchLights(Set<Vector3i> lights, World world) {
        for(Vector3i pos : lights) {
            BlockType blockType1 = world.getBlockType(pos);
            if(blockType1.getStateForBlock(blockType1) == null) {
                world.setBlockInteractionState(pos, blockType1, "Off");
            } else if(blockType1.getStateForBlock(blockType1).equalsIgnoreCase("on")) {
                world.setBlockInteractionState(pos, blockType1, "Off");
            } else if(blockType1.getState().getBlockForState("On") == null && blockType1.getState().getBlockForState("Off") != null) {
                world.setBlockInteractionState(pos, blockType1, StateData.NULL_STATE_ID);
            } else if(blockType1.getStateForBlock(blockType1).equalsIgnoreCase("off")) {
                world.setBlockInteractionState(pos, blockType1, "On");
            }
        }
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {

    }
}
