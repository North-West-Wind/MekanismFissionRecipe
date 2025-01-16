package in.northwestw.fissionrecipe.mixin;

import com.google.common.collect.Lists;
import in.northwestw.fissionrecipe.jei.FissionJEIRecipe;
import in.northwestw.fissionrecipe.jei.FissionRecipeViewType;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.generators.client.recipe_viewer.GeneratorsRVRecipeType;
import mekanism.generators.client.recipe_viewer.jei.GeneratorsJEI;
import mekanism.generators.common.registries.GeneratorsBlocks;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = GeneratorsJEI.class, remap = false)
public class MixinGeneratorsJEI {
    // override mekanism hard-coded recipes with ours
    @Inject(method = "registerRecipes", at = @At(value = "INVOKE", target = "Lmekanism/client/recipe_viewer/jei/RecipeRegistryHelper;register(Lmezz/jei/api/registration/IRecipeRegistration;Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;Ljava/util/List;)V"), cancellable = true)
    public void registerFissionRecipes(IRecipeRegistration registry, CallbackInfo ci) {
        List<FissionJEIRecipe> completedRecipes = Lists.newArrayList();
        List<FissionRecipe> recipes = getFissionRecipes();
        for (FluidCoolantRecipe recipe : FissionReactorRecipeCategory.getFluidCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getOutputRepresentation(), recipe.getInput().getRepresentations())).toList());
        for (GasCoolantRecipe recipe : FissionReactorRecipeCategory.getGasCoolants()) completedRecipes.addAll(recipes.stream().map(r -> new FissionReactorRecipeCategory.FissionJEIRecipe(r, recipe.getInput().getRepresentations(), recipe.getOutputRepresentation())).toList());
        RecipeRegistryHelper.register(registry, FissionRecipeViewType.FISSION, recipes);
        ci.cancel();
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
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) return ServerLifecycleHooks.getCurrentServer().getRecipeManager().getAllRecipesFor(MekanismFission.Recipes.FISSION.getType());
        else return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeCallable<List<FissionRecipe>>) () -> Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.Recipes.FISSION.getType()));
    }
}
