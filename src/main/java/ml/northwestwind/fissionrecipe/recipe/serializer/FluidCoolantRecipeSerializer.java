package ml.northwestwind.fissionrecipe.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.misc.Heat;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import javax.script.ScriptException;

public class FluidCoolantRecipeSerializer implements RecipeSerializer<FluidCoolantRecipe> {
    @Override
    public FluidCoolantRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().deserialize(input);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        double thermalEnthalpy = json.get("thermalEnthalpy").getAsDouble();
        double conductivity = json.get("conductivity").getAsDouble();
        double efficiency = json.get("efficiency").getAsDouble();
        double outputEfficiency = json.get("outputEfficiency").getAsDouble();
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Fluid Coolant Recipe output must not be empty.");
        }
        return new FluidCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity, efficiency, outputEfficiency);
    }

    @Nullable
    @Override
    public FluidCoolantRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        try {
            FluidStackIngredient inputIngredient = IngredientCreatorAccess.fluid().read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            double thermalEnthalpy = buffer.readDouble();
            double conductivity = buffer.readDouble();
            double efficiency = buffer.readDouble();
            double outputEfficiency = buffer.readDouble();
            return new FluidCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity, efficiency, outputEfficiency);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading Fluid Coolant Recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, FluidCoolantRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing Fluid Coolant Recipe to packet.", e);
            throw e;
        }
    }
}
