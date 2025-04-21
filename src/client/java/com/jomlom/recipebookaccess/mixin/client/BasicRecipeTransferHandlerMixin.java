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
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = BasicRecipeTransferHandler.class, remap = false)
public abstract class BasicRecipeTransferHandlerMixin {

    @Final @Shadow private IConnectionToServer serverConnection;
    @Final @Shadow private IStackHelper stackHelper;
    @Final @Shadow private IRecipeTransferHandlerHelper handlerHelper;
    @Final @Shadow private IRecipeTransferInfo<?, ?> transferInfo;

    @Inject(
            method = "transferRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private <C extends ScreenHandler, R> void onTransferRecipe(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            PlayerEntity player,
            boolean maxTransfer,
            boolean doTransfer,
            CallbackInfoReturnable<IRecipeTransferError> cir
    ) {

        PacketByteBuf buf = PacketByteBufs.create();
        RequestItemsPayload.encode(new RequestItemsPayload(1), buf);
        ClientPlayNetworking.send(RequestItemsPayload.ID, buf);

        if (!serverConnection.isJeiOnServer()) {
            Text tooltipMessage = Text.translatable("jei.tooltip.error.recipe.transfer.no.server");
            cir.setReturnValue(this.handlerHelper.createUserErrorWithTooltip(tooltipMessage));
            return;
        }

        IRecipeTransferInfo<ScreenHandler, Object> castedInfo = (IRecipeTransferInfo<ScreenHandler, Object>) transferInfo;

        if (!castedInfo.canHandle(container, recipe)) {
            IRecipeTransferError error = castedInfo.getHandlingError(container, recipe);
            cir.setReturnValue(error != null
                    ? error
                    : this.handlerHelper.createInternalError()
            );
            return;
        }

        List<Slot> craftingSlots = Collections.unmodifiableList(castedInfo.getRecipeSlots(container, recipe));
        List<Slot> inventorySlots = Collections.unmodifiableList(castedInfo.getInventorySlots(container, recipe));
        List<IRecipeSlotView> inputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);

        List<ItemStack> snapshot = ClientItemsReciever.getItemStacks();
        if (snapshot == null || snapshot.isEmpty()) {
            cir.setReturnValue(this.handlerHelper.createInternalError());
            return;
        }

        Inventory virtInv = new VirtualInventory(snapshot);
        Map<Slot, ItemStack> virtualMap = new HashMap<>();
        for (int i = 0; i < virtInv.size(); i++) {
            ItemStack s = virtInv.getStack(i);
            if (!s.isEmpty()) {
                Slot virtualSlot = new VirtualSlot(virtInv, i);
                virtualMap.put(virtualSlot, s.copy());
            }
        }

        BasicRecipeTransferHandler.InventoryState virtualState = new BasicRecipeTransferHandler.InventoryState(
                virtualMap,
                0,
                999
        );

        if (!virtualState.hasRoom(inputSlots.size())) {
            Text message = Text.translatable("jei.tooltip.error.recipe.transfer.inventory.full");
            cir.setReturnValue(this.handlerHelper.createUserErrorWithTooltip(message));
            return;
        }

        RecipeTransferOperationsResult transferOps = RecipeTransferUtil.getRecipeTransferOperations(
                stackHelper, virtualState.availableItemStacks(), inputSlots, craftingSlots
        );

        if (!transferOps.missingItems.isEmpty()) {
            Text message = Text.translatable("jei.tooltip.error.recipe.transfer.missing");
            cir.setReturnValue(this.handlerHelper.createUserErrorForMissingSlots(message, transferOps.missingItems));
            return;
        }

        if (!RecipeTransferUtil.validateSlots(player, transferOps.results, craftingSlots, inventorySlots)) {
            cir.setReturnValue(this.handlerHelper.createInternalError());
            return;
        }

        if (doTransfer){
            try {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.interactionManager.clickRecipe(
                        mc.player.currentScreenHandler.syncId,
                        (net.minecraft.recipe.Recipe<?>) recipe,
                        maxTransfer
                );
                // cancel JEI transfer if all goes well
            } catch (Throwable t) {
                System.err.println("[NearbyJEI] Fallback failed, falling back to normal JEI logic:");
                t.printStackTrace();
                // continue with JEI if anything goes wrong
            }
        }

        cir.setReturnValue(null);
    }

}