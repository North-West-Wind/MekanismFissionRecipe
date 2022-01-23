package ml.northwestwind.fissionrecipe.mixin;

import mekanism.generators.client.jei.GeneratorsJEI;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.jei.FissionReactorRecipeCategory;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(value = GeneratorsJEI.class, remap = false)
public class MixinGeneratorsJEI {
    /**
     * @author Mekanism
     */
    @Overwrite
    public void registerRecipes(IRecipeRegistration registry) {
        List<FissionRecipe> recipes = Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType());
        for (FluidCoolantRecipe recipe : FissionReactorRecipeCategory.getFluidCoolants()) registry.addRecipes(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getOutputRepresentation(), recipe.getInput().getRepresentations())).collect(Collectors.toList()), FissionRecipe.RECIPE_TYPE_ID);
        for (GasCoolantRecipe recipe : FissionReactorRecipeCategory.getGasCoolants()) registry.addRecipes(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getInput().getRepresentations(), recipe.getOutputRepresentation())).collect(Collectors.toList()), FissionRecipe.RECIPE_TYPE_ID);
    }

    /**
     * @author Mekanism
     */
    @Overwrite
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper));
    }
}
