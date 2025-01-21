package in.northwestw.fissionrecipe.mixin;

import com.google.common.collect.Lists;
import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.jei.*;
import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(value = MekanismJEI.class, remap = false)
public class MixinMekanismJEI {
    @Unique
    private IGuiHelper cacheGuiHelper;

    @Inject(method = "registerCategories", at = @At("HEAD"))
    public void cacheGuiHelper(IRecipeCategoryRegistration registry, CallbackInfo ci) {
        cacheGuiHelper = registry.getJeiHelpers().getGuiHelper();
    }

    @ModifyArg(method = "registerCategories", at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IRecipeCategoryRegistration;addRecipeCategories([Lmezz/jei/api/recipe/category/IRecipeCategory;)V", ordinal = 22))
    public IRecipeCategory<?>[] registerBoilerCategory(IRecipeCategory<?>[] recipeCategories) {
        return new IRecipeCategory[] {new BoilerRecipeCategory(cacheGuiHelper)};
    }

    @ModifyArgs(method = "registerRecipes", at = @At(value = "INVOKE", target = "Lmekanism/client/recipe_viewer/jei/RecipeRegistryHelper;register(Lmezz/jei/api/registration/IRecipeRegistration;Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;Ljava/util/List;)V", ordinal = 1))
    public void registerBoilerRecipes(Args args) {
        args.set(1, RecipeViewerRecipeTypes.BOILER);

        List<BoilerJEIRecipe> completedRecipes = Lists.newArrayList();
        List<RecipeHolder<BoilerRecipe>> recipes = getBoilerRecipes();
        for (HeatedCoolantRecipe heatedCoolantRecipe : getHeatedCoolantRecipes())
            for (RecipeHolder<BoilerRecipe> boilerRecipeHolder : recipes)
                completedRecipes.add(new BoilerJEIRecipe(boilerRecipeHolder.id(), boilerRecipeHolder.value(), heatedCoolantRecipe));

        args.set(2, completedRecipes);
    }

    @ModifyArg(method = "registerRecipeCatalysts", at = @At(value = "INVOKE", target = "Lmekanism/client/recipe_viewer/jei/CatalystRegistryHelper;register(Lmezz/jei/api/registration/IRecipeCatalystRegistration;[Lmekanism/client/recipe_viewer/type/IRecipeViewerRecipeType;)V", ordinal = 0), index = 1)
    public IRecipeViewerRecipeType<?>[] registerBoilerRecipeCatalysts(IRecipeViewerRecipeType<?>[] categories) {
        List<IRecipeViewerRecipeType<?>> list = Lists.newArrayList(categories);
        list.set(list.indexOf(RecipeViewerRecipeType.BOILER), RecipeViewerRecipeTypes.BOILER);
        return list.toArray(IRecipeViewerRecipeType[]::new);
    }

    @Unique
    private static List<RecipeHolder<BoilerRecipe>> getBoilerRecipes() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.BOILER.get());
    }

    @Unique
    private static List<HeatedCoolantRecipe> getHeatedCoolantRecipes() {
        return Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RecipeTypes.HEATED_COOLANT.get()).stream().map(holder -> (HeatedCoolantRecipe) holder.value()).toList();
    }
}
