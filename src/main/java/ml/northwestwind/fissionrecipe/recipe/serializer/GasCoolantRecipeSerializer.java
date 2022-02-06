package ml.northwestwind.fissionrecipe.recipe.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.misc.Heat;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.script.ScriptException;

public class GasCoolantRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<GasCoolantRecipe> {
    @Override
    public GasCoolantRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = JSONUtils.isArrayNode(json, JsonConstants.INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.INPUT) :
                JSONUtils.getAsJsonObject(json, JsonConstants.INPUT);
        GasStackIngredient inputIngredient = GasStackIngredient.deserialize(input);
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
    public GasCoolantRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        try {
            GasStackIngredient inputIngredient = GasStackIngredient.read(buffer);
            GasStack output = GasStack.readFromPacket(buffer);
            double thermalEnthalpy = buffer.readDouble();
            double conductivity = buffer.readDouble();
            boolean isEqt = buffer.readBoolean();
            Heat heat = new Heat(isEqt, isEqt ? 0 : buffer.readFloat(), isEqt ? buffer.readUtf() : null);
            return new GasCoolantRecipe(recipeId, inputIngredient, output, thermalEnthalpy, conductivity, heat);
        } catch (Exception e) {
            Mekanism.logger.error("Error reading Gas Coolant Recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(PacketBuffer buffer, GasCoolantRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing Gas Coolant Recipe to packet.", e);
            throw e;
        }
    }
}
