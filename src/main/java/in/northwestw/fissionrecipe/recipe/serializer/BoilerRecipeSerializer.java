package in.northwestw.fissionrecipe.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.fissionrecipe.recipe.BoilerRecipe;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record BoilerRecipeSerializer(MapCodec<BoilerRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, BoilerRecipe> streamCodec) implements RecipeSerializer<BoilerRecipe> {
    public BoilerRecipeSerializer() {
        this(RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(BoilerRecipe::getInput),
                ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(BoilerRecipe::getOutput),
                Codec.DOUBLE.fieldOf("thermalEnthalpy").forGetter(BoilerRecipe::getThermalEnthalpy),
                Codec.DOUBLE.fieldOf("conductivity").forGetter(BoilerRecipe::getConductivity),
                Codec.DOUBLE.fieldOf("efficiency").forGetter(BoilerRecipe::getEfficiency)
        ).apply(instance, BoilerRecipe::new)), StreamCodec.composite(
                FluidStackIngredient.STREAM_CODEC, BoilerRecipe::getInput,
                ChemicalStack.STREAM_CODEC, BoilerRecipe::getOutput,
                ByteBufCodecs.DOUBLE, BoilerRecipe::getThermalEnthalpy,
                ByteBufCodecs.DOUBLE, BoilerRecipe::getConductivity,
                ByteBufCodecs.DOUBLE, BoilerRecipe::getEfficiency,
                BoilerRecipe::new
        ));
    }
}
