package ml.northwestwind.fissionrecipe.mixin;

import mekanism.generators.client.jei.GeneratorsJEI;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.jei.FissionReactorRecipeCategory;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = GeneratorsJEI.class, remap = false)
public class MixinGeneratorsJEI {
    /**
     * @author Mekanism
     */
    @Overwrite
    public void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(Minecraft.getInstance().getConnection().getRecipeManager().getAllRecipesFor(MekanismFission.RegistryEvent.Recipes.FISSION.getType()), GeneratorsBlocks.FISSION_REACTOR_CASING.getRegistryName());
    }

    /**
     * @author Mekanism
     */
    @Overwrite
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new IRecipeCategory[]{new FissionReactorRecipeCategory(guiHelper)});
    }
}