package ml.northwestwind.fissionrecipe.recipe;

import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class FissionRecipe extends GasToGasRecipe {
    public static final ResourceLocation RECIPE_TYPE_ID = new ResourceLocation(Mekanism.MODID, "fission");
    private final float heat;

    public FissionRecipe(ResourceLocation id, GasStackIngredient input, GasStack output, float heat) {
        super(id, input, output);
        this.heat = heat;
    }

    @Override
    public ItemStack assemble(IgnoredIInventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEvent.Recipes.FISSION.getSerializer();
    }

    @Override
    public IRecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(RECIPE_TYPE_ID);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeFloat(this.heat);
    }

    public float getHeat() {
        return heat;
    }
}
