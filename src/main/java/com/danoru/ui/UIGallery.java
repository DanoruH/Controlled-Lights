package com.danoru.ui;

import com.danoru.components.NetworksComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

//ACOMODAR
public class UIGallery extends InteractiveCustomUIPage<UIGallery.UIGalleryEventData> {
    String selected;
    String lastSelected = null;

    public UIGallery(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, UIGalleryEventData.CODEC);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder, @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("HUI/LightSystem.ui");
        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());

        update(networksComponent, uiCommandBuilder, uiEventBuilder);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#NetworkAdd",
                new EventData().append("Action", "add"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#NetworkRemoveAll",
                new  EventData().append("Action", "removeAll"));
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                "#NetworkRemove",
                new EventData().append("Action", "remove"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UIGalleryEventData data) {
        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(ref, NetworksComponent.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        UICommandBuilder cmd = new UICommandBuilder();
        UIEventBuilder cmdEvent = new UIEventBuilder();

        //Ver que Network seleccionaste
        for(int i = 0; i < networksComponent.getNetworks().size(); i++) {
            if(("Select" + i).equals(data.action)) {
                selected = String.valueOf(i);
                cmd.set("#NetworkList[" + selected + "] #SelectedButton.Disabled", true);
                if(!selected.equals(lastSelected)) {
                    if(lastSelected != null) {
                        cmd.set("#NetworkList[" + lastSelected + "] #SelectedButton.Disabled", false);
                        lastSelected = selected;
                    } else {
                        lastSelected = selected;
                    }
                }
            }
        }

        if("add".equals(data.action)) {
            networksComponent.setModeCreate(true);
            player.getPageManager().setPage(ref, store, Page.None);
        }

        if("removeAll".equals(data.action)) {
            networksComponent.removeAllNetworks();
            cmd.clear("#NetworkList");
        }

        if("remove".equals(data.action)) {
            if(selected != null) {
                networksComponent.removeNetwork(networksComponent.getIdForIndex(selected));
                player.getPageManager().setPage(ref, store, Page.None);
            }
        }

        updateInfo(selected, cmd, networksComponent, store);
        this.sendUpdate(cmd, null, false);
    }

    private void update(NetworksComponent networksComponent, UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        int index = 0;
        if(!networksComponent.getNetworks().isEmpty()) {
            for(var networks : networksComponent.getNetworks()) {
                uiCommandBuilder.append("#NetworkList", "HUI/NetworkEntry.ui");

                uiCommandBuilder.set("#NetworkList[" + index + "] #NetworkLabel.Text", "Network: " + networks.getId());
                uiCommandBuilder.set("#NetworkList[" + index + "] #NetworkIndex.Text", "Index: " + index);

                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                        "#NetworkList[" + index + "] #SelectedButton",
                        new EventData().append("Action", "Select" + index));

                index++;
            }
        }
    }

    private void updateInfo(String selected, UICommandBuilder uiCommandBuilder, NetworksComponent networksComponent, Store<EntityStore> store) {
        if(selected != null) {
            World world = store.getExternalData().getWorld();
            String id = networksComponent.getIdForIndex(selected);
            //INFO TOTAL LIGHTS
            int total = networksComponent.getNetworkForId(id).getLights().size();
            uiCommandBuilder.set("#Total.Text", "TOTAL: " + total);

            //INFO ICON SWITCH
            uiCommandBuilder.set("#Icon.ItemId", networksComponent.getNetworkForId(id).getIdItem());
        }

    }



    public static class UIGalleryEventData {
        private String action;
        private String selected;

        public static final BuilderCodec<UIGalleryEventData> CODEC =
                BuilderCodec.builder(UIGalleryEventData.class, UIGalleryEventData::new)
                        .append(new KeyedCodec<>("Action", Codec.STRING),
                                (data, value) -> data.action = value,
                                data -> data.action).add()
                        .append(new KeyedCodec<>("Selected", Codec.STRING),
                                (data, value) -> data.selected = value,
                                data -> data.selected).add()
                        .build();
    }
}
