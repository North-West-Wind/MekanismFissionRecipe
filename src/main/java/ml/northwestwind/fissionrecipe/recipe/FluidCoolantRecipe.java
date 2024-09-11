package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidCoolantRecipe extends MekanismRecipe implements Predicate<FluidStack> {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "fluid_coolant");
    private final FluidStackIngredient input;
    private final GasStack output;
    private final double thermalEnthalpy,  conductivity, efficiency, outputEfficiency;

    public FluidCoolantRecipe(ResourceLocation id, FluidStackIngredient input, GasStack output, double thermalEnthalpy, double conductivity, double efficiency, double outputEfficiency) {
        super(id);
        this.input = input;
        this.output = output;
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
        this.efficiency = efficiency;
        this.outputEfficiency = outputEfficiency;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return this.input.test(fluidStack);
    }

    public FluidStackIngredient getInput() {
        return this.input;
    }

    public GasStack getOutputRepresentation() {
        return this.output;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        this.input.write(buffer);
        this.output.writeToPacket(buffer);
        buffer.writeDouble(this.thermalEnthalpy);
        buffer.writeDouble(this.conductivity);
        buffer.writeDouble(this.efficiency);
        buffer.writeDouble(this.outputEfficiency);
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.Recipes.FLUID_COOLANT.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return MekanismFission.Recipes.FLUID_COOLANT.getType();
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

    public double getOutputEfficiency() {
        return outputEfficiency;
    }

    public static String location() {
        return RECIPE_TYPE_ID.getPath();
    }
}
