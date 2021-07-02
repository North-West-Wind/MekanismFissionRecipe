package ml.northwestwind.fissionrecipe.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class FissionRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FissionRecipe> {
    @Override
    public FissionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = JSONUtils.isArrayNode(json, JsonConstants.INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.INPUT) :
                JSONUtils.getAsJsonObject(json, JsonConstants.INPUT);
        GasStackIngredient inputIngredient = GasStackIngredient.deserialize(input);
        GasStack output = SerializerHelper.getGasStack(json, JsonConstants.OUTPUT);
        float heat = json.get("heat").getAsFloat();
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return new FissionRecipe(recipeId, inputIngredient, output, heat);
    }

    @Nullable
    @Override
    public FissionRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        try {
            GasStackIngredient inputIngredient = GasStackIngredient.read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            float heat = buffer.readFloat();
            return new FissionRecipe(recipeId, inputIngredient, output, heat);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading gas to gas recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(PacketBuffer buffer, FissionRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing gas to gas recipe to packet.", e);
            throw e;
        }
    }
}
