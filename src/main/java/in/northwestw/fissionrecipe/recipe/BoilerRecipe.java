package in.northwestw.fissionrecipe.recipe;

import in.northwestw.fissionrecipe.MekanismFission;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class BoilerRecipe extends FluidToChemicalRecipe {
    private final double thermalEnthalpy, conductivity, efficiency;
    public BoilerRecipe(FluidStackIngredient input, ChemicalStack output, double thermalEnthalpy, double conductivity, double efficiency) {
        super(input, output);
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
        this.efficiency = efficiency;
    }

    @Override
    public RecipeType<? extends FluidToChemicalRecipe> getType() {
        return MekanismFission.RecipeTypes.BOILER.get();
    }

    @Override
    public RecipeSerializer<? extends FluidToChemicalRecipe> getSerializer() {
        return MekanismFission.RecipeSerializers.BOILER.value();
    }

    public double getThermalEnthalpy() {
        return thermalEnthalpy;
    }

    public double getConductivity() {
        return conductivity;
    }

    public double getEfficiency() {
        return efficiency;
    }
}
