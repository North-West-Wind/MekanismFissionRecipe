package in.northwestw.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.Mekanism;
import in.northwestw.fissionrecipe.MekanismFission;
import in.northwestw.fissionrecipe.jei.FissionReactorRecipeCategory;
import in.northwestw.fissionrecipe.misc.Heat;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;

public class FissionRecipe extends GasToGasRecipe {
    public static final MekanismJEIRecipeType<FissionReactorRecipeCategory.FissionJEIRecipe> RECIPE_TYPE = new MekanismJEIRecipeType<>(Mekanism.rl("fission"), FissionReactorRecipeCategory.FissionJEIRecipe.class);
    private final Heat heat;

    public FissionRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, GasStack output, Heat heat) {
        super(id, input, output);
        this.heat = heat;
    }

    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.Recipes.FISSION.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return MekanismFission.Recipes.FISSION.getType();
    }

    public GasStack getOutputRepresentation() {
        return this.output.copy();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        boolean isEqt = this.heat.isEqt();
        buffer.writeBoolean(isEqt);
        if (isEqt) buffer.writeUtf(this.heat.getEquation());
        else buffer.writeDouble(this.heat.getConstant());
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

    public Heat getHeatObject() {
        return this.heat;
    }

    public static String location() {
        return RECIPE_TYPE.uid().getPath();
    }
}
