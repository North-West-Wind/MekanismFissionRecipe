package in.northwestw.fissionrecipe.recipe.serializer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.basic.BasicChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import in.northwestw.fissionrecipe.recipe.GasCoolantRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record GasCoolantRecipeSerializer(MapCodec<GasCoolantRecipe> codec, StreamCodec<RegistryFriendlyByteBuf, GasCoolantRecipe> streamCodec) implements RecipeSerializer<GasCoolantRecipe> {
    public static GasCoolantRecipeSerializer create() {
        return new GasCoolantRecipeSerializer(RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientCreatorAccess.chemicalStack().codec().fieldOf(SerializationConstants.INPUT).forGetter(GasCoolantRecipe::getInput),
                ChemicalStack.MAP_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(GasCoolantRecipe::getOutputRaw),
                Codec.DOUBLE.fieldOf("thermalEnthalpy").forGetter(GasCoolantRecipe::getThermalEnthalpy),
                Codec.DOUBLE.fieldOf("conductivity").forGetter(GasCoolantRecipe::getConductivity)
        ).apply(instance, GasCoolantRecipe::new)), StreamCodec.composite(
                IngredientCreatorAccess.chemicalStack().streamCodec(), ChemicalToChemicalRecipe::getInput,
                ChemicalStack.STREAM_CODEC, BasicChemicalToChemicalRecipe::getOutputRaw,
                ByteBufCodecs.DOUBLE, GasCoolantRecipe::getThermalEnthalpy,
                ByteBufCodecs.DOUBLE, GasCoolantRecipe::getConductivity,
                GasCoolantRecipe::new
        ));
    }
}
