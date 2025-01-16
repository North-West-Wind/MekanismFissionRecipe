package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.common.Mekanism;
import in.northwestw.fissionrecipe.MekanismFission;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidCoolantRecipe extends MekanismRecipe<SingleFluidRecipeInput> implements Predicate<FluidStack> {
    private final FluidStackIngredient input;
    private final ChemicalStack output;
    private final double thermalEnthalpy,  conductivity, efficiency;

    public FluidCoolantRecipe(FluidStackIngredient input, ChemicalStack output, double thermalEnthalpy, double conductivity, double efficiency) {
        this.input = input;
        this.output = output;
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
        this.efficiency = efficiency;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    public FluidStackIngredient getInput() {
        return this.input;
    }

    public ChemicalStack getOutput() {
        return this.output;
    }

    public ChemicalStack getOutputRepresentation() {
        return this.output;
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public boolean matches(SingleFluidRecipeInput input, Level level) {
        return !this.isIncomplete() && this.test(input.fluid());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.RecipeSerializers.FLUID_COOLANT.get();
    }

    @Override
    public RecipeType<?> getType() {
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
