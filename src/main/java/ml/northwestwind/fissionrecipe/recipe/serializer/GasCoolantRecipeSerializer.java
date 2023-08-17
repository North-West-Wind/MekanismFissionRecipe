package ml.northwestwind.fissionrecipe.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;

public class GasCoolantRecipeSerializer implements RecipeSerializer<GasCoolantRecipe> {
    @Override
    public GasCoolantRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().deserialize(input);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        double thermalEnthalpy = json.get("thermalEnthalpy").getAsDouble();
        double conductivity = json.get("conductivity").getAsDouble();
        return new GasCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity);
    }

    @Nullable
    @Override
    public GasCoolantRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            ChemicalStackIngredient.GasStackIngredient inputIngredient = IngredientCreatorAccess.gas().read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            double thermalEnthalpy = buffer.readDouble();
            double conductivity = buffer.readDouble();
            return new GasCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity);
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
