package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import com.jomlom.recipebookaccess.network.RequestItemsPayload;
import com.jomlom.recipebookaccess.util.VirtualInventory;
import com.jomlom.recipebookaccess.util.VirtualSlot;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = BasicRecipeTransferHandler.class, remap = false)
public class BasicRecipeTransferHandlerMixin {

    @Shadow @Final private IStackHelper stackHelper;
    @Shadow @Final private IRecipeTransferInfo<?, ?> transferInfo;

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
        ClientPlayNetworking.send(new RequestItemsPayload(1));
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
                    Slot virtualSlot = new VirtualSlot(virtInv, i);
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

    @Inject(
            method = "transferRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private <C extends ScreenHandler, R> void onTransferRecipe(
            C container, R recipe, IRecipeSlotsView recipeSlotsView, PlayerEntity player, boolean maxTransfer, boolean doTransfer, CallbackInfoReturnable<IRecipeTransferError> cir
    ) {

        if (!doTransfer) return;


        @SuppressWarnings("unchecked")
        IRecipeTransferInfo<ScreenHandler, Object> castedInfo = (IRecipeTransferInfo<ScreenHandler, Object>) transferInfo;

        List<Slot> craftingSlots = Collections.unmodifiableList(castedInfo.getRecipeSlots(container, recipe));
        List<Slot> inventorySlots = Collections.unmodifiableList(castedInfo.getInventorySlots(container, recipe));
        List<IRecipeSlotView> inputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);

        BasicRecipeTransferHandler.InventoryState invState = BasicRecipeTransferHandler.getInventoryState(
                craftingSlots, inventorySlots, player, container, castedInfo
        );

        if (invState == null) return;

        RecipeTransferOperationsResult transferOps = RecipeTransferUtil.getRecipeTransferOperations(
                stackHelper, invState.availableItemStacks(), inputSlots, craftingSlots
        );

        // detect any arbitrary slots by slot ID
        boolean hasVirtual = transferOps.results.stream()
                .anyMatch(op -> op.inventorySlotId() < 0 || op.craftingSlotId() < 0);

        if (hasVirtual) {
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.interactionManager.clickRecipe(
                        mc.player.currentScreenHandler.syncId,
                        (RecipeEntry<?>) recipe,
                        maxTransfer
                );
                cir.setReturnValue(null); // cancel JEI transfer
            } catch (Throwable t) {
                System.err.println("[NearbyJEI] Fallback failed, falling back to normal JEI logic:");
                t.printStackTrace();
                // continue with JEI if anything goes wrong
            }
        }
    }

}
