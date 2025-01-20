package in.northwestw.fissionrecipe.recipe;

import in.northwestw.fissionrecipe.MekanismFission;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class HeatedCoolantRecipe extends BasicChemicalToChemicalRecipe {
    private final double thermalEnthalpy, efficiency, temperature;

    public HeatedCoolantRecipe(ChemicalStackIngredient input, ChemicalStack output, double thermalEnthalpy, double efficiency, double temperature) {
        super(input, output, MekanismFission.RecipeTypes.HEATED_COOLANT.get());
        this.thermalEnthalpy = thermalEnthalpy;
        this.efficiency = efficiency;
        this.temperature = temperature;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.RecipeSerializers.HEATED_COOLANT.value();
    }

    public double getThermalEnthalpy() {
        return thermalEnthalpy;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getTemperature() {
        return temperature;
    }
}
