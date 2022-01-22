package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.misc.Heat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.script.ScriptException;
import java.util.function.Predicate;

public class FluidCoolantRecipe extends MekanismRecipe implements Predicate<FluidStack> {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "fluid_coolant");
    private final FluidStackIngredient input;
    private final GasStack output;
    private final Heat heat;

    public FluidCoolantRecipe(ResourceLocation id, FluidStackIngredient input, GasStack output, Heat heat) {
        super(id);
        this.input = input;
        this.output = output;
        this.heat = heat;
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

    public GasStack getOutput(FluidStack input) {
        return this.output.copy();
    }

    @Override
    public void write(PacketBuffer buffer) {
        this.input.write(buffer);
        this.output.writeToPacket(buffer);
        boolean isEqt = this.heat.isEqt();
        buffer.writeBoolean(isEqt);
        if (isEqt) buffer.writeUtf(this.heat.getEquation());
        else buffer.writeFloat(this.heat.getConstant());
    }

    @Override
    public ItemStack assemble(IgnoredIInventory p_77572_1_) {
        return ItemStack.EMPTY;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismFission.RegistryEvent.Recipes.FLUID_COOLANT.getSerializer();
    }

    @Override
    public IRecipeType<?> getType() {
        return MekanismFission.RegistryEvent.Recipes.FLUID_COOLANT.getType();
    }

    public double getHeat(double toBurn) {
        if (!this.heat.isEqt()) return toBurn * this.heat.getConstant();
        String substituted = this.heat.getEquation().replaceAll("x", Double.toString(toBurn));
        try {
            return (double) Heat.JS_ENGINE.eval(substituted);
        } catch (ScriptException e) {
            Mekanism.logger.error("Failed to evaluate Fission Recipe equation.");
            e.printStackTrace();
            return 0;
        }
    }
}
