package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Inject(
            method = "onClosed(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD")
    )
    private void onClosedInject(PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (handler instanceof CraftingScreenHandler screenHandler && screenHandler instanceof RecipeBookInventoryProvider customPop) {
            if (!customPop.persistentInventory()) {
                for (Slot slot : screenHandler.slots.subList(
                        customPop.inputSlotsStartIndex(),
                        customPop.inputSlotsEndIndex()
                )
                ) {
                    ItemStack stack = slot.getStack().copy();
                    boolean returned = RecipeBookAccessUtils.tryReturnItemToOrigin(slot, stack);
                    if (!returned) {
                        player.getInventory().offer(stack, false);
                    }
                    slot.setStackNoCallbacks(stack);
                }
            }
        }
    }
}