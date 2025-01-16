package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import in.northwestw.fissionrecipe.MekanismFission;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GasCoolantRecipe extends BasicChemicalToChemicalRecipe {
    private final double thermalEnthalpy, conductivity;

    public GasCoolantRecipe(ChemicalStackIngredient input, ChemicalStack output, double thermalEnthalpy, double conductivity) {
        super(input, output, MekanismFission.RecipeTypes.GAS_COOLANT.get());
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.RecipeSerializers.GAS_COOLANT.get();
    }

    public double getThermalEnthalpy() {
        return thermalEnthalpy;
    }

    public double getConductivity() {
        return conductivity;
    }
}
