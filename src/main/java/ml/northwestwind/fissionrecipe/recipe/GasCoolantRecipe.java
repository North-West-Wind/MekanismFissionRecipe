package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.MekanismFission;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public class GasCoolantRecipe extends GasToGasRecipe {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "gas_coolant");
    private final double thermalEnthalpy, conductivity;

    public GasCoolantRecipe(ResourceLocation id, ChemicalStackIngredient.GasStackIngredient input, GasStack output, double thermalEnthalpy, double conductivity) {
        super(id, input, output);
        this.thermalEnthalpy = thermalEnthalpy;
        this.conductivity = conductivity;
    }

    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismFission.Recipes.GAS_COOLANT.getSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return MekanismFission.Recipes.GAS_COOLANT.getType();
    }

    public GasStack getOutputRepresentation() {
        return this.output.copy();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeDouble(this.thermalEnthalpy);
        buffer.writeDouble(this.conductivity);
    }

    public double getThermalEnthalpy() {
        return thermalEnthalpy;
    }

    public double getConductivity() {
        return conductivity;
    }

    public static String location() {
        return RECIPE_TYPE_ID.getPath();
    }
}
