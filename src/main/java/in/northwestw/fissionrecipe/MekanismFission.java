package in.northwestw.fissionrecipe;

import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.common.Mekanism;
import in.northwestw.fissionrecipe.recipe.FissionRecipe;
import in.northwestw.fissionrecipe.recipe.FluidCoolantRecipe;
import in.northwestw.fissionrecipe.recipe.GasCoolantRecipe;
import in.northwestw.fissionrecipe.recipe.serializer.FissionRecipeSerializer;
import in.northwestw.fissionrecipe.recipe.serializer.FluidCoolantRecipeSerializer;
import in.northwestw.fissionrecipe.recipe.serializer.GasCoolantRecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@net.neoforged.fml.common.Mod(MekanismFission.MOD_ID)
public class MekanismFission {
    public static final String MOD_ID = "fissionrecipe";
    public static final Logger LOGGER = LogManager.getLogger();

    public MekanismFission(IEventBus bus) {
        RecipeTypes.register(bus);
        RecipeSerializers.register(bus);
    }

    public static class RecipeTypes {
        private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Mekanism.MODID);
        public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> FISSION = TYPES.register("fission", () -> RecipeType.simple(Mekanism.rl("fission")));
        public static final DeferredHolder<RecipeType<?>, RecipeType<FluidChemicalToChemicalRecipe>> FLUID_COOLANT = TYPES.register("fluid_coolant", () -> RecipeType.simple(Mekanism.rl("fluid_coolant")));
        public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> GAS_COOLANT = TYPES.register("gas_coolant", () -> RecipeType.simple(Mekanism.rl("gas_coolant")));

        private static void register(IEventBus bus) {
            TYPES.register(bus);
        }
    }

    public static class RecipeSerializers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Mekanism.MODID);
        public static final DeferredHolder<RecipeSerializer<?>, FissionRecipeSerializer> FISSION = SERIALIZERS.register("fission", FissionRecipeSerializer::create);
        public static final DeferredHolder<RecipeSerializer<?>, FluidCoolantRecipeSerializer> FLUID_COOLANT = SERIALIZERS.register("fluid_coolant", FluidCoolantRecipeSerializer::create);
        public static final DeferredHolder<RecipeSerializer<?>, GasCoolantRecipeSerializer> GAS_COOLANT = SERIALIZERS.register("gas_coolant", GasCoolantRecipeSerializer::create);

        private static void register(IEventBus bus) {
            SERIALIZERS.register(bus);
        }
    }
}
