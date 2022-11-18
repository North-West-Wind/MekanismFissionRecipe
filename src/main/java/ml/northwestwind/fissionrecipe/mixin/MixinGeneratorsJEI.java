package ml.northwestwind.fissionrecipe.mixin;

import com.google.common.collect.Lists;
import mekanism.client.jei.CatalystRegistryHelper;
import mekanism.client.jei.RecipeRegistryHelper;
import mekanism.generators.client.jei.GeneratorsJEI;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.jei.FissionReactorRecipeCategory;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = GeneratorsJEI.class, remap = false)
public class MixinGeneratorsJEI {
    /**
     * @author Mekanism
     * @reason Register custom fission recipes
     */
    @Overwrite
    public void registerRecipes(IRecipeRegistration registry) {
        List<FissionReactorRecipeCategory.FissionJEIRecipe> completedRecipes = Lists.newArrayList();
        List<FissionRecipe> recipes = getFissionRecipes();
        for (FluidCoolantRecipe recipe : FissionReactorRecipeCategory.getFluidCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getOutputRepresentation(), recipe.getInput().getRepresentations())).toList());
        for (GasCoolantRecipe recipe : FissionReactorRecipeCategory.getGasCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getInput().getRepresentations(), recipe.getOutputRepresentation())).toList());
        RecipeRegistryHelper.register(registry, FissionRecipe.RECIPE_TYPE, completedRecipes);
    }

    /**
     * @author Mekanism
     * @reason Register relevant category for custom fission recipes
     */
    @Overwrite
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper));
    }

    /**
     * @author Mekanism
     * @reason Change catalysts to be registered in custom category
     */
    @Overwrite
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, FissionRecipe.RECIPE_TYPE, GeneratorsBlocks.FISSION_REACTOR_CASING, GeneratorsBlocks.FISSION_REACTOR_PORT,
                GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlocks.FISSION_FUEL_ASSEMBLY, GeneratorsBlocks.CONTROL_ROD_ASSEMBLY);
    }

    @Unique
    private static List<FissionRecipe> getFissionRecipes() {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) return ServerLifecycleHooks.getCurrentServer().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType());
        else return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeCallable<List<FissionRecipe>>) () -> Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType()));
    }
}
