package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.misc.Heat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import javax.script.ScriptException;

public class GasCoolantRecipe extends GasToGasRecipe {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "gas_coolant");
    private final double thermalEnthalpy;
    private final double conductivity;
    private final Heat heat;

    public GasCoolantRecipe(ResourceLocation id, GasStackIngredient input, GasStack output, double thermalEnthalpy, double conductivity, Heat heat) {
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
    public IRecipeSerializer<?> getSerializer() {
        return MekanismFission.RegistryEvent.Recipes.GAS_COOLANT.getSerializer();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(RECIPE_TYPE_ID);
    }

    @Override
    public void write(PacketBuffer buffer) {
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
}
