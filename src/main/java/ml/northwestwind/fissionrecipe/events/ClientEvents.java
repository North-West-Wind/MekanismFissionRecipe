package ml.northwestwind.fissionrecipe.events;

import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.RecipeStorage;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MekanismFission.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void recipeUpdated(final RecipesUpdatedEvent event) {
        RecipeStorage.clearFissionRecipes();
        IRecipeType<FissionRecipe> type = (IRecipeType<FissionRecipe>) Registry.RECIPE_TYPE.get(FissionRecipe.RECIPE_TYPE_ID);
        if (type == null) return;
        List<FissionRecipe> recipes = event.getRecipeManager().getAllRecipesFor(type);
        RecipeStorage.addFissionRecipes(recipes.toArray(new FissionRecipe[0]));
    }
}
