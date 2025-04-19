package com.jomlom.recipebookaccess.mixin.client;

import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.common.transfer.TransferOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(RecipeTransferUtil.class)
public class RecipeTransferUtilMixin {
    @Inject(
            method = "validateSlots",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onValidateSlots(
            PlayerEntity player,
            Collection<TransferOperation> transferOperations,
            Collection<Slot> craftingSlots,
            Collection<Slot> inventorySlots,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // bypass JEI's slot validation
        cir.setReturnValue(true);
    }
}
