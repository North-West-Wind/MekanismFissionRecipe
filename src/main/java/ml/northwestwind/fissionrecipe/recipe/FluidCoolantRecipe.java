package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.misc.Heat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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

    @Override
    public void write(FriendlyByteBuf buffer) {
        this.input.write(buffer);
        this.output.writeToPacket(buffer);
        boolean isEqt = this.heat.isEqt();
        buffer.writeBoolean(isEqt);
        if (isEqt) buffer.writeUtf(this.heat.getEquation());
        else buffer.writeFloat(this.heat.getConstant());
    }

    @Override
    public boolean isIncomplete() {
        return input.hasNoMatchingInstances();
    }

    @Override
    public ItemStack assemble(IgnoredIInventory p_77572_1_) {
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

    public static String location() {
        return RECIPE_TYPE_ID.getPath();
    }
}
