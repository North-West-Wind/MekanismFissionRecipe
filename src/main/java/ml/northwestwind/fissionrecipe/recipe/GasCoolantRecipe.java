package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.misc.Heat;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import javax.script.ScriptException;

public class GasCoolantRecipe extends GasToGasRecipe {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "gas_coolant");
    private final double thermalEnthalpy;
    private final double conductivity;
    private final Heat heat;

    public GasCoolantRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, GasStack output, double thermalEnthalpy, double conductivity, Heat heat) {
        super(id, input, output);
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
        this.heat = heat;
    }

    @Override
    public ItemStack assemble(IgnoredIInventory p_77572_1_) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.Recipes.GAS_COOLANT.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(RECIPE_TYPE_ID);
    }

    public GasStack getOutputRepresentation() {
        return this.output.copy();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeDouble(this.thermalEnthalpy);
        buffer.writeDouble(this.conductivity);
        boolean isEqt = this.heat.isEqt();
        buffer.writeBoolean(isEqt);
        if (isEqt) buffer.writeUtf(this.heat.getEquation());
        else buffer.writeFloat(this.heat.getConstant());
    }

    public double getThermalEnthalpy() {
        return thermalEnthalpy;
    }

    public double getConductivity() {
        return conductivity;
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
