package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import in.northwestw.fissionrecipe.MekanismFission;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class FluidCoolantRecipe extends FluidToChemicalRecipe {
    private final double thermalEnthalpy,  conductivity, efficiency;

    public FluidCoolantRecipe(FluidStackIngredient input, ChemicalStack output, double thermalEnthalpy, double conductivity, double efficiency) {
        super(input, output);
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
        this.efficiency = efficiency;
    }

    @Override
    public RecipeSerializer<? extends FluidToChemicalRecipe> getSerializer() {
        return MekanismFission.RecipeSerializers.FLUID_COOLANT.get();
    }

    @Override
    public RecipeType<? extends FluidToChemicalRecipe> getType() {
        return MekanismFission.RecipeTypes.FLUID_COOLANT.get();
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
