package ml.northwestwind.fissionrecipe.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.misc.Heat;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import javax.script.ScriptException;

public class GasCoolantRecipeSerializer implements RecipeSerializer<GasCoolantRecipe> {
    @Override
    public GasCoolantRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().deserialize(input);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        double thermalEnthalpy = json.get("thermalEnthalpy").getAsDouble();
        double conductivity = json.get("conductivity").getAsDouble();
        JsonElement heatObj = json.get("efficiency");
        boolean isEqt = false;
        float heat = 0;
        String heatEqt = null;
        try {
            heat = 1 / heatObj.getAsFloat();
        } catch (ClassCastException ignored) {
            heatEqt = heatObj.getAsString().replaceAll("x", "(1/x)");
            try {
                Heat.JS_ENGINE.eval(heatEqt.replaceAll("x", "0"));
            } catch (ScriptException e) {
                throw new JsonSyntaxException("Gas Coolant Recipe heat equation is not valid.");
            }
            isEqt = true;
        }
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Gas Coolant Recipe output must not be empty.");
        }
        return new GasCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity, new Heat(isEqt, heat, heatEqt));
    }

    @Nullable
    @Override
    public GasCoolantRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            double thermalEnthalpy = buffer.readDouble();
            double conductivity = buffer.readDouble();
            boolean isEqt = buffer.readBoolean();
            Heat heat = new Heat(isEqt, isEqt ? 0 : buffer.readDouble(), isEqt ? buffer.readUtf() : null);
            return new GasCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity, heat);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading Gas Coolant Recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GasCoolantRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing Gas Coolant Recipe to packet.", e);
            throw e;
        }
    }
}
