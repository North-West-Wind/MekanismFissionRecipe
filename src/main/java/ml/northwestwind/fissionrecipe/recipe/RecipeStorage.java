package ml.northwestwind.fissionrecipe.recipe;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class RecipeStorage {
    private static final List<FissionRecipe> FISSION_RECIPES = Lists.newArrayList();

    public static void addFissionRecipes(FissionRecipe... recipes) {
        FISSION_RECIPES.addAll(Arrays.asList(recipes));
    }

    public static List<FissionRecipe> getFissionRecipes() {
        return FISSION_RECIPES;
    }

    public static void clearFissionRecipes() {
        FISSION_RECIPES.clear();
    }
}
