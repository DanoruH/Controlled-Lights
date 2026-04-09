package com.danoru.interactions;
//ACOMODADO @
import com.danoru.components.NetworksComponent;
import com.danoru.ui.UIGallery;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class InterfaceInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<InterfaceInteraction> CODEC =
            BuilderCodec.builder(InterfaceInteraction.class, InterfaceInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = interactionContext.getEntity().getStore();

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if(playerRef == null) return;

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        if(networksComponent == null) return;

        if(!networksComponent.isModeCreate() && !networksComponent.isModeEdit()) {
            UIGallery page = new UIGallery(playerRef);
            player.getPageManager().openCustomPage(ref, store, page);
        }
    }
}
