package com.danoru.ui;
//ACOMODAR
import com.danoru.components.NetworksComponent;
import com.danoru.interactions.ControllerInteraction;
import com.danoru.interactions.SwitchControllerInteracion;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class SubmitUI extends InteractiveCustomUIPage<SubmitUI.SubmitUIEventData> {

    public static boolean is_correct = true;

    public SubmitUI(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, SubmitUIEventData.CODEC);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());

        uiCommandBuilder.append("HUI/CreateID.ui");
        uiCommandBuilder.set("#idInput.PlaceholderText", "Enter an ID or by default it will be: " + String.valueOf(networksComponent.getIdDefault()));

        String message1 = "Examples of IDs you can use:";
        String message2 = "Network11, netwoK, 12, N3tW0rK";
        uiCommandBuilder.set("#Message1.Text", message1);
        uiCommandBuilder.set("#Message2.Text", message2);
        uiCommandBuilder.set("#Message1.Style.TextColor", "#f9fff7");
        uiCommandBuilder.set("#Message2.Style.TextColor", "#f9fff7");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#SubmitID",
                new EventData().append("@TextContent", "#idInput.Value"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SubmitUIEventData data) {
        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        UICommandBuilder cmd = new UICommandBuilder();
        String id = data.textContent != null ? data.textContent : "";
        Player player = store.getComponent(ref, Player.getComponentType());

        //CREA LA RED
        if(networksComponent.isModeCreate()){
            if(!networksComponent.getIdNetworks().contains(id)){
                //VERIFICA SI PUSO UN ID O NO Y LO CREA
                if(id.equalsIgnoreCase("")){
                    String idDef = networksComponent.consumeIDdefault();
                    ControllerInteraction.create_Network(idDef);
                } else  {
                    ControllerInteraction.create_Network(id);
                }

                //DEFINE EL MODO CREATE EN FALSO Y REDIRIGE AL MENU PRINCIPAL
                networksComponent.setModeCreate(false);
                player.getPageManager().openCustomPage(ref, store, new UIGallery(playerRef));

//                player.sendMessage(Message.raw("IDs: " + networksComponent.getIdNetworks()));
//                player.sendMessage(Message.raw("Switchs: " + networksComponent.getLocalSwitches()));
//                player.sendMessage(Message.raw("Lights: " + networksComponent.getLocalLights()));
            } else {
                String message2 = "Invalid ID: This ID already exists.";
                cmd.set("#Message1.Text", "");
                cmd.set("#Message2.Text", message2);
                cmd.set("#Message2.Style.TextColor", "#ef4444");
            }

            this.sendUpdate(cmd, null, false);
        }

    }

    public static class SubmitUIEventData {
        String textContent;

        public static final BuilderCodec<SubmitUIEventData> CODEC =
                BuilderCodec.builder(SubmitUIEventData.class, SubmitUIEventData::new)
                        .append(new KeyedCodec<>("@TextContent", Codec.STRING),
                                (data, value) -> data.textContent = value,
                                data -> data.textContent).add()
                        .build();
    }
}
