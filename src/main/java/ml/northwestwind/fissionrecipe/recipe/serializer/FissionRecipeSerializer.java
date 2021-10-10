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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class FissionRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<FissionRecipe> {
    public static final ScriptEngine JS_ENGINE;

    @Override
    public FissionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonElement input = JSONUtils.isArrayNode(json, JsonConstants.INPUT) ? JSONUtils.getAsJsonArray(json, JsonConstants.INPUT) :
                JSONUtils.getAsJsonObject(json, JsonConstants.INPUT);
        GasStackIngredient inputIngredient = GasStackIngredient.deserialize(input);
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
                JS_ENGINE.eval(heatEqt.replaceAll("x", "0"));
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
    public FissionRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        try {
            GasStackIngredient inputIngredient = GasStackIngredient.read(buffer);
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
    public void toNetwork(PacketBuffer buffer, FissionRecipe recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            Mekanism.logger.error("Error writing Fission Recipe to packet.", e);
            throw e;
        }
    }

    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        JS_ENGINE = mgr.getEngineByName("JavaScript");
    }

    public static class Heat {
        private final boolean isEqt;
        private final float constant;
        private final String equation;

        public Heat(boolean isEqt, float constant, String equation) {
            this.isEqt = isEqt;
            if (this.isEqt) {
                this.constant = 0;
                this.equation = equation;
            } else {
                this.constant = constant;
                this.equation = null;
            }
        }

        public boolean isEqt() {
            return isEqt;
        }

        public float getConstant() {
            return constant;
        }

        public String getEquation() {
            return equation;
        }
    }
}
