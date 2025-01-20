package in.northwestw.fissionrecipe.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.fissionrecipe.recipe.HeatedCoolantRecipe;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record HeatedCoolantRecipeSerializer(MapCodec<HeatedCoolantRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, HeatedCoolantRecipe> streamCodec) implements RecipeSerializer<HeatedCoolantRecipe> {
    public HeatedCoolantRecipeSerializer() {
        this(RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(HeatedCoolantRecipe::getInput),
                ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(HeatedCoolantRecipe::getOutputRaw),
                Codec.DOUBLE.fieldOf("thermalEnthalpy").forGetter(HeatedCoolantRecipe::getThermalEnthalpy),
                Codec.DOUBLE.fieldOf("efficiency").forGetter(HeatedCoolantRecipe::getEfficiency),
                Codec.DOUBLE.fieldOf("temperature").forGetter(HeatedCoolantRecipe::getTemperature)
        ).apply(instance, HeatedCoolantRecipe::new)), StreamCodec.composite(
                IngredientCreatorAccess.chemicalStack().streamCodec(), HeatedCoolantRecipe::getInput,
                ChemicalStack.STREAM_CODEC, HeatedCoolantRecipe::getOutputRaw,
                ByteBufCodecs.DOUBLE, HeatedCoolantRecipe::getThermalEnthalpy,
                ByteBufCodecs.DOUBLE, HeatedCoolantRecipe::getEfficiency,
                ByteBufCodecs.DOUBLE, HeatedCoolantRecipe::getTemperature,
                HeatedCoolantRecipe::new
        ));
    }
}
