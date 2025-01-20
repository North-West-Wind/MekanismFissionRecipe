package in.northwestw.fissionrecipe;

import in.northwestw.fissionrecipe.recipe.*;
import in.northwestw.fissionrecipe.recipe.serializer.*;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(MekanismFission.MOD_ID)
public class MekanismFission {
    public static final String MOD_ID = "fissionrecipe";
    public static final Logger LOGGER = LogManager.getLogger();

    public MekanismFission(IEventBus bus) {
        RecipeTypes.register(bus);
        RecipeSerializers.register(bus);
    }

    public static class RecipeTypes {
        private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Mekanism.MODID);
        public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> FISSION = TYPES.register("fission", simpleType("fission"));
        public static final DeferredHolder<RecipeType<?>, RecipeType<FluidCoolantRecipe>> FLUID_COOLANT = TYPES.register("fluid_coolant", simpleType("fluid_coolant"));
        public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> GAS_COOLANT = TYPES.register("gas_coolant", simpleType("gas_coolant"));

        public static final DeferredHolder<RecipeType<?>, RecipeType<BoilerRecipe>> BOILER = TYPES.register("boiler", simpleType("boiler"));
        public static final DeferredHolder<RecipeType<?>, RecipeType<ChemicalToChemicalRecipe>> HEATED_COOLANT = TYPES.register("heated_coolant", simpleType("heated_coolant"));

        private static void register(IEventBus bus) {
            TYPES.register(bus);
        }

        private static <T extends Recipe<?>> Supplier<RecipeType<T>> simpleType(String name) {
            return () -> RecipeType.simple(Mekanism.rl(name));
        }
    }

    public static class RecipeSerializers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Mekanism.MODID);
        public static final DeferredHolder<RecipeSerializer<?>, FissionRecipeSerializer> FISSION = SERIALIZERS.register("fission", FissionRecipeSerializer::create);
        public static final DeferredHolder<RecipeSerializer<?>, FluidCoolantRecipeSerializer> FLUID_COOLANT = SERIALIZERS.register("fluid_coolant", FluidCoolantRecipeSerializer::create);
        public static final DeferredHolder<RecipeSerializer<?>, GasCoolantRecipeSerializer> GAS_COOLANT = SERIALIZERS.register("gas_coolant", GasCoolantRecipeSerializer::create);

        public static final DeferredHolder<RecipeSerializer<?>, BoilerRecipeSerializer> BOILER = SERIALIZERS.register("boiler", BoilerRecipeSerializer::new);
        public static final DeferredHolder<RecipeSerializer<?>, HeatedCoolantRecipeSerializer> HEATED_COOLANT = SERIALIZERS.register("heated_coolant", HeatedCoolantRecipeSerializer::new);

        private static void register(IEventBus bus) {
            SERIALIZERS.register(bus);
        }
    }
}
