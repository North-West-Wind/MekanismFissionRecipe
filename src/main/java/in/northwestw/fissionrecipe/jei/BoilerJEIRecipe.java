package in.northwestw.fissionrecipe.jei;

import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BoilerJEIRecipe extends BoilerRecipe {
    private final ResourceLocation id;
    private final List<ChemicalStack> coolantInputs;
    private final ChemicalStack coolantOutput;

    public BoilerJEIRecipe(ResourceLocation id, BoilerRecipe recipe, List<ChemicalStack> coolantInputs, ChemicalStack coolantOutput) {
        super(recipe.getInput(), recipe.getOutput(), recipe.getThermalEnthalpy(), recipe.getConductivity(), recipe.getEfficiency());
        this.id = id;
        this.coolantInputs = coolantInputs;
        this.coolantOutput = coolantOutput;
    }
}
