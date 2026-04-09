package com.danoru.watcher;
//ACOMODAR
import com.danoru.ui.EmptyHUI;
import com.danoru.ui.IndicationEHUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerHotbarWatcher implements PlayerPacketWatcher {
    @Override
    public void accept(PlayerRef playerRef, Packet packet) {
        if(packet instanceof SyncInteractionChains syncChain){
            for(SyncInteractionChain chain : syncChain.updates) {
                if(chain.interactionType == InteractionType.SwapFrom && chain.data != null) {
                    short before = (short) chain.activeHotbarSlot;
                    short current = (short) chain.data.targetSlot;

                    World world = Universe.get().getWorld(playerRef.getWorldUuid());
                    if(world == null) return;

                    world.execute(() -> {
                        Ref<EntityStore> ref = playerRef.getReference();
                        Player player = ref.getStore().getComponent(ref, Player.getComponentType());

                        ItemStack currentItem = player.getInventory().getHotbar().getItemStack(current);
                        ItemStack beforeItem = player.getInventory().getHotbar().getItemStack(before);

                        if(currentItem != null && currentItem.getItemId().equalsIgnoreCase("Controller")){
                            player.getHudManager().setCustomHud(playerRef, new IndicationEHUI(playerRef));
                        } else {
                            if(beforeItem != null && beforeItem.getItemId().equalsIgnoreCase("Controller")){
                                player.getHudManager().setCustomHud(playerRef, new EmptyHUI(playerRef));
                            }
                        }

                    });
                }
            }
        }

    }
}
