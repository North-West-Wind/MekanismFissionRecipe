package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.misc.Heat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.script.ScriptException;

public class FissionRecipe extends BasicChemicalToChemicalRecipe {
    private final Heat heat;

    public FissionRecipe(ChemicalStackIngredient input, ChemicalStack output, Heat heat) {
        super(input, output, MekanismFission.RecipeTypes.FISSION.get());
        this.heat = heat;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.RecipeSerializers.FISSION.get();
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

    public Heat getHeat() {
        return this.heat;
    }
}
