package in.northwestw.fissionrecipe.mixin;

import in.northwestw.fissionrecipe.jei.RecipeViewerRecipeTypes;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GuiThermoelectricBoiler.class, remap = false)
public class MixinGuiThermoelectricBoiler {
    @ModifyArg(method = "addGuiElements", at = @At(value = "INVOKE", target = "Lmekanism/client/gui/element/GuiInnerScreen;recipeViewerCategories([Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;)Lmekanism/client/gui/element/GuiInnerScreen;"))
    public @NotNull IRecipeViewerRecipeType<?>[] changeRecipeType(@NotNull IRecipeViewerRecipeType<?>[] recipeCategories) {
        return new IRecipeViewerRecipeType<?>[] {RecipeViewerRecipeTypes.BOILER};
    }
}
