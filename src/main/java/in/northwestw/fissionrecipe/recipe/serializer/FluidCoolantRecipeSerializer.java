package in.northwestw.fissionrecipe.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import in.northwestw.fissionrecipe.recipe.FluidCoolantRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record FluidCoolantRecipeSerializer(MapCodec<FluidCoolantRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, FluidCoolantRecipe> streamCodec) implements RecipeSerializer<FluidCoolantRecipe> {
    public static FluidCoolantRecipeSerializer create() {
        return new FluidCoolantRecipeSerializer(RecordCodecBuilder.mapCodec(instance -> instance.group(
                FluidStackIngredient.CODEC.fieldOf(SerializationConstants.INPUT).forGetter(FluidCoolantRecipe::getInput),
                ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(FluidCoolantRecipe::getOutput),
                Codec.DOUBLE.fieldOf("thermalEnthalpy").forGetter(FluidCoolantRecipe::getThermalEnthalpy),
                Codec.DOUBLE.fieldOf("conductivity").forGetter(FluidCoolantRecipe::getConductivity),
                Codec.DOUBLE.fieldOf("efficiency").forGetter(FluidCoolantRecipe::getEfficiency)
        ).apply(instance, FluidCoolantRecipe::new)), StreamCodec.composite(
                FluidStackIngredient.STREAM_CODEC, FluidCoolantRecipe::getInput,
                ChemicalStack.STREAM_CODEC, FluidCoolantRecipe::getOutput,
                ByteBufCodecs.DOUBLE, FluidCoolantRecipe::getThermalEnthalpy,
                ByteBufCodecs.DOUBLE, FluidCoolantRecipe::getConductivity,
                ByteBufCodecs.DOUBLE, FluidCoolantRecipe::getEfficiency,
                FluidCoolantRecipe::new
        ));
    }
}
