package in.northwestw.fissionrecipe.jei;

import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BoilerJEIRecipe extends BoilerRecipe {
    private final ResourceLocation id;
    private final HeatedCoolantRecipe heatedCoolantRecipe;

    public BoilerJEIRecipe(ResourceLocation id, BoilerRecipe recipe, HeatedCoolantRecipe heatedCoolantRecipe) {
        super(recipe.getInput(), recipe.getOutput(), recipe.getThermalEnthalpy(), recipe.getConductivity(), recipe.getEfficiency());
        this.id = id;
        this.heatedCoolantRecipe = heatedCoolantRecipe;
    }

    public ResourceLocation getId() {
        return id;
    }

    public double getTemperature() {
        return this.heatedCoolantRecipe.getTemperature();
    }

    public ChemicalStackIngredient getCoolantInput() {
        return this.heatedCoolantRecipe.getInput();
    }

    public ChemicalStack getCoolantOutput() {
        return this.heatedCoolantRecipe.getOutputRaw();
    }
}
