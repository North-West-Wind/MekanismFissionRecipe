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
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.script.ScriptException;

public class FissionRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<FissionRecipe> {
    @Override
    public FissionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().deserialize(input);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        JsonElement heatObj = json.get("heat");
        boolean isEqt = false;
        float heat = 0;
        String heatEqt = null;
        try {
            heat = heatObj.getAsFloat();
        } catch (ClassCastException ignored) {
            heatEqt = heatObj.getAsString();
            try {
                Heat.JS_ENGINE.eval(heatEqt.replaceAll("x", "0"));
            } catch (ScriptException e) {
                throw new JsonSyntaxException("Fission Recipe heat equation is not valid.");
            }
            isEqt = true;
        }
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Fission Recipe output must not be empty.");
        }
        return new FissionRecipe(recipeId, inputIngredient, output, new Heat(isEqt, heat, heatEqt));
    }

    @Nullable
    @Override
    public FissionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            boolean isEqt = buffer.readBoolean();
            Heat heat = new Heat(isEqt, isEqt ? 0 : buffer.readFloat(), isEqt ? buffer.readUtf() : null);
            return new FissionRecipe(recipeId, inputIngredient, output, heat);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading Fission Recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, FissionRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing Fission Recipe to packet.", e);
            throw e;
        }
    }
}
