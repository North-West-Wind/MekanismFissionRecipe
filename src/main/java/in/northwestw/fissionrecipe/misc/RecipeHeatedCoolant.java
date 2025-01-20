package in.northwestw.fissionrecipe.misc;

import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.providers.IChemicalProvider;

public class RecipeHeatedCoolant extends ChemicalAttributes.HeatedCoolant {
    public RecipeHeatedCoolant(HeatedCoolantRecipe recipe) {
        super(recipe.getOutputRaw().getChemical(), recipe.getThermalEnthalpy(), 0); // conductivity is never used
    }
}
