package ml.northwestwind.fissionrecipe;

import mekanism.common.Mekanism;
import ml.northwestwind.fissionrecipe.events.RegistryEvents;
import ml.northwestwind.fissionrecipe.recipe.FissionRecipe;
import ml.northwestwind.fissionrecipe.recipe.FluidCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.GasCoolantRecipe;
import ml.northwestwind.fissionrecipe.recipe.serializer.FissionRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.FluidCoolantRecipeSerializer;
import ml.northwestwind.fissionrecipe.recipe.serializer.GasCoolantRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(MekanismFission.MOD_ID)
public class MekanismFission {
    public static final String MOD_ID = "fissionrecipe";

    public MekanismFission() {
        RegistryEvents.registerItems();
        RegistryEvents.registerGases();
        Recipes.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static class Recipes<S extends RecipeSerializer<?>> {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Mekanism.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Mekanism.MODID);
        public static final Recipes<FissionRecipeSerializer> FISSION = new Recipes<>(SERIALIZERS.register(FissionRecipe.location(), FissionRecipeSerializer::new));
        public static final Recipes<FluidCoolantRecipeSerializer> FLUID_COOLANT = new Recipes<>(SERIALIZERS.register(FluidCoolantRecipe.location(), FluidCoolantRecipeSerializer::new));
        public static final Recipes<GasCoolantRecipeSerializer> GAS_COOLANT = new Recipes<>(SERIALIZERS.register(GasCoolantRecipe.location(), GasCoolantRecipeSerializer::new));

        private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> customType(ResourceLocation rl) {
            return TYPES.register(rl.getPath(), () -> new RecipeType<T>() {
                public String toString() {
                    return rl.toString();
                }
            });
        }
        RegistryObject<RecipeType<Recipe<?>>> type;
        RegistryObject<S> serializer;

        private Recipes(RegistryObject<S> serializer) {
            this.serializer = serializer;
            this.type = customType(serializer.getId());
        }

        public S getSerializer() {
            return serializer.get();
        }

        @SuppressWarnings("unchecked")
        public <T extends RecipeType<?>> T getType() {
            return (T) type.get();
        }

        public static void register(IEventBus bus) {
            SERIALIZERS.register(bus);
            TYPES.register(bus);
        }
    }

}
