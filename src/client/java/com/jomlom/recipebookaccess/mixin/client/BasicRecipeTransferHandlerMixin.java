package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.util.VirtualInventory;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(BasicRecipeTransferHandler.class)
public class BasicRecipeTransferHandlerMixin {

    @Inject(
            method = "getInventoryState(Ljava/util/Collection;Ljava/util/Collection;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/screen/ScreenHandler;Lmezz/jei/api/recipe/transfer/IRecipeTransferInfo;)Lmezz/jei/library/transfer/BasicRecipeTransferHandler$InventoryState;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static <C extends ScreenHandler, R> void onGetInventoryState(
            Collection<Slot> craftingSlots,
            Collection<Slot> inventorySlots,
            PlayerEntity player,
            C container,
            IRecipeTransferInfo<C, R> transferInfo,
            CallbackInfoReturnable<BasicRecipeTransferHandler.InventoryState> cir
    ) {

        PacketByteBuf buf = PacketByteBufs.create();
        RequestItemsPayload payload = new RequestItemsPayload(1);
        RequestItemsPayload.encode(payload, buf);
        ClientPlayNetworking.send(RequestItemsPayload.ID, buf);

        BasicRecipeTransferHandler.InventoryState original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        try {
            List<ItemStack> snapshot = ClientItemsReciever.getItemStacks();
            if (snapshot.isEmpty()) {
                return;
            }

            Inventory virtInv = new VirtualInventory(snapshot);

            Map<Slot, ItemStack> newMap = new HashMap<>();
            int added = 0;
            for (int i = 0; i < virtInv.size(); i++) {
                ItemStack s = virtInv.getStack(i);
                if (!s.isEmpty()) {
                    Slot virtualSlot = new Slot(virtInv, i, -1, -1);
                    newMap.put(virtualSlot, s.copy());
                    added++;
                }
            }

            cir.setReturnValue(new BasicRecipeTransferHandler.InventoryState(
                    newMap,
                    original.filledCraftSlotCount(),
                    original.emptySlotCount()
            ));
            System.out.println("[NearbyJEI] Overrode JEI client inventory, added " + added + " stacks");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}