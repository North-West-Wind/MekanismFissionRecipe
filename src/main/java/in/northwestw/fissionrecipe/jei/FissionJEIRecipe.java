package in.northwestw.fissionrecipe.jei;

import in.northwestw.fissionrecipe.recipe.FissionRecipe;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class FissionJEIRecipe extends FissionRecipe {
    private final ResourceLocation id;
    private final boolean fluidCoolant;
    private final List<FluidStack> fluidInputs;
    private final List<ChemicalStack> gasInputs;
    private final ChemicalStack output;

    public FissionJEIRecipe(ResourceLocation id, FissionRecipe recipe, ChemicalStack output, List<FluidStack> inputs) {
        super(recipe.getInput(), recipe.getOutputRaw(), recipe.getHeat());
        this.id = id;
        this.fluidCoolant = true;
        this.fluidInputs = inputs;
        this.gasInputs = null;
        this.output = output;
    }

    public FissionJEIRecipe(ResourceLocation id, FissionRecipe recipe, List<ChemicalStack> inputs, ChemicalStack output) {
        super(recipe.getInput(), recipe.getOutputRaw(), recipe.getHeat());
        this.id = id;
        this.fluidCoolant = false;
        this.fluidInputs = null;
        this.gasInputs = inputs;
        this.output = output;
    }

    public boolean isFluidCoolant() {
        return this.fluidCoolant;
    }

    public List<FluidStack> getFluidInputs() {
        // If for some reason the input has negative amount, set it to 0
        List<FluidStack> copy = Lists.newArrayList();
        fluidInputs.forEach(stack -> {
            if (stack.getAmount() < 0) {
                FluidStack stackCopy = stack;
                stackCopy.setAmount(0);
                copy.add(stackCopy);
            } else copy.add(stack);
        });
        return copy;
    }

    public List<ChemicalStack> getGasInputs() {
        return gasInputs;
    }

    public ChemicalStack getOutput() {
        return output;
    }

    public ResourceLocation getId() {
        return id;
    }
}
