package com.danoru.interactions;

import com.danoru.components.NetworksComponent;
import com.danoru.ui.SubmitUI;
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
    public static Player player;
    public static BsonDocument metadata;
    public static ItemStack itemStack;
    public static String uuidSwitch;

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        if(networksComponent == null) return;

        ItemStack itemheld = interactionContext.getHeldItem();
        BsonDocument metadata = itemheld.getMetadata();


        if(networksComponent.isModeCreate()) {
            //LÓGICA DE CONTROLLER
            if(interactionContext.getHeldItem().getItemId().startsWith("Switch_")){
                if(metadata == null || metadata.isEmpty()) {
                    if(!ControllerInteraction.lightsRaw.isEmpty()) {
                        metadata = new BsonDocument();

                        //PROCESO DE ENLAZAMIENTO Y ENCAPSULAMIENTO
//                        blockPosition = "hola"; //remplazar
                        ControllerInteraction.commandBufferOrigin = commandBuffer;
                        ControllerInteraction.networksComponentOrigin = networksComponent;
                        ControllerInteraction.storeOrigin = store;
                        player.getPageManager().openCustomPage(ref, store, new SubmitUI(playerRef));
//                        create_Network(networksComponent.consumeIDdefault());
                    } else {
                        SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(ControllerInteraction.SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                        player.sendMessage(Message.raw("You didn't link any lights."));
                    }
                } else {
                    SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(ControllerInteraction.SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                    player.sendMessage(Message.raw("It's already used"));
                }
                return;
            }
        }
    }

    public static void createNetworkWithMetadata(String id) {
        uuidSwitch = UUID.randomUUID().toString();
        metadata.append("Uuid",  new BsonString(uuidSwitch));
        create_Network(id);
        //Reemplaza el controller con METADATA
        ItemStack modifiedController = itemStack.withMetadata(metadata);
        player.getInventory().getHotbar().setItemStackForSlot(player.getInventory().getActiveHotbarSlot(),  modifiedController);
    }

    public static void removeUUIDMetadata( String uuID) {
        ItemContainer itemContainer = player.getInventory().getCombinedHotbarFirst();
        short capacity = itemContainer.getCapacity();
        for(short slot = 0; slot < capacity; slot++){
            ItemStack item = itemContainer.getItemStack(slot);

            BsonDocument metadata = item.getMetadata();
            if(metadata != null || !metadata.isEmpty()) {
                String uuid = metadata.get("Uuid").asString().getValue();
                if(uuid.equals(uuID)) {
                    metadata.remove("Uuid");
                    break;
                }
            }
        }
    }

    private static void create_Network(String id) {
        ControllerInteraction.networksComponentOrigin.setNetwork(id, new HashSet<>(ControllerInteraction.lightsRaw), uuidSwitch);
        ControllerInteraction.lightsRaw.clear();
        for (Ref<EntityStore> entities : ControllerInteraction.models) {
            ControllerInteraction.commandBufferOrigin.run((o) -> ControllerInteraction.commandBufferOrigin.removeEntity(entities, RemoveReason.REMOVE));
        }
        ControllerInteraction.uid_models.clear();
        ControllerInteraction.models.clear();
        SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(ControllerInteraction.SOUND_LINKED), SoundCategory.SFX, ControllerInteraction.commandBufferOrigin);
    }
}
