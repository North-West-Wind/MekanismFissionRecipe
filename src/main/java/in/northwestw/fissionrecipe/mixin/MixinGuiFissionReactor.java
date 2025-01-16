package in.northwestw.fissionrecipe.mixin;

import in.northwestw.fissionrecipe.jei.FissionRecipeViewType;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.generators.client.gui.GuiFissionReactor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = GuiFissionReactor.class, remap = false)
public abstract class MixinGuiFissionReactor {
    @ModifyArg(method = "addGuiElements", at = @At(value = "INVOKE", target = "Lmekanism/client/gui/element/GuiInnerScreen;recipeViewerCategories([Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;)Lmekanism/client/gui/element/GuiInnerScreen;"))
    public @NotNull IRecipeViewerRecipeType<?>[] changeRecipeType(@NotNull IRecipeViewerRecipeType<?>[] recipeCategories) {
        return new IRecipeViewerRecipeType<?>[] {FissionRecipeViewType.FISSION};
    }
}
