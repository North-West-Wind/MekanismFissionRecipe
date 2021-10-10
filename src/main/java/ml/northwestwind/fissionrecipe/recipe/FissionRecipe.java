package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import ml.northwestwind.fissionrecipe.recipe.serializer.FissionRecipeSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import javax.script.ScriptException;

public class FissionRecipe extends GasToGasRecipe {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "fission");
    private final FissionRecipeSerializer.Heat heat;

    public FissionRecipe(ResourceLocation id, GasStackIngredient input, GasStack output, FissionRecipeSerializer.Heat heat) {
        super(id, input, output);
        this.heat = heat;
    }

    @Override
    public ItemStack assemble(IgnoredIInventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismFission.RegistryEvent.Recipes.FISSION.getSerializer();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(RECIPE_TYPE_ID);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        boolean isEqt = this.heat.isEqt();
        buffer.writeBoolean(isEqt);
        if (isEqt) buffer.writeUtf(this.heat.getEquation());
        else buffer.writeFloat(this.heat.getConstant());
    }

    public double getHeat(double toBurn) {
        if (!this.heat.isEqt()) return toBurn * this.heat.getConstant();
        String substituted = this.heat.getEquation().replaceAll("x", Double.toString(toBurn));
        try {
            return (double) FissionRecipeSerializer.JS_ENGINE.eval(substituted);
        } catch (ScriptException e) {
            Mekanism.logger.error("Failed to evaluate Fission Recipe equation.");
            e.printStackTrace();
            return 0;
        }
    }
}
