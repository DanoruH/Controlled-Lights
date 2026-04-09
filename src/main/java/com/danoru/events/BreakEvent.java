package com.danoru.events;

import com.danoru.components.NetworksComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class BreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    public BreakEvent() {
        super(BreakBlockEvent.class);
    }
    private final String SOUND_DISCONNECT = "SFX_Disconnect";

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl BreakBlockEvent breakBlockEvent) {
        Ref<EntityStore> reference = archetypeChunk.getReferenceTo(i);
        Player player = store.getComponent(reference, Player.getComponentType());

        BlockType blockType = breakBlockEvent.getBlockType();
        Vector3i targetBlock = breakBlockEvent.getTargetBlock();

        NetworksComponent networksComponent = (NetworksComponent) store.getComponent(reference, NetworksComponent.getComponentType());

        if(networksComponent == null) return;
        if(blockType.getItem() == null) return;

        if(blockType.getItem().getId().equalsIgnoreCase("Switch_Light")){
            if(networksComponent.getLocalSwitches().containsValue(targetBlock)){
                player.sendMessage(Message.raw(networksComponent.toString()));
                networksComponent.removeNetwork(networksComponent.getIdForSwitch(targetBlock));
                SoundUtil.playSoundEvent2d(SoundEvent.getAssetMap().getIndex(SOUND_DISCONNECT), SoundCategory.SFX, commandBuffer);
                player.sendMessage(Message.raw("Broken, unliked"));
            }
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
