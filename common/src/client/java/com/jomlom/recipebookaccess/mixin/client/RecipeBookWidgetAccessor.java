package com.jomlom.recipebookaccess.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookWidgetAccessor {
    @Accessor("menu")
    RecipeBookMenu<?> getCraftingScreenHandler();
}
