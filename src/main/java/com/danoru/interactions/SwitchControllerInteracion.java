package com.danoru.interactions;

import com.danoru.components.NetworksComponent;
import com.danoru.ui.SubmitUI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.HashSet;
import java.util.UUID;

public class SwitchControllerInteracion extends SimpleInstantInteraction {

    public static final BuilderCodec<SwitchControllerInteracion> CODEC;

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        if(networksComponent == null) return;

        ItemStack itemStack = interactionContext.getHeldItem();
        ControllerInteraction.itemStackOrigin = itemStack;
        ControllerInteraction.metadataOrigin = itemStack.getMetadata();

        if(networksComponent.isModeCreate()) {
            //LÓGICA DE CONTROLLER
            if(itemStack.getItem().getId().startsWith("Switch_") && itemStack != null){
                if(ControllerInteraction.metadataOrigin == null) {
                    ControllerInteraction.metadataOrigin = new BsonDocument();
                }
                if(ControllerInteraction.metadataOrigin.isEmpty() || !networksComponent.containsuuIdMetadata(ControllerInteraction.metadataOrigin)) {
                    if(!ControllerInteraction.lightsRaw.isEmpty()) {
                        ControllerInteraction.isCreate_Block = false;
                        player.getPageManager().openCustomPage(ref, store, new SubmitUI(playerRef));
                    } else {
                        SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(ControllerInteraction.SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                        player.sendMessage(Message.raw("You didn't link any lights."));
                    }
                } else {
                    SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(ControllerInteraction.SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                    player.sendMessage(Message.raw("It's already used"));
                }
            }
        }
    }

    static {
        CODEC = BuilderCodec.builder(SwitchControllerInteracion.class, SwitchControllerInteracion::new, SimpleInstantInteraction.CODEC).build();
    }
}
