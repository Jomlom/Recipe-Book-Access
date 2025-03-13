package com.jomlom.recipebookaccess.mixin.client;

import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.util.RecipeBookAccessUtils;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.lang.reflect.Field;

@Mixin(RecipeBookWidget.class)
public abstract class RecipeBookWidgetMixin {

	@Redirect(
			method = "refreshInputs",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/PlayerInventory;populateRecipeFinder(Lnet/minecraft/recipe/RecipeFinder;)V"
			)
	)
	private void redirectPopulateRecipeFinder(PlayerInventory inventory, RecipeFinder recipeFinder) {
		RecipeBookWidget<?> widget = (RecipeBookWidget<?>)(Object)this;
		try {
			Field field = RecipeBookWidget.class.getDeclaredField("craftingScreenHandler");
			field.setAccessible(true);
			AbstractRecipeScreenHandler handler =
					(AbstractRecipeScreenHandler) field.get(widget);
			if (handler instanceof RecipeBookInventoryProvider customPopulator) {
				RecipeBookAccessUtils.populateCustomRecipeFinder(recipeFinder, customPopulator);
			} else {
				inventory.populateRecipeFinder(recipeFinder);
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			inventory.populateRecipeFinder(recipeFinder);
		}
	}

}
