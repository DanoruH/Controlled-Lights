package com.danoru.interactions;

import com.danoru.components.NetworksComponent;
import com.danoru.ui.SubmitUI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ControllerInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<ControllerInteraction> CODEC =
            BuilderCodec.builder(ControllerInteraction.class, ControllerInteraction::new, SimpleBlockInteraction.CODEC).build();

    public static Set<Vector3i> lightsRaw = new HashSet<>();
    public static HashMap<Vector3i, UUIDComponent> uid_models = new HashMap<>();
    public static Set<Ref<EntityStore>> models = new HashSet<>();

    public static final String SOUND_SELECT = "SFX_SelectLight";
    public static final String SOUND_LINKED = "SFX_LinkedBlock";
    public static final String SOUND_ERROR = "SFX_Error";

    public static Vector3i blockPosition;
    public static CommandBuffer<EntityStore> commandBufferOrigin;
    public static Store<EntityStore> storeOrigin;
    public static NetworksComponent networksComponentOrigin;

    @Override
    protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl Vector3i target_block, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        BlockType blockType = world.getBlockType(target_block);
        if(blockType == null) return;

        //INVOCACIÓN DE MODELO VISUAL
        Holder<EntityStore> vmodel = create_Vmodel(target_block, commandBuffer);
        UUIDComponent uuidComponent = vmodel.ensureAndGetComponent(UUIDComponent.getComponentType());

        //INSTANCIA COMPONENT PRINCIPAL
        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        if(networksComponent == null) return;

        //LÓGICA CONDICIONAL (PROX)
        if(networksComponent.isModeCreate()) {
            //LÓGICA DE CONTROLLER
            if(blockType.getItem().getId().startsWith("Switch_")){
                if(!networksComponent.getLocalSwitches().containsValue(target_block)){
                    if(!lightsRaw.isEmpty()) {
                        //PROCESO DE ENLAZAMIENTO Y ENCAPSULAMIENTO
                        blockPosition = target_block;
                        commandBufferOrigin = commandBuffer;
                        networksComponentOrigin = networksComponent;
                        storeOrigin = store;
                        player.getPageManager().openCustomPage(ref, store, new SubmitUI(playerRef));
//                        create_Network(networksComponent.consumeIDdefault());
                    } else {
                        SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                        player.sendMessage(Message.raw("You didn't link any lights."));
                    }
                } else {
                    SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(SOUND_ERROR), SoundCategory.SFX, commandBuffer);
                    player.sendMessage(Message.raw("It's already used"));
                }
                return;
            }

            //LÓGICA DE LA SELECCIÓN DE DE SWITCH
            if(blockType.getState() == null) return;

            if(blockType.getState().getBlockForState("On") != null || blockType.getState().getBlockForState("Off") != null) {
                if (!lightsRaw.contains(target_block)) {
                    //PROCESO DE AÑADIDO A LA LISTA CRUDA.
                    lightsRaw.add(target_block);
                    uid_models.put(target_block, uuidComponent);
                    commandBuffer.run((o) -> models.add(commandBuffer.addEntity(vmodel, AddReason.SPAWN)));
                    SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(SOUND_SELECT), SoundCategory.SFX, commandBuffer);
                } else {
                    //REMOVIENDO EL BLOQUE VISUAL "SELECTED"
                    Ref<EntityStore> ref1 = getEntityModel(target_block);
                    uid_models.remove(target_block);
                    lightsRaw.remove(target_block);
                    commandBuffer.run((o) -> commandBuffer.removeEntity(ref1, RemoveReason.REMOVE));
                    models.remove(ref1);
                }
            }
        }
    }


    //CREACIÓN DE MODELO VISUAL "SELECTED"
    private Holder<EntityStore> create_Vmodel(Vector3i target_block, CommandBuffer<EntityStore> commandBuffer) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("BlockSelected");
        Model model = Model.createScaledModel(modelAsset, 2.0f);
        Vector3d vector_pos = Vector3d.add(target_block.toVector3d(),new Vector3d(0.5, 0, 0.5));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(vector_pos, new Vector3f(0.0f, 0.0f, 0.0f)));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(2.0f));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(commandBuffer.getExternalData().takeNextNetworkId()));

        return holder;
    }

    //SACAR REFERENCIA DEL EFECTO VISUAL
    public static Ref<EntityStore> getEntityModel(Vector3i targetBlock) {
        for (Ref<EntityStore> model : models) {
            Store<EntityStore> store = model.getStore();
            UUIDComponent uidcomponent = store.getComponent(model, UUIDComponent.getComponentType());

            if(uidcomponent.equals(uid_models.get(targetBlock))) {
                Ref<EntityStore> modelAux = model;
                models.remove(model);
                return modelAux;
            }
        }
        return null;
    }

    public static void create_Network(String id) {
        networksComponentOrigin.setNetwork(id, new HashSet<>(lightsRaw), blockPosition);
        lightsRaw.clear();
        for (Ref<EntityStore> entities : models) {
            commandBufferOrigin.run((o) -> commandBufferOrigin.removeEntity(entities, RemoveReason.REMOVE));
        }
        uid_models.clear();
        models.clear();
        SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(SOUND_LINKED), SoundCategory.SFX, commandBufferOrigin);
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {

    }
}
