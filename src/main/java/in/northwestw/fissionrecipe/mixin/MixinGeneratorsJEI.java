package in.northwestw.fissionrecipe.mixin;

import com.google.common.collect.Lists;
import in.northwestw.fissionrecipe.jei.FissionJEIRecipe;
import in.northwestw.fissionrecipe.jei.FissionRecipeViewType;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.client.recipe_viewer.jei.CatalystRegistryHelper;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.generators.client.recipe_viewer.jei.GeneratorsJEI;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.jei.FissionReactorRecipeCategory;
import in.northwestw.fissionrecipe.recipe.FissionRecipe;
import in.northwestw.fissionrecipe.recipe.FluidCoolantRecipe;
import in.northwestw.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GeneratorsJEI.class, remap = false)
public class MixinGeneratorsJEI {
    // override mekanism hard-coded recipes with ours
    @Inject(method = "registerRecipes", at = @At(value = "INVOKE", target = "Lmekanism/client/recipe_viewer/jei/RecipeRegistryHelper;register(Lmezz/jei/api/registration/IRecipeRegistration;Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;Ljava/util/List;)V"), cancellable = true)
    public void registerFissionRecipes(IRecipeRegistration registry, CallbackInfo ci) {
        List<FissionJEIRecipe> completedRecipes = Lists.newArrayList();
        List<RecipeHolder<ChemicalToChemicalRecipe>> recipes = getFissionRecipes();
        MekanismFission.LOGGER.debug("Mixin iterating through {} fission recipes", recipes.size());
        for (FluidCoolantRecipe recipe : FissionReactorRecipeCategory.getFluidCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionJEIRecipe(r.id(), (FissionRecipe) r.value(), recipe.getOutputRepresentation(), recipe.getInput().getRepresentations())).toList());
        for (GasCoolantRecipe recipe : FissionReactorRecipeCategory.getGasCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionJEIRecipe(r.id(), (FissionRecipe) r.value(), recipe.getInput().getRepresentations(), recipe.getOutputRaw())).toList());
        RecipeRegistryHelper.register(registry, FissionRecipeViewType.FISSION, completedRecipes);
        ci.cancel();
    }

    @Inject(method = "registerCategories", at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IRecipeCategoryRegistration;addRecipeCategories([Lmezz/jei/api/recipe/category/IRecipeCategory;)V"), cancellable = true)
    public void registerFissionCategory(IRecipeCategoryRegistration registry, CallbackInfo ci) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper));
        ci.cancel();
    }

    @Inject(method = "registerRecipeCatalysts", at = @At(value = "INVOKE", target = "Lmekanism/client/recipe_viewer/jei/CatalystRegistryHelper;register(Lmezz/jei/api/registration/IRecipeCatalystRegistration;[Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;)V"), cancellable = true)
    public void registerFissionCatalysts(IRecipeCatalystRegistration registry, CallbackInfo ci) {
        CatalystRegistryHelper.register(registry, FissionRecipeViewType.FISSION);
        ci.cancel();
    }

    @Unique
    private static List<RecipeHolder<ChemicalToChemicalRecipe>> getFissionRecipes() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.FISSION.get());
    }
}
