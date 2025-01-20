package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Predicate;

public abstract class FluidToChemicalRecipe extends MekanismRecipe<SingleFluidRecipeInput> implements Predicate<FluidStack> {
    protected final FluidStackIngredient input;
    protected final ChemicalStack output;

    public FluidToChemicalRecipe(FluidStackIngredient input, ChemicalStack output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    @Override
    public boolean matches(SingleFluidRecipeInput input, Level level) {
        return !this.isIncomplete() && this.test(input.fluid());
    }

    @Override
    public boolean isIncomplete() {
        return this.input.hasNoMatchingInstances();
    }

    @Override
    public abstract RecipeType<? extends FluidToChemicalRecipe> getType();

    @Override
    public abstract RecipeSerializer<? extends FluidToChemicalRecipe> getSerializer();

    public FluidStackIngredient getInput() {
        return this.input;
    }

    public ChemicalStack getOutput() {
        return this.output;
    }

    public ChemicalStack getOutputRepresentation() {
        return this.output;
    }
}
