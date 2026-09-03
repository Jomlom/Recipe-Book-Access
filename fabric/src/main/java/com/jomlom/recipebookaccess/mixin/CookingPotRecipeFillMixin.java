package com.jomlom.recipebookaccess.mixin;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.List;
import java.util.Map;

@Mixin(CookingPotMenu.class)
public abstract class CookingPotRecipeFillMixin {

    @Redirect(
            method = "handlePlacement",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/recipebook/ServerPlaceRecipe;"
                            + "placeRecipe("
                            + "Lnet/minecraft/recipebook/ServerPlaceRecipe$CraftingMenuAccess;"
                            + "II"
                            + "Ljava/util/List;"
                            + "Ljava/util/List;"
                            + "Lnet/minecraft/world/entity/player/Inventory;"
                            + "Lnet/minecraft/world/item/crafting/RecipeHolder;"
                            + "ZZ"
                            + ")Lnet/minecraft/world/inventory/RecipeBookMenu$PostPlaceAction;"
            )
    )
    private RecipeBookMenu.PostPlaceAction redirectPlaceRecipe(
            ServerPlaceRecipe.CraftingMenuAccess menu,
            int gridWidth,
            int gridHeight,
            List<Slot> inputGridSlots,
            List<Slot> slotsToClear,
            Inventory inventory,
            RecipeHolder recipe,
            boolean useMaxItems,
            boolean isCreative
    ) {
        if (this instanceof RecipeBookInventoryProvider customPop) {
            RecipeBookAccessUtils.reconcileGridForRecipe(inputGridSlots, recipe, inventory.player);

            Map<Slot, Integer> beforeCounts = RecipeBookAccessUtils.snapshotGridCounts(inputGridSlots);

            RecipeBookAccessUtils.SyntheticInventory synthetic =
                    RecipeBookAccessUtils.buildSyntheticInventory(inventory.player, customPop);

            RecipeBookMenu.PostPlaceAction result = ServerPlaceRecipe.placeRecipe(
                    menu, gridWidth, gridHeight, inputGridSlots, slotsToClear, synthetic.inventory, recipe, useMaxItems, isCreative
            );

            RecipeBookAccessUtils.trackGridSlotOrigins(inputGridSlots, synthetic, beforeCounts);
            RecipeBookAccessUtils.reconcileSyntheticInventory(synthetic, customPop);
            return result;
        }

        return ServerPlaceRecipe.placeRecipe(
                menu, gridWidth, gridHeight, inputGridSlots, slotsToClear, inventory, recipe, useMaxItems, isCreative
        );
    }
}
